package com.example.nesafood

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.view.ContextThemeWrapper


class RiwayatPesananActivity : AppCompatActivity() {

    private val listRiwayat = mutableListOf<Pesanan>()
    private val listSemua = mutableListOf<Pesanan>() // backup semua data
    private lateinit var adapter: RiwayatAdapter
    private val baseUrl = "http://10.205.130.250/nesafood"

    private lateinit var btnPilihTanggal: Button
    private lateinit var btnResetTanggal: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        val rv = findViewById<RecyclerView>(R.id.rvRiwayatPesanan)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = RiwayatAdapter(listRiwayat)
        rv.adapter = adapter

        // 🔹 Hubungkan tombol filter di layout
        btnPilihTanggal = findViewById(R.id.btnPilihTanggal)
        btnResetTanggal = findViewById(R.id.btnResetTanggal)

        // 🔹 Ambil data awal
        ambilRiwayatPembeli()

        // 🔹 Event filter tanggal
        btnPilihTanggal.setOnClickListener { pilihTanggal() }
        btnResetTanggal.setOnClickListener { resetFilter() }
    }

    private fun ambilRiwayatPembeli() {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)

        if (userId == 0) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$baseUrl/list_riwayat_pesanan.php"

        val req = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.optBoolean("success", false)) {
                        val arr = obj.getJSONArray("data")
                        listRiwayat.clear()
                        listSemua.clear()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val pesanan = Pesanan(
                                id = o.getString("id_pesanan"),
                                namaPembeli = o.optString("nama_toko", "-"),
                                items = o.optString("items", "-"),
                                total = o.optInt("total", 0),
                                status = o.optString("status", "-"),
                                tanggal = o.optString("tanggal", "-")
                            )
                            listRiwayat.add(pesanan)
                            listSemua.add(pesanan)
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, obj.optString("message", "Belum ada riwayat"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Kesalahan parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("id_user" to userId.toString())
        }

        Volley.newRequestQueue(this).add(req)
    }

    // 🔹 Pilih tanggal lewat DatePicker
    private fun pilihTanggal() {
        val kalender = Calendar.getInstance()
        val dialog = DatePickerDialog(
            ContextThemeWrapper(this, R.style.CustomDatePickerTheme),
            { _, tahun, bulan, hari ->
                val format = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = Calendar.getInstance().apply { set(tahun, bulan, hari) }
                val tanggalDipilih = format.format(date.time)
                filterTanggal(tanggalDipilih)
            },
            kalender.get(Calendar.YEAR),
            kalender.get(Calendar.MONTH),
            kalender.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()

    }

    // 🔹 Filter daftar sesuai tanggal yang dipilih
    private fun filterTanggal(tanggal: String) {
        val hasil = listSemua.filter { it.tanggal?.contains(tanggal, ignoreCase = true) == true }
        listRiwayat.clear()
        listRiwayat.addAll(hasil)
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Menampilkan pesanan tanggal $tanggal", Toast.LENGTH_SHORT).show()
    }

    // 🔹 Kembalikan semua data
    private fun resetFilter() {
        listRiwayat.clear()
        listRiwayat.addAll(listSemua)
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Filter direset", Toast.LENGTH_SHORT).show()
    }
}
