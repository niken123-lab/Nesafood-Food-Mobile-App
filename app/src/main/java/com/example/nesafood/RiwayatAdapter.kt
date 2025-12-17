package com.example.nesafood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RiwayatAdapter(private val listRiwayat: List<Pesanan>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaMenu: TextView = itemView.findViewById(R.id.tvNamaMenu)
        val tvTanggalPesanan: TextView = itemView.findViewById(R.id.tvTanggalPesanan)
        val tvStatusPesanan: TextView = itemView.findViewById(R.id.tvStatusPesanan)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = listRiwayat.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listRiwayat[position]

        holder.tvNamaMenu.text = item.items
        holder.tvTanggalPesanan.text = item.tanggal ?: "-"
        holder.tvTotalHarga.text = "${item.total.toRupiah()}"
        holder.tvStatusPesanan.text = item.status

        val context = holder.itemView.context

        // 🔹 Warna status
        when (item.status.uppercase()) {
            "SELESAI", "DONE" -> holder.tvStatusPesanan.setTextColor(
                ContextCompat.getColor(context, R.color.green)
            )
            "SIAP AMBIL" -> holder.tvStatusPesanan.setTextColor(
                ContextCompat.getColor(context, R.color.primarycolor)
            )
            "DIPROSES", "PROSES" -> holder.tvStatusPesanan.setTextColor(
                ContextCompat.getColor(context, R.color.nesa_orange)
            )
            else -> holder.tvStatusPesanan.setTextColor(
                ContextCompat.getColor(context, R.color.black)
            )
        }
    }
}
