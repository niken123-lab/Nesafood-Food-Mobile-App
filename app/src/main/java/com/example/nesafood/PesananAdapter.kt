package com.example.nesafood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PesananAdapter(
    private val list: MutableList<Pesanan>,
    private val onOkClick: (Pesanan, Int) -> Unit
) : RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    inner class PesananViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaToko: TextView = itemView.findViewById(R.id.tvNamaToko)
        val tvDetailPesanan: TextView = itemView.findViewById(R.id.tvDetailPesanan)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHarga)
        val btnOk: Button = itemView.findViewById(R.id.btnOk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan, parent, false)
        return PesananViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val item = list[position]

        holder.tvNamaToko.text = item.namaPembeli
        holder.tvDetailPesanan.text = item.items
        holder.tvHarga.text = "Rp ${item.total}"

        holder.btnOk.setOnClickListener {
            onOkClick(item, position)
        }
    }

    override fun getItemCount(): Int = list.size

    fun removeAt(position: Int): Pesanan {
        val removed = list.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    fun add(item: Pesanan) {
        list.add(item)
        notifyItemInserted(list.size - 1)
    }
}
