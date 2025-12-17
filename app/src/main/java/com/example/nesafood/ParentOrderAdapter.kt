package com.example.nesafood

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.animation.ObjectAnimator

class ParentOrderAdapter(
    private val context: Context,
    private val listOrders: List<ParentOrder>
) : RecyclerView.Adapter<ParentOrderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNamaToko: TextView = itemView.findViewById(R.id.txtNamaToko)
        val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        val txtTanggal: TextView = itemView.findViewById(R.id.txtTanggal)
        val btnLihatStatus: Button = itemView.findViewById(R.id.btnLihatStatus)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parent_order, parent, false)
        return ViewHolder(v)
    }

    private fun normalizeStatus(s: String): String {
        return s.uppercase().replace('_', ' ').trim()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listOrders[position]

        holder.txtNamaToko.text = item.namaToko
        holder.txtTotal.text = "Total: ${item.total.toRupiah()}"
        holder.txtTanggal.text = item.tanggal

        holder.btnLihatStatus.setOnClickListener {
            val intent = Intent(context, StatusActivity::class.java)
            intent.putExtra("parent_id", item.id)
            intent.putExtra("status", item.status)
            context.startActivity(intent)
        }
        val color = when (normalizeStatus(item.status)) {
            "PENDING", "PAID", "DITERIMA" -> context.getColor(R.color.status_active_green)
            "PROSES", "DIPROSES" -> context.getColor(R.color.status_active_orange)
            "SIAP AMBIL" -> context.getColor(R.color.status_active_blue)
            else -> context.getColor(R.color.white)
        }

        val animator = ObjectAnimator.ofArgb(holder.cardView, "cardBackgroundColor", color)
        animator.duration = 300
        animator.start()


        when (normalizeStatus(item.status)) {
            "PENDING", "PAID", "DITERIMA" -> holder.cardView.setCardBackgroundColor(
                context.getColor(R.color.status_active_green)
            )
            "PROSES", "DIPROSES" -> holder.cardView.setCardBackgroundColor(
                context.getColor(R.color.status_active_orange)
            )
            "SIAP AMBIL" -> holder.cardView.setCardBackgroundColor(
                context.getColor(R.color.status_active_blue)
            )
            "SELESAI", "DONE" -> holder.cardView.setCardBackgroundColor(
                context.getColor(R.color.background)
            )
            else -> holder.cardView.setCardBackgroundColor(context.getColor(R.color.white))
        }

    }


    override fun getItemCount(): Int = listOrders.size
}
