package com.example.nesafood

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nesafood.databinding.ActivitySplashBinding
import android.os.Build
import android.content.pm.PackageManager
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logo.alpha = 0f
        binding.logo.animate().alpha(1f).setDuration(1500).start()

        binding.root.postDelayed({
            val sharedPref = getSharedPreferences("UserData", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
            val role = sharedPref.getString("role", "")

            val intent = when {
                isLoggedIn && role == "penjual" -> Intent(this, PenjualActivity::class.java)
                isLoggedIn && role == "pembeli" -> Intent(this, UtamaActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}
