package com.example.nesafood

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import android.content.Intent
import android.widget.Button

class MenuDalam : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MenuAdapter
    private val listMenu = mutableListOf<MenuItem>()

    private val BASE_URL = "http://10.205.130.250/nesafood"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        recyclerView = findViewById(R.id.rvMenu)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MenuAdapter(listMenu)
        recyclerView.adapter = adapter

        val idPenjual = intent.getStringExtra("id_penjual")

        if (idPenjual != null) {
            loadMenu(idPenjual)
        } else {
            Toast.makeText(this, "ID penjual tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Tombol Checkout
        // Tombol Checkout
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            // Ambil semua item dengan qty > 0
            val orderedItems = ArrayList(
                listMenu.filter { it.qty > 0 }.map {
                        Cart(

                            idMenu = it.idMenu, // atau isi kalau kamu punya id dari server
                            nama = it.nama,
                            harga = it.harga.toInt(),
                            qty = it.qty,
                            imageUrl = it.gambar,
                            id_penjual = it.id_penjual
                        )

                }
            )

            if (orderedItems.isEmpty()) {
                Toast.makeText(this, "Belum ada item yang dipilih", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kirim data ke CartActivity
            val intent = Intent(this, CartActivity::class.java)
            intent.putParcelableArrayListExtra("cart_items", orderedItems)
            startActivity(intent)
        }
    }

        // 🔹 Ambil menu dari server
    private fun loadMenu(idPenjual: String) {
        val url = "$BASE_URL/get_menu.php?id_penjual=$idPenjual"
        val queue = Volley.newRequestQueue(this)

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response -> parseMenu(response) },
            { error ->
                Toast.makeText(this, "Gagal memuat menu: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        queue.add(request)
    }

    // 🔹 Parse hasil JSON dan isi list
    private fun parseMenu(response: JSONArray) {
        listMenu.clear()

        for (i in 0 until response.length()) {
            val obj = response.getJSONObject(i)
            val namaMenu = obj.getString("nama_menu")
            val harga = obj.optDouble("harga", 0.0)
            val gambarUrl = obj.optString("gambar_url", "$BASE_URL/uploads/default.jpg")
            val idPenjual  = obj.optInt("id_penjual", 0)

            listMenu.add(
                MenuItem(
                    idMenu = obj.getString("id_menu"),
                    nama = namaMenu,
                    harga = harga,
                    gambar = gambarUrl,
                    id_penjual = idPenjual
                )
            )
        }

        adapter.notifyDataSetChanged()
    }
}
