package com.example.nesafood

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.bumptech.glide.Glide
import androidx.core.content.ContextCompat


class MenuEditAdapter(
    private val listMenu: MutableList<MenuModel>,
    private val context: Context
) : RecyclerView.Adapter<MenuEditAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaMenu)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHargaMenu)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        val imgMenu: ImageView = itemView.findViewById(R.id.imgMenuPenjual)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_menu_edit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listMenu[position]
        holder.tvNama.text = item.nama
        holder.tvHarga.text = "Rp ${item.harga}"

        // ✅ Load gambar dari URL pakai Glide
        if (item.gambar.isNotEmpty()) {
            com.bumptech.glide.Glide.with(context)
                .load(item.gambar)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgMenu!!)
        } else {
            holder.imgMenu?.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // ✏️ Tombol Edit → buka halaman edit menu
        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, UbahMenuActivity::class.java)
            intent.putExtra("id_menu", item.id)
            intent.putExtra("nama_menu", item.nama)
            intent.putExtra("harga", item.harga)
            intent.putExtra("deskripsi", item.deskripsi)
            intent.putExtra("gambar", item.gambar) // kirim URL, bukan base64
            context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Hapus Menu")
            builder.setMessage("Apakah Anda yakin ingin menghapus menu \"${item.nama}\"?")
            builder.setPositiveButton("Hapus") { _, _ ->
                hapusMenu(item.id, position)
            }
            builder.setNegativeButton("Batal", null)

            val dialog = builder.create()
            dialog.show()

            // 🖤 Ubah warna teks di tombol
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(context, android.R.color.black)
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(context, android.R.color.black)
            )

            // 🖤 Ubah warna teks judul & pesan jadi hitam
            val textViewTitle = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            val textViewMessage = dialog.findViewById<TextView>(android.R.id.message)
            textViewTitle?.setTextColor(ContextCompat.getColor(context, android.R.color.black))
            textViewMessage?.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

    }
        override fun getItemCount(): Int = listMenu.size

    // 🔹 Fungsi hapus menu dari database dan list adapter
    private fun hapusMenu(idMenu: Int, position: Int) {
        val url = "http://10.205.130.250/nesafood/delete_menu.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    if (obj.getBoolean("success")) {
                        listMenu.removeAt(position)
                        notifyItemRemoved(position)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Gagal koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_menu"] = idMenu.toString()
                return params
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
}
