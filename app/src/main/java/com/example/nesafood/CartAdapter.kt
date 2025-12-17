package com.example.nesafood

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nesafood.databinding.ItemCartBinding

class CartAdapter(
    private val items: MutableList<Cart>,
    private val onDataChanged: (subtotal: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.VH>() {

    inner class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCartBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        with(holder.b) {
            tvCartNama.text = item.nama
            tvCartHarga.text = item.harga.toRupiah()
            tvCartQty.text = "x${item.qty}"
            tvCartTotalItem.text = (item.harga * item.qty).toRupiah()

            // ✅ Tombol hapus item
            btnHapus.setOnClickListener {
                // Hapus item dari list & CartManager
                CartManager.removeItem(item.idMenu)
                items.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)

                // Update subtotal
                notifySubtotal()
            }
        }
    }

    fun notifySubtotal() {
        val subtotal = items.sumOf { it.harga * it.qty }
        onDataChanged(subtotal)
    }
}