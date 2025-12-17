package com.example.nesafood

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class StatusPemesananActivity : AppCompatActivity() {

    private val allPesanan = mutableListOf<Pesanan>()
    private val listDiterima = mutableListOf<Pesanan>()
    private val listDiproses = mutableListOf<Pesanan>()
    private val listSiapAmbil = mutableListOf<Pesanan>()

    private lateinit var adpDiterima: PesananAdapter
    private lateinit var adpDiproses: PesananAdapter
    private lateinit var adpSiapAmbil: PesananAdapter

    private lateinit var rvDiterima: RecyclerView
    private lateinit var rvDiproses: RecyclerView
    private lateinit var rvSiapAmbil: RecyclerView

    private val baseUrl = "http://10.205.130.250/nesafood"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        // === Setup RecyclerView ===
        rvDiterima = findViewById(R.id.rvDiterima)
        rvDiproses = findViewById(R.id.rvDiproses)
        rvSiapAmbil = findViewById(R.id.rvSiapAmbil)

        rvDiterima.layoutManager = LinearLayoutManager(this)
        rvDiproses.layoutManager = LinearLayoutManager(this)
        rvSiapAmbil.layoutManager = LinearLayoutManager(this)

        adpDiterima = PesananAdapter(listDiterima) { pesanan, _ -> ubahStatus(pesanan, "DIPROSES") }
        adpDiproses = PesananAdapter(listDiproses) { pesanan, _ -> ubahStatus(pesanan, "SIAP AMBIL") }
        adpSiapAmbil = PesananAdapter(listSiapAmbil) { pesanan, _ -> ubahStatus(pesanan, "SELESAI") }


        rvDiterima.adapter = adpDiterima
        rvDiproses.adapter = adpDiproses
        rvSiapAmbil.adapter = adpSiapAmbil

        // Ambil pesanan awal
        ambilPesananDariServer()

        // 🔹 Cek intent dari PenjualActivity
        val targetTab = intent.getStringExtra("tab")
        if (targetTab == "diproses") {
            rvDiproses.postDelayed({
                ambilPesananDariServer()
                rvDiproses.smoothScrollToPosition(0)
                Toast.makeText(this, "Menampilkan pesanan yang sedang diproses", Toast.LENGTH_SHORT).show()
            }, 400)
        }
    }

    // ========================= 🔻 SERVER COMMUNICATION =========================

    private fun ambilPesananDariServer() {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val role = prefs.getString("role", "pembeli")
        val userId = prefs.getInt("user_id", 0)
        val username = prefs.getString("username", "")

        if (userId == 0) {
            Toast.makeText(this, "User belum login dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        val url =
            if (role == "penjual") "$baseUrl/list_pesanan_penjual.php"
            else "$baseUrl/list_pesanan.php"

        val req = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val trimmed = response.trim()
                    if (trimmed.startsWith("{")) {
                        // Kalau error dari server
                        val err = JSONObject(trimmed)
                        Toast.makeText(
                            this,
                            err.optString("error", "Terjadi kesalahan server"),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Parsing daftar pesanan
                        val arr = JSONArray(trimmed)
                        allPesanan.clear()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            allPesanan.add(
                                Pesanan(
                                    id = obj.optString("id_pesanan"),
                                    namaPembeli = obj.optString("nama_pembeli", "-"),
                                    items = obj.optString("items", "-"),
                                    total = obj.optInt("total", 0),
                                    status = obj.optString("status", "PENDING")
                                )
                            )
                        }
                        refreshLists()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal parsing data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {

            override fun getParams(): MutableMap<String, String> {
                val p = HashMap<String, String>()
                if (role == "penjual") p["shop_username"] = username ?: ""
                else p["user_id"] = userId.toString()
                return p
            }
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun ubahStatus(pesanan: Pesanan, statusBaru: String) {
        val url = "$baseUrl/update_status_order.php"
        val req = object : StringRequest(Method.POST, url,
            { response ->
                Toast.makeText(this, "Server: $response", Toast.LENGTH_LONG).show() // 👈 tampilkan JSON mentah
                try {
                    val obj = JSONObject(response)
                    if (obj.optBoolean("success", false)) {
                        Toast.makeText(this, "Status diperbarui ke $statusBaru", Toast.LENGTH_SHORT).show()
                        android.os.Handler(mainLooper).postDelayed({
                            ambilPesananDariServer()
                        }, 1000)
                    } else {
                        Toast.makeText(this, obj.optString("message", "Gagal update"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Kesalahan parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Update gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("id_pesanan" to pesanan.id, "status_baru" to statusBaru)
        }
        Volley.newRequestQueue(this).add(req)
    }



    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val autoRefresh = object : Runnable {
        override fun run() {
            ambilPesananDariServer()
            handler.postDelayed(this, 10_000) // tiap 10 detik
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(autoRefresh)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoRefresh)
    }


    // ========================= 🔻 UI UPDATER =========================
    private fun normalize(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        val clean = s.uppercase()
            .replace("_", " ")
            .replace("-", " ")
            .replace(Regex("\\s+"), " ") // hapus spasi ganda
            .trim()
        return clean
    }



    private fun refreshLists() {
        listDiterima.clear(); listDiproses.clear(); listSiapAmbil.clear()
        // 🔹 Debug: cek semua status mentah dari server
        for (p in allPesanan) {
            android.util.Log.d("STATUS_DEBUG", "ID=${p.id} | Status='${p.status}'")
        }
        listDiterima.addAll(allPesanan.filter {
            val st = normalize(it.status)
            st in listOf("PAID", "PENDING", "DITERIMA")
        })
        listDiproses.addAll(allPesanan.filter {
            val st = normalize(it.status)
            st in listOf("PROSES", "DIPROSES")
        })
        listSiapAmbil.addAll(allPesanan.filter {
            val st = normalize(it.status)
            st in listOf("READY", "SIAP AMBIL")
        })

        adpDiterima.notifyDataSetChanged()
        adpDiproses.notifyDataSetChanged()
        adpSiapAmbil.notifyDataSetChanged()
    }



    private fun fadeIn(rv: RecyclerView) {
        rv.alpha = 0f
        rv.animate().alpha(1f).setDuration(300).start()
    }
}
