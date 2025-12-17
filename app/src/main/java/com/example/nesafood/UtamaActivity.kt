package com.example.nesafood

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.content.Intent
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

class UtamaActivity : AppCompatActivity() {

    private lateinit var adapter: RestoranAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private val listMenu = mutableListOf<Restoran>()

    private val BASE_URL = "http://10.205.130.250/nesafood"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utama)

        // === FCM Token untuk notifikasi ===
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM_TOKEN", "Token: ${task.result}")
            } else {
                Log.e("FCM_TOKEN", "Gagal ambil token", task.exception)
            }
        }

        // === Binding view ===
        recyclerView = findViewById(R.id.rvMenu)
        searchInput = findViewById(R.id.search_input)

        val btnCart = findViewById<ImageButton>(R.id.btnCart)
        val btnStatus = findViewById<LinearLayout>(R.id.btnStatus)
        val btnRiwayat = findViewById<LinearLayout>(R.id.btnRiwayat)
        val btnHome = findViewById<LinearLayout>(R.id.btnHome)
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfile)

        // === Tombol Cart ===
        btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            intent.putParcelableArrayListExtra("cart_items", ArrayList(CartManager.getAll()))
            startActivity(intent)
        }

        // === Tombol Navigasi Bawah ===
        btnStatus.setOnClickListener {
            startActivity(Intent(this, StatusListActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatPesananActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnHome.setOnClickListener {
            Toast.makeText(this, "Kamu sudah di halaman utama", Toast.LENGTH_SHORT).show()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // === RecyclerView ===
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RestoranAdapter(mutableListOf())
        recyclerView.adapter = adapter

        // === Cek role user (pembeli / penjual) ===
        val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
        val idPenjual = sharedPref.getString("id_penjual", null)
        val role = sharedPref.getString("role", "pembeli")

        if (role == "penjual" && idPenjual != null) {
            loadMenuData(idPenjual)
        } else {
            loadAllPenjual()
        }

        // === Fitur pencarian ===
        searchInput.addTextChangedListener { text ->
            adapter.filter(text.toString())
        }
    }

    // ==================== 🔹 Pembeli Melihat Semua Penjual ====================
    private fun loadAllPenjual() {
        val url = "$BASE_URL/get_penjual.php"
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getBoolean("success")) {
                        val dataArray = obj.getJSONArray("data")
                        val tempList = mutableListOf<Restoran>()

                        for (i in 0 until dataArray.length()) {
                            val p = dataArray.getJSONObject(i)
                            val idPenjual = p.getString("id_penjual")
                            val namaToko = p.getString("nama_toko")
                            val fotoToko = p.optString(
                                "foto_profil",
                                "http://10.205.130.250/nesafood/uploads/default.jpg"
                            )
                            val statusToko = p.optString("status_toko", "buka")

                            tempList.add(
                                Restoran(
                                    nama = namaToko,
                                    deskripsi = if (statusToko == "tutup") "Toko Tutup" else "Ketuk untuk lihat menu",
                                    gambar = fotoToko,
                                    id_penjual = idPenjual,
                                    status_toko = statusToko
                                )
                            )
                        }

                        adapter.updateData(tempList)
                    } else {
                        Toast.makeText(this, "Data kosong", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal parsing data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal memuat penjual: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    // ==================== 🔹 Penjual Melihat Menu Miliknya Sendiri ====================
    private fun loadMenuData(idPenjual: String) {
        val url = "$BASE_URL/get_menu.php?id_penjual=$idPenjual"
        val queue = Volley.newRequestQueue(this)

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val tempList = mutableListOf<Restoran>()
                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val nama = obj.getString("nama_menu")
                    val deskripsi = obj.optString("deskripsi", "")
                    val harga = obj.getDouble("harga")

                    tempList.add(
                        Restoran(
                            nama = "$nama - Rp${harga.toInt()}",
                            deskripsi = deskripsi,
                            gambar = "http://10.205.130.250/nesafood/uploads/default.jpg",
                            id_penjual = idPenjual
                        )
                    )
                }
                adapter.updateData(tempList)
            },
            { error ->
                Toast.makeText(this, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
        val idPenjual = sharedPref.getString("id_penjual", null)
        val role = sharedPref.getString("role", "pembeli")

        if (role == "penjual" && idPenjual != null) {
            loadMenuData(idPenjual)
        } else {
            loadAllPenjual()
        }
    }
}
