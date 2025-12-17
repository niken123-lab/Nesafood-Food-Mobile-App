package com.example.nesafood

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest

class PaymentActivity : AppCompatActivity() {

    private var parentId: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val ivQR = findViewById<ImageView>(R.id.ivQR)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)

        parentId = intent.getIntExtra("PARENT_ID", 0)
        val total = intent.getIntExtra("TOTAL", 0)
        val qrBase64 = intent.getStringExtra("QR_BASE64") ?: ""

        tvTotal.text = getString(R.string.total_fmt, total.toRupiah())
        ivQR.setImageBitmap(base64ToBitmap(qrBase64))
        android.util.Log.d("PAY", "orderCode=${intent.getStringExtra("ORDER_CODE")}")
        android.util.Log.d("PAY", "parentId=${intent.getIntExtra("PARENT_ID",0)}")

        startPolling()
    }

    private fun base64ToBitmap(b64: String): android.graphics.Bitmap {
        val bytes = Base64.decode(b64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun startPolling() {
        handler.post(object : Runnable {
            override fun run() {
                val url = "http://10.205.130.250/nesafood/order_status.php?parent_id=$parentId"
                val req = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { resp ->
                        val parent = resp.optJSONObject("parent")
                        val status = parent?.optString("status") ?: ""
                        if (status == "PAID") {
                            Toast.makeText(this@PaymentActivity, "Pembayaran terverifikasi", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@PaymentActivity, StatusActivity::class.java)
                                .putExtra("status", "diterima"))
                            finish()
                        } else {
                            handler.postDelayed(this, 5000)
                        }
                    },
                    { _ -> handler.postDelayed(this, 5000) }
                )
                com.android.volley.toolbox.Volley.newRequestQueue(this@PaymentActivity).add(req)
            }
        })
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
