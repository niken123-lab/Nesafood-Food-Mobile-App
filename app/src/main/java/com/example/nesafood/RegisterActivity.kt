package com.example.nesafood

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.content.Intent
import android.view.View

class RegisterActivity : AppCompatActivity() {

    private val URL_REGISTER = "http://10.205.130.250/nesafood/register.php"
    private var userRole: String = "pembeli" // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register2)

        // 🔹 Ambil role dari halaman sebelumnya (UserActivity)
        userRole = intent.getStringExtra("role") ?: "pembeli"

        val edtEmail = findViewById<EditText>(R.id.edt_email)
        val edtUsername = findViewById<EditText>(R.id.edt_usn)
        val edtPhone = findViewById<EditText>(R.id.edt_phone)
        val edtPass = findViewById<EditText>(R.id.edt_pass)
        val btnRegister = findViewById<Button>(R.id.r_btn_1)

        btnRegister.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val username = edtUsername.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val password = edtPass.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, username, phone, password)
            }
        }
    }
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    private fun registerUser(email: String, username: String, phone: String, password: String) {
        val stringRequest = object : StringRequest(
            Request.Method.POST, URL_REGISTER,
            { response ->
                try {
                    val obj = JSONObject(response)

                    if (obj.getBoolean("success")) {
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                        finish() // balik ke LoginActivity
                    } else {
                        Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Koneksi gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["username"] = username
                params["no_telp"] = phone
                params["password"] = password
                params["role"] = userRole  //
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}
