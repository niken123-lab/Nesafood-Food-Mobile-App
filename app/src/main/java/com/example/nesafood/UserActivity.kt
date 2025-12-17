package com.example.nesafood

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class UserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // 🔹 Tombol untuk pembeli dan penjual
        val btnPembeli = findViewById<Button>(R.id.btn_1)
        val btnPenjual = findViewById<Button>(R.id.btn_2)

        // 🟢 Klik pembeli → buka halaman pilihan (login / daftar) untuk pembeli
        btnPembeli.setOnClickListener {
            showRoleOptions("pembeli")
        }

        // 🟠 Klik penjual → buka halaman pilihan (login / daftar) untuk penjual
        btnPenjual.setOnClickListener {
            showRoleOptions("penjual")
        }
    }

    // Fungsi untuk pilih login / daftar sesuai role
    private fun showRoleOptions(role: String) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("role", role)
        startActivity(intent)
    }
}
