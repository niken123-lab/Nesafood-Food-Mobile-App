package com.example.nesafood

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class Tambah_Menu_Penjual : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var edtNama: EditText
    private lateinit var edtHarga: EditText
    private lateinit var edtDeskripsi: EditText
    private lateinit var btnSimpan: Button
    private var bitmap: Bitmap? = null

    private val IMAGE_PICK_CODE = 100
    private val URL_ADD_MENU = "http://10.205.130.250/nesafood/tambah_menu.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_menu_penjual)

        imgPreview = findViewById(R.id.imgPreview)
        edtNama = findViewById(R.id.edtNamaMenu)
        edtHarga = findViewById(R.id.edtHargaMenu)
        edtDeskripsi = findViewById(R.id.edtDeskripsi)
        btnSimpan = findViewById(R.id.btnSimpanMenu)

        imgPreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnSimpan.setOnClickListener {
            val nama = edtNama.text.toString().trim()
            val harga = edtHarga.text.toString().trim()
            val deskripsi = edtDeskripsi.text.toString().trim()

            if (nama.isEmpty() || harga.isEmpty() || deskripsi.isEmpty() || bitmap == null) {
                Toast.makeText(this, "Semua field wajib diisi dan gambar harus dipilih", Toast.LENGTH_SHORT).show()
            } else {
                uploadMenu(nama, harga, deskripsi)
            }
        }
    }

    private fun uploadMenu(nama: String, harga: String, deskripsi: String) {
        val request = object : StringRequest(
            Request.Method.POST, URL_ADD_MENU,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getBoolean("success")) {
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal menambah menu: ${obj.getString("message")}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["nama_menu"] = edtNama.text.toString().trim()
                params["deskripsi"] = edtDeskripsi.text.toString().trim()
                params["harga"] = edtHarga.text.toString().trim()

                // 🔹 Ambil id_penjual dari SharedPreferences hasil login
                val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
                val idPenjual = sharedPref.getString("id_penjual", null)

                if (idPenjual != null && idPenjual.isNotEmpty()) {
                    params["id_penjual"] = idPenjual
                } else {
                    params["id_penjual"] = "0" // fallback aman
                }

                params["gambar"] = encodeImage(bitmap!!)
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun encodeImage(bmp: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            imgPreview.setImageBitmap(bitmap)
        }
    }
}
