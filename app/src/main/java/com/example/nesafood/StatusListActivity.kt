package com.example.nesafood

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class StatusListActivity : AppCompatActivity() {

    private lateinit var rvParentOrders: RecyclerView
    private lateinit var tvEmpty: TextView
    private val list = mutableListOf<ParentOrder>()
    private val baseUrl = "http://10.205.130.250/nesafood"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_list)

        rvParentOrders = findViewById(R.id.rvParentOrders)
        tvEmpty = findViewById(R.id.tvEmpty)

        rvParentOrders.layoutManager = LinearLayoutManager(this)
        ambilDataDariServer()
    }

    private fun ambilDataDariServer() {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)

        if (userId == 0) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$baseUrl/list_parent_orders.php"
        val req = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.optBoolean("success")) {
                        val arr = obj.getJSONArray("data")
                        list.clear()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            list.add(
                                ParentOrder(
                                    id = o.getString("parent_id"),
                                    namaToko = o.getString("nama_toko"),
                                    total = o.getInt("grand_total"),
                                    tanggal = o.getString("tanggal"),
                                    status = o.getString ("status")
                                )
                            )
                        }

                        rvParentOrders.adapter = ParentOrderAdapter(this, list)
                        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        tvEmpty.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user_id" to userId.toString(),
                    "mode" to "aktif" )

            }
        }
        Volley.newRequestQueue(this).add(req)
    }
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val refresher = object : Runnable {
        override fun run() {
            ambilDataDariServer()
            handler.postDelayed(this, 8_000) // tiap 8 detik
        }
    }

    override fun onResume() {
        super.onResume()
        ambilDataDariServer() // refresh langsung begitu activity aktif
        handler.post(refresher)
    }

    override fun onPause() { super.onPause(); handler.removeCallbacks(refresher) }

}
