package com.example.smartgain.data

data class CartItem(
    val productId: String,
    val name: String,
    var quantity: Int,    // 使用 var 因為數量可能會累加
    val price: Int,       // 單價
    val stock: Int        // 記錄原始庫存，方便在對話框內做即時檢查
) {
    // 方便計算這項商品的總價
    val subtotal: Int
        get() = price * quantity
}