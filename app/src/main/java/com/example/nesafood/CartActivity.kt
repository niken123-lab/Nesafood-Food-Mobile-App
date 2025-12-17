package com.example.nesafood

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.nesafood.databinding.ActivityCartBinding
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    private lateinit var b: ActivityCartBinding
    private lateinit var adapter: CartAdapter
    private val cartItems: MutableList<Cart> = mutableListOf()

    // ====== Ubah ke domain kamu sendiri kalau perlu ======
    private val BASE = "http://10.205.130.250/nesafood"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCartBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Ambil data dari intent (opsional – tetap gunakan CartManager sbg sumber kebenaran)
        intent.getParcelableArrayListExtra<Cart>("cart_items")?.let { incoming ->
            cartItems.clear()
            cartItems.addAll(incoming.filter { it.qty > 0 })
        }

        adapter = CartAdapter(cartItems) { subtotal -> updateSummary(subtotal) }
        b.rvCart.layoutManager = LinearLayoutManager(this)
        b.rvCart.adapter = adapter
        adapter.notifySubtotal()

        b.btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doCheckout()
        }
    }

    override fun onResume() {
        super.onResume()
        cartItems.clear()
        cartItems.addAll(CartManager.getAll())
        adapter.notifyDataSetChanged()
        adapter.notifySubtotal()

        // Jika baru balik dari Snap, lanjut polling status
        val pid = getSharedPreferences("UserData", MODE_PRIVATE).getInt("last_parent_id", 0)
        if (pid > 0) pollStatus(pid)
    }

    private fun updateSummary(subtotal: Int) {
        b.tvGrandTotal.text = "Total: ${subtotal.toRupiah()}"
    }

    // ================== CHECKOUT ==================
    private fun doCheckout() {
        // 0) Validasi user id (HARUS id_username yang valid di tabel users)
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)
        if (userId <= 0) {
            Toast.makeText(
                this,
                "User belum valid. Pastikan saat login menyimpan id_username ke SharedPreferences.",
                Toast.LENGTH_LONG
            ).show()
            android.util.Log.e("CHECKOUT", "INVALID user_id = $userId")
            return
        }

        // 1) Kelompokkan item per toko
        val grouped: Map<Int, List<Cart>> = cartItems.groupBy { it.id_penjual }

        // 2) Susun JSON shops[] (HANYA product_id & qty)
        val shopsArr = JSONArray().apply {
            grouped.forEach { (shopId, items) ->
                put(JSONObject().apply {
                    put("shop_id", shopId)
                    put("shipping_method", "REG")
                    put("items", JSONArray().apply {
                        items.forEach { c ->
                            val pid = c.idMenu.toIntOrNull() ?: 0
                            if (pid > 0 && c.qty > 0) {
                                put(JSONObject().apply {
                                    put("product_id", pid)
                                    put("qty", c.qty)
                                })
                            }
                        }
                    })
                })
            }
        }

        if (shopsArr.length() == 0) {
            Toast.makeText(this, "Tidak ada item valid untuk checkout.", Toast.LENGTH_SHORT).show()
            return
        }

        val body = JSONObject().apply {
            put("user_id", userId)
            put("shops", shopsArr)
        }

        val req = object : JsonObjectRequest(
            Request.Method.POST,
            Api.CHECKOUT, // pastikan Api.CHECKOUT = "$BASE/checkout.php"
            body,
            { resp ->
                try {
                    val parentId = resp.getInt("parent_order_id")

                    // simpan untuk polling setelah user kembali dari Snap
                    getSharedPreferences("UserData", MODE_PRIVATE)
                        .edit().putInt("last_parent_id", parentId).apply()

                    openSnap(parentId)     // buka Snap (Midtrans)
                } catch (e: Exception) {
                    Toast.makeText(this, "Respon tidak sesuai: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode
                val bodyBytes = error.networkResponse?.data
                val bodyStr = if (bodyBytes != null) String(bodyBytes) else "(no body)"
                android.util.Log.e("CHECKOUT", "HTTP $code: $bodyStr")

                var msg = "Checkout gagal ($code)"
                try {
                    val j = JSONObject(bodyStr)
                    val detail = j.optString("detail")
                    if (detail.isNotEmpty()) msg = "$msg\n$detail"
                } catch (_: Exception) {
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        ) {}.apply {
            retryPolicy = DefaultRetryPolicy(20_000, 0, 1f)
        }

        Volley.newRequestQueue(this).add(req)
    }

    // ================== SNAP ==================
    private fun openSnap(parentId: Int) {
        val url = "$BASE/create_snap.php?parent_id=$parentId"
        val req = JsonObjectRequest(
            Request.Method.GET, url, null,
            { r ->
                val redirect = r.optString("redirect_url", "")
                if (redirect.isNotEmpty()) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(redirect)))
                } else {
                    Toast.makeText(this, "Gagal ambil Snap URL", Toast.LENGTH_SHORT).show()
                }
            },
            { e -> Toast.makeText(this, "Gagal Snap: ${e.message}", Toast.LENGTH_LONG).show() }
        ).apply {
            retryPolicy = DefaultRetryPolicy(20_000, 0, 1f)
        }
        Volley.newRequestQueue(this).add(req)
    }

    // ================== POLLING STATUS ==================
    private fun pollStatus(parentId: Int) {
        val url =
            "http://10.205.130.250/nesafood/status_sync.php?parent_id=$parentId" // ⬅️ ganti ke status_sync
        val req = com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET, url,
            { body ->
                try {
                    val obj = org.json.JSONObject(body)
                    val parent = obj.getJSONObject("parent")
                    val status = parent.getString("status")
                    if (status == "PAID") {
                        // bersihkan flag & keranjang
                        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                            .remove("last_parent_id").apply()
                        CartManager.clear()

                        // pindah ke StatusActivity
                        val i = Intent(this, StatusActivity::class.java)
                        i.putExtra("status_from", "paid")
                        startActivity(i)
                        finish()
                    } else if (status == "CANCELLED") {
                        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                            .remove("last_parent_id").apply()
                        Toast.makeText(this, "Pembayaran dibatalkan/expired.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        // masih pending → ulangi 4 detik lagi
                        b.rvCart.postDelayed({ pollStatus(parentId) }, 4000)
                    }
                } catch (_: Exception) {
                    b.rvCart.postDelayed({ pollStatus(parentId) }, 4000)
                }
            },
            {
                // error jaringan → coba lagi
                b.rvCart.postDelayed({ pollStatus(parentId) }, 4000)
            }
        )
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(req)
    }
}