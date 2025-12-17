package com.example.nesafood

import java.text.NumberFormat
import java.util.Locale
fun Int.toRupiah(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(this).replace(",00", "")
}
