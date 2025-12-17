package com.example.nesafood

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest   // ⭐ tambahan
import org.json.JSONObject
import java.net.URLEncoder
// ⭐ tambahan

class LoginActivity : AppCompatActivity() {

    private val urlLogin  = "http://10.205.130.250/nesafood/login.php"
    private val urlForgot = "http://10.205.130.250/nesafood/forgot_password.php"
    private val urlGetUserId = "http://10.205.130.250/nesafood/get_user_id.php?username=" // ⭐ tambahan
    private var userRole: String = "pembeli"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userRole = intent.getStringExtra("role") ?: "pembeli"

        val edtUsername = findViewById<EditText>(R.id.l_edt_usn)
        val edtPassword = findViewById<EditText>(R.id.l_edt_pass)
        val btnLogin = findViewById<Button>(R.id.l_btn_1)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // 🔹 Dialog Lupa Password
        tvForgotPassword.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
            builder.setView(view)
            val dialog = builder.create()

            val edtEmail = view.findViewById<EditText>(R.id.edtForgotEmail)
            val edtNoTelp = view.findViewById<EditText>(R.id.edtForgotNoTelp)
            val btnKirim = view.findViewById<Button>(R.id.btnKirim)
            val btnBatal = view.findViewById<Button>(R.id.btnBatal)

            btnKirim.setOnClickListener {
                val email = edtEmail.text.toString().trim()
                val noTelp = edtNoTelp.text.toString().trim()

                if (email.isEmpty() || noTelp.isEmpty()) {
                    Toast.makeText(this, "Email dan nomor telepon wajib diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    kirimResetPassword(email, noTelp)
                    dialog.dismiss()
                }
            }

            btnBatal.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        // 👁️ Toggle Show/Hide Password
        var isPasswordVisible = false
        edtPassword.setOnTouchListener { v, event ->
            v.performClick()
            val drawableEnd = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (edtPassword.right -
                            edtPassword.compoundDrawables[drawableEnd].bounds.width() -
                            edtPassword.paddingRight)
                ) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        edtPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0)
                    } else {
                        edtPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
                    }
                    edtPassword.setSelection(edtPassword.text.length)
                    true
                } else false
            } else false
        }

        // 🔹 Tombol Login
        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password wajib diisi!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }

        // 🔹 Tombol "Sign Up"
        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("role", userRole)
            startActivity(intent)
        }
    }

    // 🔐 Fungsi login
    private fun loginUser(username: String, password: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST, urlLogin,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getBoolean("success")) {
                        val message   = obj.getString("message")
                        val role      = obj.getString("role")
                        val user      = obj.getString("username")
                        val email     = obj.optString("email", "")
                        val idPenjual = obj.optString("id_penjual", "")
                        val fotoUrl   = obj.optString("foto", "")

                        // ⭐ kalau API login sudah kirim id_username, simpan langsung
                        val idUsername = obj.optInt("id_username", 0)

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                        // ✅ simpan semua data penting + status login
                        getSharedPreferences("UserData", MODE_PRIVATE).edit().apply {
                            putString("username", user)
                            putString("email", email)
                            putString("role", role)
                            putString("id_penjual", idPenjual)
                            putString("foto", fotoUrl)
                            putBoolean("isLoggedIn", true)
                            if (idUsername > 0) putInt("user_id", idUsername)   // ⭐ penting
                            apply()
                        }

                        // ⭐ jika login.php belum mengembalikan id_username → fallback cari lewat endpoint kecil
                        if (idUsername == 0) {
                            fetchUserIdAndSave(user)
                        }

                        val intent = if (role == "penjual") {
                            Intent(this, PenjualActivity::class.java)
                        } else {
                            Intent(this, UtamaActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membaca data dari server: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["password"] = password
                params["role"] = userRole
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    // ✉️ Kirim permintaan reset password ke server
    private fun kirimResetPassword(email: String, noTelp: String) {
        val request = object : StringRequest(
            Request.Method.POST, urlForgot,
            { response ->
                try {
                    val obj = JSONObject(response)
                    Toast.makeText(this, obj.getString("message"), Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Kesalahan parsing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["no_telp"] = noTelp
                return params
            }
        }
        request.retryPolicy = com.android.volley.DefaultRetryPolicy(
            0, // timeout
            0, // max retry count
            1f
        )
        Volley.newRequestQueue(this).add(request)
    }

    // ⭐ Fallback: ambil id_username berdasarkan username kalau login.php tidak mengirim
    private fun fetchUserIdAndSave(username: String) {
        val url = urlGetUserId + URLEncoder.encode(username, "UTF-8")
        val req = JsonObjectRequest(
            Request.Method.GET, url, null,
            { r ->
                val id = r.optInt("id_username", 0)
                if (id > 0) {
                    getSharedPreferences("UserData", MODE_PRIVATE)
                        .edit().putInt("user_id", id).apply()
                }
            },
            { /* diamkan saja; tidak mengganggu flow login */ }
        )
        Volley.newRequestQueue(this).add(req)
    }
}
