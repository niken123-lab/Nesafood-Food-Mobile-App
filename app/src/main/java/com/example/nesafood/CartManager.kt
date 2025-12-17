package com.example.nesafood

object CartManager {
    val cartItems = mutableListOf<Cart>()

    fun addOrUpdateItem(item: Cart) {
        val existing = cartItems.find { it.idMenu == item.idMenu }
        if (existing != null) {
            val index = cartItems.indexOf(existing)
            cartItems[index] = item.copy() // update qty/harga terbaru
        } else {
            cartItems.add(item)
        }
    }

    fun removeItem(idMenu: String) {
        cartItems.removeAll { it.idMenu == idMenu }
    }

    fun clear() {
        cartItems.clear()
    }

    fun getAll(): List<Cart> = cartItems

    fun total(): Int {
        return cartItems.sumOf { it.harga * it.qty }
        fun removeItem(idMenu: String) {
            cartItems.removeAll { it.idMenu == idMenu }
        }
    }
}
