package com.example.nesafood

data class Pesanan(
    val id: String,
    val namaPembeli: String,
    val items: String,
    val total: Int,
    var status: String,
    val tanggal: String? = null
)

