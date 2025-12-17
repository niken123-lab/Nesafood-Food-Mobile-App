package com.example.nesafood

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject

class PenjualActivity : AppCompatActivity() {

    private lateinit var rvPesanan: RecyclerView
    private lateinit var btnStatus: LinearLayout
    private lateinit var btnTutup: LinearLayout
    private lateinit var btnRiwayatPenjual: LinearLayout
    private lateinit var btnProfile: LinearLayout
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnEditMenu: Button
    private lateinit var adapter: PesananAdapter

    private val listPesanan = mutableListOf<Pesanan>()
    private val baseUrl = "http://10.205.130.250/nesafood"
    private var statusToko = "buka"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_penjual)

        // === Binding elemen XML ===
        rvPesanan = findViewById(R.id.rvPesanan)
        btnStatus = findViewById(R.id.btnStatus)
        btnTutup = findViewById(R.id.btnTutup)
        btnRiwayatPenjual = findViewById(R.id.btnRiwayatPenjual)
        btnProfile = findViewById(R.id.btnProfile)
        btnEditMenu = findViewById(R.id.btnEditMenu)
        fabTambah = findViewById(R.id.fabTambah)

        // === SharedPreferences ===
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = prefs.getString("username", "")
        statusToko = prefs.getString("status_toko", "buka") ?: "buka"
        updateTampilanTombol(statusToko)

        if (!username.isNullOrEmpty()) loadStatusTokoDariServer(username)

        // === RecyclerView setup ===
        adapter = PesananAdapter(listPesanan) { pesanan, position ->
            onPesananOkClicked(pesanan, position)
        }

        rvPesanan.layoutManager = LinearLayoutManager(this)
        rvPesanan.adapter = adapter

        // === Ambil pesanan dari server ===
        ambilPesananDariServer()

        // === Tombol Navigasi Bawah ===
        btnStatus.setOnClickListener {
            startActivity(Intent(this, StatusPemesananActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnRiwayatPenjual.setOnClickListener {
            startActivity(Intent(this, RiwayatPenjualActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnEditMenu.setOnClickListener {
            startActivity(Intent(this, ManageMenuActivity::class.java))
        }

        fabTambah.setOnClickListener {
            startActivity(Intent(this, Tambah_Menu_Penjual::class.java))
        }

        btnTutup.setOnClickListener {
            val newStatus = if (statusToko == "buka") "tutup" else "buka"
            ubahStatusToko(username ?: "", newStatus)
        }
    }

    // ==================== 🔹 Ambil Pesanan dari Server ====================
    private fun ambilPesananDariServer() {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val idPenjual = prefs.getString("id_penjual", "") ?: ""

        if (idPenjual.isEmpty()) {
            Toast.makeText(this, "ID penjual kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$baseUrl/list_pesanan_penjual.php"
        val req = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val dataObj = obj.optJSONObject("data")

                    listPesanan.clear()
                    if (dataObj != null) {
                        val diterima = dataObj.optJSONArray("diterima") ?: JSONArray()
                        val diproses = dataObj.optJSONArray("diproses") ?: JSONArray()
                        val siapAmbil = dataObj.optJSONArray("siap_ambil") ?: JSONArray()

                        val semuaPesanan = mutableListOf<JSONObject>()
                        for (i in 0 until diterima.length()) semuaPesanan.add(diterima.getJSONObject(i))
                        for (i in 0 until diproses.length()) semuaPesanan.add(diproses.getJSONObject(i))
                        for (i in 0 until siapAmbil.length()) semuaPesanan.add(siapAmbil.getJSONObject(i))

                        for (o in semuaPesanan) {
                            listPesanan.add(
                                Pesanan(
                                    id = o.getString("id_pesanan"),
                                    namaPembeli = o.getString("nama_pembeli"),
                                    items = o.getString("items"),
                                    total = o.getInt("total"),
                                    status = o.getString("status")
                                )
                            )
                        }
                    } else {
                        val arr = obj.getJSONArray("data")
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            listPesanan.add(
                                Pesanan(
                                    id = o.getString("id_pesanan"),
                                    namaPembeli = o.getString("nama_pembeli"),
                                    items = o.getString("items"),
                                    total = o.getInt("total"),
                                    status = o.getString("status")
                                )
                            )
                        }
                    }

                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_penjual" to idPenjual)
            }
        }

        Volley.newRequestQueue(this).add(req)
    }

    // 🔄 Auto-refresh pesanan tiap 8 detik
    private val handler = Handler(Looper.getMainLooper())
    private val autoRefresh = object : Runnable {
        override fun run() {
            ambilPesananDariServer()
            handler.postDelayed(this, 8_000)
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

    // ==================== 🔹 Status Toko ====================
    private fun loadStatusTokoDariServer(username: String) {
        val req = object : StringRequest(
            Request.Method.POST, "$baseUrl/get_status_toko.php",
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getBoolean("success")) {
                        statusToko = obj.getString("status_toko")
                        updateTampilanTombol(statusToko)
                        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                            .putString("status_toko", statusToko).apply()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal cek status toko: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("username" to username)
            }
        }
        Volley.newRequestQueue(this).add(req)
    }

    private fun ubahStatusToko(username: String, newStatus: String) {
        val req = object : StringRequest(
            Request.Method.POST, "$baseUrl/update_status_toko.php",
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getBoolean("success")) {
                        statusToko = newStatus
                        updateTampilanTombol(statusToko)
                        broadcastStatusToko(statusToko)
                        getSharedPreferences("UserData", MODE_PRIVATE).edit()
                            .putString("status_toko", statusToko).apply()
                        val pesan = if (newStatus == "tutup") "Toko ditutup sementara"
                        else "Toko dibuka kembali"
                        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Kesalahan parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("username" to username, "status_toko" to newStatus)
            }
        }
        Volley.newRequestQueue(this).add(req)
    }

    private fun updateTampilanTombol(status: String) {
        val labelTutup = (btnTutup.getChildAt(1) as? TextView)
        labelTutup?.text = if (status == "tutup") "BUKA TOKO" else "TUTUP TOKO"
    }

    // ==================== 🔹 Klik Pesanan OK ====================
    private fun onPesananOkClicked(pesanan: Pesanan, position: Int) {
        val url = "$baseUrl/update_status_order.php"
        val req = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.optBoolean("success")) {
                        listPesanan.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        Toast.makeText(
                            this,
                            "Pesanan ${pesanan.namaPembeli} sedang diproses...",
                            Toast.LENGTH_SHORT
                        ).show()
                        kirimStatusKePembeli(pesanan.id, "DIPROSES")
                    } else {
                        Toast.makeText(this, obj.optString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Kesalahan parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_pesanan" to pesanan.id, "status_baru" to "DIPROSES")
            }
        }
        Volley.newRequestQueue(this).add(req)
    }

    private fun kirimStatusKePembeli(idPesanan: String, statusBaru: String) {
        val url = "$baseUrl/update_status_pembeli.php"
        val req = object : StringRequest(
            Request.Method.POST, url, {}, { error ->
                println("Gagal kirim ke pembeli: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_pesanan" to idPesanan, "status_baru" to statusBaru)
            }
        }
        Volley.newRequestQueue(this).add(req)
    }

    private fun broadcastStatusToko(status: String) {
        try {
            val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
            val username = prefs.getString("username", "") ?: ""
            val req = object : StringRequest(
                Request.Method.POST, "$baseUrl/update_status_toko_global.php",
                {}, { error -> println("❌ Gagal broadcast status: ${error.message}") }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    return hashMapOf("username" to username, "status_toko" to status)
                }
            }
            Volley.newRequestQueue(this).add(req)
        } catch (e: Exception) {
            println("⚠️ Error broadcast status: ${e.message}")
        }
    }
}
