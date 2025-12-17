package com.example.nesafood

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DeepLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val u: Uri? = intent?.data
        val parentId = u?.getQueryParameter("parent_id")?.toIntOrNull() ?: 0
        val status   = (u?.getQueryParameter("status") ?: "PENDING").uppercase()

        // opsional: simpan last_parent_id utk safety
        getSharedPreferences("UserData", MODE_PRIVATE).edit()
            .putInt("last_parent_id", parentId).apply()

        // langsung arahkan ke StatusActivity
        val i = Intent(this, StatusActivity::class.java).apply {
            putExtra("parent_id", parentId)
            putExtra("status", status) // "PAID", "PENDING", "CANCELLED"
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(i)
        finish()
    }
}
