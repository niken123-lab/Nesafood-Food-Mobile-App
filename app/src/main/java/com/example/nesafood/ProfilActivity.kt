package com.example.nesafood

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.io.InputStream

class ProfilActivity : AppCompatActivity() {

    private val BASE_URL = "http://10.205.130.250/nesafood/profile.php"
    private val UPLOAD_URL = "http://10.205.130.250/nesafood/upload_photo.php"
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = prefs.getString("username", "")
        val role = prefs.getString("role", "pembeli")

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtUserOrStore = findViewById<EditText>(R.id.edtUserOrStore)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnChangePhoto = findViewById<Button>(R.id.btnChangePhoto)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()}
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        // Nonaktifkan edit manual
        edtEmail.isEnabled = false
        edtUserOrStore.isEnabled = false
        btnSave.text = "UBAH PROFIL"

        // 🔹 Load profil (server atau cache)
        loadProfile(username!!, role!!, edtEmail, edtUserOrStore, imgProfile)

        // 🔹 Tombol ubah username
        btnSave.setOnClickListener {
            showEditDialog(username, role, edtEmail, edtUserOrStore, imgProfile)
        }

        // 🔹 Tombol ubah foto
        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // 🔹 Tombol Logout
        btnLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi Logout")
            builder.setMessage("Apakah Anda yakin ingin keluar dari akun?")
            builder.setPositiveButton("Ya") { _, _ ->
                val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
                val foto = prefs.getString("foto_profil", "") // simpan dulu foto lama

                val editor = prefs.edit()
                editor.clear() // hapus semua data login termasuk isLoggedIn
                if (!foto.isNullOrEmpty()) {
                    editor.putString("foto_profil", foto) // simpan kembali foto
                }
                editor.putBoolean("isLoggedIn", false) // pastikan status login = false
                editor.apply()


                // 🔹 Kembali ke MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Batal", null)
            val alert = builder.create()
            alert.show()

            val messageView = alert.findViewById<TextView>(android.R.id.message)
            messageView?.setTextColor(resources.getColor(android.R.color.black))
        }
    }

    // === Pilih foto dari galeri ===
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
            val username = prefs.getString("username", "")
            val role = prefs.getString("role", "pembeli")
            uploadPhoto(username!!, role!!)
        }
    }

    // === Upload foto ke server dan update profil ===
    private fun uploadPhoto(username: String, role: String) {
        val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
        val bytes = inputStream!!.readBytes()
        inputStream.close()

        val request = object : VolleyMultipartRequest(
            Request.Method.POST, UPLOAD_URL,
            { response ->
                val json = JSONObject(String(response.data))
                if (json.getBoolean("success")) {
                    val fotoUrl = json.getString("file_url")
                    Toast.makeText(this, "Foto berhasil diupdate!", Toast.LENGTH_SHORT).show()

                    val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                    val editor = getSharedPreferences("UserData", MODE_PRIVATE).edit()
                    editor.putString("foto_profil", fotoUrl)
                    editor.apply()

                    Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_default_user)
                        .error(R.drawable.ic_default_user)
                        .into(imgProfile)
                } else {
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Gagal upload: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getByteData(): MutableMap<String, DataPart> {
                val params = HashMap<String, DataPart>()
                params["photo"] = DataPart("profile_${System.currentTimeMillis()}.jpg", bytes)
                return params
            }

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["role"] = role
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    // === Load profil dari server (atau cache lokal lebih dulu) ===
    private fun loadProfile(
        username: String,
        role: String,
        edtEmail: EditText,
        edtUserOrStore: EditText,
        imgProfile: ImageView
    ) {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val localFoto = prefs.getString("foto_profil", "")

        // Tampilkan foto dari cache jika ada
        if (!localFoto.isNullOrEmpty()) {
            Glide.with(this)
                .load(localFoto)
                .placeholder(R.drawable.ic_default_user)
                .error(R.drawable.ic_default_user)
                .into(imgProfile)
        } else {
            Glide.with(this)
                .load(R.drawable.ic_default_user)
                .into(imgProfile)
        }

        // Ambil data dari server
        val url = "$BASE_URL?username=$username&role=$role"
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                if (response.getBoolean("success")) {
                    val data = response.getJSONObject("data")
                    edtUserOrStore.setText(data.getString("username"))
                    edtEmail.setText(data.getString("email"))
                    val fotoUrl = data.optString("foto_profil", "")

                    val editor = prefs.edit()
                    editor.putString("email", data.getString("email"))

                    if (fotoUrl.isNotEmpty()) {
                        val fullUrl = "http://10.205.130.250/nesafood/$fotoUrl"
                        editor.putString("foto_profil", fullUrl)
                        editor.apply()

                        Glide.with(this)
                            .load(fullUrl)
                            .placeholder(R.drawable.ic_default_user)
                            .error(R.drawable.ic_default_user)
                            .into(imgProfile)
                    } else {
                        Glide.with(this)
                            .load(R.drawable.ic_default_user)
                            .into(imgProfile)
                    }
                }
            },
            { error ->
                Toast.makeText(this, "Gagal load: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        Volley.newRequestQueue(this).add(request)
    }

    // === Dialog ubah username ===
    private fun showEditDialog(
        username: String,
        role: String,
        edtEmail: EditText,
        edtUserOrStore: EditText,
        imgProfile: ImageView
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val edtEmailDialog = dialogView.findViewById<EditText>(R.id.dialogEmail)
        val edtPasswordDialog = dialogView.findViewById<EditText>(R.id.dialogPassword)
        val edtNewUsername = dialogView.findViewById<EditText>(R.id.dialogUsername)

        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        edtEmailDialog.setText(prefs.getString("email", ""))

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ubah Username")
            .setView(dialogView)
            .setPositiveButton("SIMPAN") { _, _ ->
                val email = edtEmailDialog.text.toString().trim()
                val password = edtPasswordDialog.text.toString().trim()
                val newUsername = edtNewUsername.text.toString().trim()

                if (email.isEmpty() || password.isEmpty() || newUsername.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                } else {
                    updateUsername(email, password, newUsername, role, edtEmail, edtUserOrStore, imgProfile)
                }
            }
            .setNegativeButton("BATAL", null)
            .create()

        dialog.show()
    }
    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val edtOld = view.findViewById<EditText>(R.id.edtOldPassword)
        val edtNew = view.findViewById<EditText>(R.id.edtNewPassword)
        val edtConfirm = view.findViewById<EditText>(R.id.edtConfirmPassword)

        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val email = prefs.getString("email", "")
        val role = prefs.getString("role", "pembeli")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Ubah Password")
            .setView(view)
            .setPositiveButton("SIMPAN") { _, _ ->
                val oldPass = edtOld.text.toString().trim()
                val newPass = edtNew.text.toString().trim()
                val confirm = edtConfirm.text.toString().trim()

                if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                } else if (newPass != confirm) {
                    Toast.makeText(this, "Konfirmasi password tidak sama!", Toast.LENGTH_SHORT).show()
                } else {
                    ubahPassword(email!!, oldPass, newPass, role!!)
                }
            }
            .setNegativeButton("BATAL", null)
            .create()

        dialog.show()
    }

    private fun ubahPassword(email: String, oldPass: String, newPass: String, role: String) {
        val url = "http://10.205.130.250/nesafood/change_password.php"
        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                val obj = JSONObject(response)
                Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["old_password"] = oldPass
                params["new_password"] = newPass
                params["role"] = role
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    // === Update username ke server ===
    private fun updateUsername(
        email: String,
        password: String,
        newUsername: String,
        role: String,
        edtEmail: EditText,
        edtUserOrStore: EditText,
        imgProfile: ImageView
    ) {
        val request = object : StringRequest(
            Request.Method.POST, BASE_URL,
            { response ->
                val obj = JSONObject(response)
                if (obj.getBoolean("success")) {
                    val newName = obj.getString("new_username")
                    Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()

                    val editor = getSharedPreferences("UserData", MODE_PRIVATE).edit()
                    editor.putString("username", newName)
                    editor.apply()

                    // Refresh tampilan
                    loadProfile(newName, role, edtEmail, edtUserOrStore, imgProfile)
                } else {
                    Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["password"] = password
                params["new_username"] = newUsername
                params["role"] = role
                return params
            }
        }
        Volley.newRequestQueue(this).add(request)
    }
}
