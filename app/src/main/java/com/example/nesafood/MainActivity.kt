package com.example.nesafood

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Gunakan ID tombol dari XML kamu
        val btnPembeli = findViewById<Button>(R.id.btn_1)
        val btnPenjual = findViewById<Button>(R.id.btn_2)

        // Tombol Pembeli
        btnPembeli.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("role", "pembeli")
            startActivity(intent)
        }

        // Tombol Penjual
        btnPenjual.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("role", "penjual")
            startActivity(intent)
        }
    }
}
