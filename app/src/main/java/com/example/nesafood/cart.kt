package com.example.nesafood

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cart(
    val idMenu: String,
    val nama: String,
    val harga: Int,      // harga satuan dalam rupiah (tanpa titik)
    val qty: Int,
    val imageUrl: String? = null,
    val id_penjual: Int
) : Parcelable
