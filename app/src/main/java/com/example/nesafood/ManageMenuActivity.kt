package com.example.nesafood

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ManageMenuActivity : AppCompatActivity() {

    private lateinit var adapter: MenuEditAdapter
    private val listMenu = mutableListOf<MenuModel>()
    private val urlGetMenu = "http://10.205.130.250/nesafood/get_menu_penjual.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_menu)

        val rvListMenu = findViewById<RecyclerView>(R.id.rvListMenu)
        rvListMenu.layoutManager = LinearLayoutManager(this)

        adapter = MenuEditAdapter(listMenu, this)
        rvListMenu.adapter = adapter

        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val idPenjual = prefs.getString("id_penjual", null)

        if (idPenjual != null) {
            loadMenuDariServer(idPenjual)
        } else {
            Toast.makeText(this, "ID penjual tidak ditemukan!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMenuDariServer(idPenjual: String) {
        val request = object : StringRequest(
            Request.Method.POST, urlGetMenu,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getBoolean("success")) {
                        val dataArray = obj.getJSONArray("data")
                        listMenu.clear()

                        for (i in 0 until dataArray.length()) {
                            val data = dataArray.getJSONObject(i)
                            val menu = MenuModel(
                                id = data.getInt("id_menu"),
                                nama = data.getString("nama_menu"),
                                deskripsi = data.getString("deskripsi"),
                                harga = data.getInt("harga"),
                                stok = data.getInt("stok"),
                                gambar = data.getString("gambar")
                            )
                            listMenu.add(menu)
                        }

                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Kesalahan parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace() // tampilkan error di Logcat
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()

    }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_penjual"] = idPenjual
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}
