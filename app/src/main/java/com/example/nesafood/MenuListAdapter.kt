package com.example.nesafood.com.example.nesafood

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nesafood.R


// Adapter untuk menampilkan daftar menu di RecyclerView
class MenuListAdapter(
    private val context: Context,
    private val menuList: List<Menu>
) : RecyclerView.Adapter<MenuListAdapter.MenuViewHolder>() {

    // ViewHolder = 1 item layout (item_menu.xml)
    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMenu: ImageView = view.findViewById(R.id.imgMenu)
        val tvNama: TextView = view.findViewById(R.id.tvNamaMenu)
        val tvHarga: TextView = view.findViewById(R.id.tvHargaMenu)
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]

        holder.tvNama.text = menu.nama_menu
        holder.tvHarga.text = "Rp${menu.harga}"

        // Load gambar pakai Glide
        Glide.with(context)
            .load("http://10.205.130.250/nesafood/uploads/${menu.gambar}") // kalau HP fisik, ganti IP laptop
            .into(holder.imgMenu)

        // Logic tombol + dan -
        var qty = 0
        holder.tvQty.text = qty.toString()

        holder.btnPlus.setOnClickListener {
            qty++
            holder.tvQty.text = qty.toString()
        }

        holder.btnMinus.setOnClickListener {
            if (qty > 0) qty--
            holder.tvQty.text = qty.toString()
        }
    }

    override fun getItemCount(): Int = menuList.size
}
