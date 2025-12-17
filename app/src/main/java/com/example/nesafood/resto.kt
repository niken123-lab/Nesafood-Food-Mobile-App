package com.example.nesafood

data class Restoran(
    val nama: String,
    val deskripsi: String,
    val gambar: String,
    val id_penjual: String? = null,
    val status_toko: String = "buka" // 🔹 default agar kompatibel
    )


