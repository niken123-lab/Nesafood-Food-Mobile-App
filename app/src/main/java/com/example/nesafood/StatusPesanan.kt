package com.example.nesafood

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StatusPesanan : AppCompatActivity() {

    private val listDiterima = mutableListOf<Pesanan>()
    private val listDiproses = mutableListOf<Pesanan>()
    private val listSiapAmbil = mutableListOf<Pesanan>()

    private lateinit var adpDiterima: StatusAdapter
    private lateinit var adpDiproses: StatusAdapter
    private lateinit var adpSiapAmbil: StatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        // 🧩 Data dummy
        listDiterima.addAll(
            listOf(
                Pesanan("1", "Nazia", "Nasi Gila x1\nEs Teh x2", 40000, "diterima"),
                Pesanan("2", "Budi", "Ayam Geprek x1", 23000, "diterima")
            )
        )

        // 🔹 Adapter untuk daftar “Diterima”
        adpDiterima = StatusAdapter(listDiterima) { pesanan, pos ->
            val removed = adpDiterima.removeAt(pos)
            removed.status = "diproses"
            listDiproses.add(removed)
            adpDiproses.notifyItemInserted(listDiproses.size - 1)
        }

        // 🔹 Adapter untuk daftar “Diproses”
        adpDiproses = StatusAdapter(listDiproses) { pesanan, pos ->
            val removed = adpDiproses.removeAt(pos)
            removed.status = "siap ambil"
            listSiapAmbil.add(removed)
            adpSiapAmbil.notifyItemInserted(listSiapAmbil.size - 1)
        }

        // 🔹 Adapter untuk daftar “Siap Ambil”
        adpSiapAmbil = StatusAdapter(listSiapAmbil) { pesanan, pos ->
            adpSiapAmbil.removeAt(pos)
        }

        // 🔹 Set adapter ke tiap RecyclerView
        findViewById<RecyclerView>(R.id.rvDiterima).apply {
            layoutManager = LinearLayoutManager(this@StatusPesanan)
            adapter = adpDiterima
        }
        findViewById<RecyclerView>(R.id.rvDiproses).apply {
            layoutManager = LinearLayoutManager(this@StatusPesanan)
            adapter = adpDiproses
        }
        findViewById<RecyclerView>(R.id.rvSiapAmbil).apply {
            layoutManager = LinearLayoutManager(this@StatusPesanan)
            adapter = adpSiapAmbil
        }
    }
}
