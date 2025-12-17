package com.example.nesafood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MenuAdapter(private val list: MutableList<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMenu: ImageView = view.findViewById(R.id.imgMenu)
        val tvNama: TextView = view.findViewById(R.id.tvNamaMenu)
        val tvHarga: TextView = view.findViewById(R.id.tvHargaMenu)
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = list[position]

        holder.tvNama.text = item.nama
        holder.tvHarga.text = "Rp${item.harga.toInt()}"
        holder.tvQty.text = item.qty.toString()

        // ✅ Load gambar dari URL
        Glide.with(holder.itemView.context)
            .load(item.gambar)
            .placeholder(R.drawable.kantin1)
            .into(holder.imgMenu)

        // ✅ Tombol tambah
        holder.btnPlus.setOnClickListener {
            item.qty++
            holder.tvQty.text = item.qty.toString()

            // 🔹 Tambah atau update ke keranjang global
            CartManager.addOrUpdateItem(
                Cart(
                    idMenu = item.idMenu,
                    nama = item.nama,
                    harga = item.harga.toInt(),
                    qty = item.qty,
                    imageUrl = item.gambar,
                    id_penjual = item.id_penjual
                )
            )
        }

        // ✅ Tombol kurang
        holder.btnMinus.setOnClickListener {
            if (item.qty > 0) {
                item.qty--
                holder.tvQty.text = item.qty.toString()

                // 🔹 Jika qty 0 → hapus dari keranjang
                if (item.qty == 0) {
                    CartManager.removeItem(item.idMenu)
                } else {
                    CartManager.addOrUpdateItem(
                        Cart(
                            idMenu = item.idMenu,
                            nama = item.nama,
                            harga = item.harga.toInt(),
                            qty = item.qty,
                            imageUrl = item.gambar,
                            id_penjual = item.id_penjual
                        )
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
