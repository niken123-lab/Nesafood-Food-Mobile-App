package com.example.nesafood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "order_updates"
        const val CHANNEL_NAME = "Update Pesanan"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // --- Ambil payload ---
        val title = message.notification?.title ?: "NesaFood"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Status pesanan diperbarui."

        // Bisa kirim order_id/status di data payload
        val orderId = message.data["order_id"]
        val status  = message.data["status"]

        // --- Tujuan ketika notifikasi diklik (contoh ke StatusPemesananActivity) ---
        val intent = Intent(this, StatusPemesananActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (orderId != null) putExtra("order_id", orderId)
            if (status != null) putExtra("status", status)
        }
        val pi = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // --- Bangun notifikasi ---
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            // pakai ikon kecil yang ada; kalau belum punya, sementara pakai launcher
            .setSmallIcon(R.drawable.ic_stat_nesa)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .build()

        // --- Cek izin Android 13+ ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // user belum memberi izin — jangan crash, cukup batal kirim
                return
            }
        }

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNewToken(token: String) {
        // Simpan token ke server kamu
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)
        if (userId == 0) return

        val url = "http://10.205.130.250/nesafood/update_fcm_token.php"
        val req = object : StringRequest(Method.POST, url,
            { /* success */ },
            { /* ignore / log */ }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("user_id" to userId.toString(), "token" to token)
        }
        Volley.newRequestQueue(applicationContext).add(req)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi perubahan status pesanan"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(chan)
        }
    }
}
