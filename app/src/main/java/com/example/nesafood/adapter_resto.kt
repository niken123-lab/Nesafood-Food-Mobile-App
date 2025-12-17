package com.example.nesafood

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RestoranAdapter(private val fullList: MutableList<Restoran>) :
    RecyclerView.Adapter<RestoranAdapter.RestoranViewHolder>() {

    private val list: MutableList<Restoran> = mutableListOf()

    init {
        list.addAll(fullList)
    }

    inner class RestoranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRestoran: ImageView = itemView.findViewById(R.id.img_restoran)
        val tvNama: TextView = itemView.findViewById(R.id.tv_nama)
        val tvDeskripsi: TextView = itemView.findViewById(R.id.tv_deskripsi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestoranViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_toko, parent, false)
        return RestoranViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestoranViewHolder, position: Int) {
        val item = list[position]

        Glide.with(holder.itemView.context)
            .load(item.gambar)
            .placeholder(R.drawable.kantin1)
            .error(R.drawable.kantin1)
            .into(holder.imgRestoran)

        holder.tvNama.text = item.nama
        holder.tvDeskripsi.text = item.deskripsi

        // ✅ Tambahan logika toko tutup
        if (item.status_toko == "tutup") {
            // Buat tampilan abu-abu dan nonaktif
            holder.itemView.alpha = 0.5f
            holder.itemView.isEnabled = false
            holder.itemView.setOnClickListener(null)

            // Tambahkan tulisan “Toko Tutup” biar pembeli tahu
            holder.tvDeskripsi.text = "Toko Tutup"
        } else {
            // Toko buka → normal dan bisa diklik
            holder.itemView.alpha = 1f
            holder.itemView.isEnabled = true

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, MenuDalam::class.java)
                intent.putExtra("id_penjual", item.id_penjual)
                intent.putExtra("nama_toko", item.nama)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    // 🔍 Fungsi pencarian
    fun filter(query: String) {
        list.clear()
        if (query.isEmpty()) {
            list.addAll(fullList)
        } else {
            val lowerQuery = query.lowercase()
            val filtered = fullList.filter {
                it.nama.lowercase().contains(lowerQuery) ||
                        it.deskripsi.lowercase().contains(lowerQuery)
            }
            list.addAll(filtered)
        }
        notifyDataSetChanged()
    }

    // 🔄 Update data dari luar
    fun updateData(newList: List<Restoran>) {
        fullList.clear()
        fullList.addAll(newList)
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
