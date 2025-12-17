package com.example.nesafood

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.util.Log


class StatusActivity : AppCompatActivity() {

    private lateinit var statusDiterima: LinearLayout
    private lateinit var statusDiproses: LinearLayout
    private lateinit var statusSiap: LinearLayout

    private val baseUrl = "http://10.205.130.250/nesafood"
    private var parentId: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private var isPolling = false
    private val pollRunnable = object : Runnable {
        override fun run() {
            if (isPolling && parentId > 0) pollOnce(parentId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.status_pembeli)

        statusDiterima = findViewById(R.id.statusDiterima)
        statusDiproses = findViewById(R.id.statusDiproses)
        statusSiap = findViewById(R.id.statusSiap)

        // 1️⃣ Ambil status dari Intent (misal dari halaman sebelumnya)
        intent.getStringExtra("status")?.let { applyStatusColor(it) }

        // 2️⃣ Deep-link dari Midtrans (misal dari pembayaran)
        val deep: Uri? = intent?.data
        val pidFromDeep = deep?.getQueryParameter("parent_id")?.toIntOrNull() ?: 0
        val stFromDeep = deep?.getQueryParameter("status")

        parentId = when {
            pidFromDeep > 0 -> pidFromDeep
            else -> getSharedPreferences("UserData", MODE_PRIVATE).getInt("last_parent_id", 0)
        }

        // Jika deep-link sudah bawa status final, tampilkan langsung
        if (!stFromDeep.isNullOrBlank()) {
            applyStatusFromServer(stFromDeep.uppercase())
            return
        }

        // Kalau belum final → mulai polling
        if (parentId > 0) startPolling()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    override fun onResume() {
        super.onResume()
        if (parentId > 0) startPolling()
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true
        handler.post(pollRunnable)
    }

    private fun stopPolling() {
        isPolling = false
        handler.removeCallbacks(pollRunnable)
    }

    private fun pollOnce(pid: Int) {
        val url = "$baseUrl/order_status.php?parent_id=$pid"
        val req = StringRequest(
            Request.Method.GET, url,
            { body ->
                try {
                    val obj = JSONObject(body)
                    val status = obj.optJSONObject("parent")
                        ?.optString("status", "PENDING")
                        ?.uppercase() ?: "PENDING"

                    applyStatusFromServer(status)
                } catch (e: Exception) {
                    scheduleNext()
                }
            },
            { scheduleNext() }
        )
        Volley.newRequestQueue(this).add(req)
    }

    private fun scheduleNext() {
        if (isPolling) handler.postDelayed(pollRunnable, 3000)
    }

    // ============================================================
    // 🔹 Sesuai dengan urutan status PHP
    // PAID/PENDING/DITERIMA → DIPROSES → SIAP_AMBIL → SELESAI
    // ============================================================
    private fun applyStatusFromServer(statusUpper: String) {
        when (statusUpper) {
            "PAID", "PENDING", "DITERIMA","pending","diterima"-> {
                applyStatusColor("diterima")
            }
            "DIPROSES" -> {
                applyStatusColor("diproses")
            }
            "SIAP_AMBIL" -> {
                applyStatusColor("siap")
            }
            "SELESAI", "DONE" -> {
                applyStatusColor("selesai")
                Toast.makeText(this, "Pesanan selesai 🎉", Toast.LENGTH_SHORT).show()
                stopPolling()
            }
             "EXPIRED", "DENY", "FAILURE" -> {
                statusDiterima.setBackgroundColor(Color.parseColor("#FFCDD2"))
                statusDiproses.setBackgroundColor(Color.parseColor("#FFCDD2"))
                statusSiap.setBackgroundColor(Color.parseColor("#FFCDD2"))
                Toast.makeText(this, "Pesanan dibatalkan / expired ❌", Toast.LENGTH_SHORT).show()
                stopPolling()
            }
            else -> {
                applyStatusColor("diterima")
                scheduleNext()
            }
        }
    }

    // ============================================================
    // 🔹 Pewarnaan status: sinkron dengan logika PHP baru
    // ============================================================
    private fun applyStatusColor(status: String) {
        val defaultColor = Color.parseColor("#E0E0E0") // abu-abu terang
        val activeGreen = Color.parseColor("#DCEDC8")  // hijau lembut
        val activeOrange = Color.parseColor("#FFECB3") // oranye lembut
        val activeBlue = Color.parseColor("#BBDEFB")   // biru lembut

        // reset semua ke default
        statusDiterima.setBackgroundColor(defaultColor)
         statusDiproses.setBackgroundColor(defaultColor)
        statusSiap.setBackgroundColor(defaultColor)

        // normalize spasi dan huruf besar
        val cleanStatus = status.lowercase().trim().replace(" ", "_")
        Log.d("StatusColorDebug", "Status diterima dari server: $status")

        when (cleanStatus) {
           "pending","diterima", "PAID", "PENDING", "DITERIMA"-> {
                statusDiterima.setBackgroundColor(activeGreen)
            }
            "diproses" -> {
                statusDiterima.setBackgroundColor(activeGreen)
                statusDiproses.setBackgroundColor(activeOrange)
            }
            "siap", "siap_ambil" -> {
                statusDiterima.setBackgroundColor(activeGreen)
                statusDiproses.setBackgroundColor(activeOrange)
                statusSiap.setBackgroundColor(activeBlue)
            }
            "selesai" -> {
                statusDiterima.setBackgroundColor(activeGreen)
                statusDiproses.setBackgroundColor(activeGreen)
                statusSiap.setBackgroundColor(activeGreen)
            }
        }
    }

}
