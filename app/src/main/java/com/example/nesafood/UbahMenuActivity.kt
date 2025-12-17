package com.example.nesafood

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class UbahMenuActivity : AppCompatActivity() {

    private val urlUpdate = "http://10.205.130.250/nesafood/update_menu.php"
    private lateinit var imgPreview: ImageView
    private lateinit var edtNama: EditText
    private lateinit var edtHarga: EditText
    private lateinit var edtDeskripsi: EditText
    private lateinit var btnSimpan: Button
    private var idMenu = 0
    private var gambarBase64 = ""

    private val PICK_IMAGE_REQUEST = 100 // request code untuk galeri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_menu_penjual)

        imgPreview = findViewById(R.id.imgPreview)
        edtNama = findViewById(R.id.edtNamaMenu)
        edtHarga = findViewById(R.id.edtHargaMenu)
        edtDeskripsi = findViewById(R.id.edtDeskripsi)
        btnSimpan = findViewById(R.id.btnSimpanMenu)

        // 🔹 Ambil data dari intent
        idMenu = intent.getIntExtra("id_menu", 0)
        val nama = intent.getStringExtra("nama_menu")
        val harga = intent.getIntExtra("harga", 0)
        val deskripsi = intent.getStringExtra("deskripsi")
        val gambar = intent.getStringExtra("gambar")

        edtNama.setText(nama)
        edtHarga.setText(harga.toString())
        edtDeskripsi.setText(deskripsi)

        // 🔹 Tampilkan gambar dari Base64 (jika ada)
        // 🔹 Tampilkan gambar dari server atau Base64
        // 🔹 Tampilkan gambar dari server atau Base64
        if (!gambar.isNullOrEmpty()) {
            // kalau yang dikirim server sudah URL lengkap (http...)
            if (gambar.startsWith("http")) {
                com.bumptech.glide.Glide.with(this)
                    .load(gambar)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgPreview)
            } else if (gambar.length > 100) {
                // kalau yang dikirim base64 (biasanya panjang banget)
                try {
                    val bytes = Base64.decode(gambar, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imgPreview.setImageBitmap(bitmap)
                    gambarBase64 = gambar
                } catch (e: Exception) {
                    imgPreview.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                // kalau kosong atau error
                imgPreview.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }


        // 🔹 Klik gambar untuk pilih gambar baru
        imgPreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // 🔹 Tombol simpan perubahan
        btnSimpan.setOnClickListener {
            val namaBaru = edtNama.text.toString().trim()
            val hargaBaru = edtHarga.text.toString().trim()
            val deskripsiBaru = edtDeskripsi.text.toString().trim()

            if (namaBaru.isEmpty() || hargaBaru.isEmpty()) {
                Toast.makeText(this, "Nama dan harga wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateMenu(idMenu, namaBaru, hargaBaru, deskripsiBaru)
        }
    }

    // 🔹 Ambil gambar dari galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            imgPreview.setImageBitmap(bitmap)

            // ubah ke Base64
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val imageBytes = baos.toByteArray()
            gambarBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }
    }

    // 🔹 Kirim data ke server
    private fun updateMenu(id: Int, nama: String, harga: String, deskripsi: String) {
        val request = object : StringRequest(
            Request.Method.POST, urlUpdate,
            { response ->
                try {
                    val obj = JSONObject(response)
                    Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    if (obj.getBoolean("success")) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal koneksi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_menu"] = id.toString()
                params["nama_menu"] = nama
                params["harga"] = harga
                params["deskripsi"] = deskripsi
                if (gambarBase64.isNotEmpty()) {
                    params["gambar"] = gambarBase64
                }
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}
