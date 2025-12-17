package com.example.nesafood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class StatusAdapter(
    private val list: MutableList<Pesanan>,
    private val onOkClick: (Pesanan, Int) -> Unit
) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvNamaToko)
        val tvNama: TextView = itemView.findViewById(R.id.tvDetailPesanan)
        val tvItems: TextView = itemView.findViewById(R.id.tvHarga)
        val tvStatus: TextView = itemView.findViewById(R.id.btnOk)
        val btnOk: Button = itemView.findViewById(R.id.btnOk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val item = list[position]
        holder.tvId.text = item.id
        holder.tvNama.text = item.namaPembeli
        holder.tvItems.text = item.items
        holder.tvStatus.text = item.status

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
