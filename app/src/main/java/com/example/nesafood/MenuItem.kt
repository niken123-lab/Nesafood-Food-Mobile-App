package com.example.nesafood

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItem(
    val idMenu: String,
    val id_penjual: Int,
    val nama: String,
    val harga: Double,
    val gambar: String,
    var qty: Int = 0

) : Parcelable
