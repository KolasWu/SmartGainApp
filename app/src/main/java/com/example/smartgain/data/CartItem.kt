package com.example.smartgain.data

data class CartItem(
    var productId: String = "",
    var name: String = "",
    var quantity: Int = 0,    // 使用 var 因為數量可能會累加
    var price: Int = 0,       // 單價
    var stock: Int = 0 // 記錄原始庫存，方便在對話框內做即時檢查
) {
    // 方便計算這項商品的總價
    @get:com.google.firebase.firestore.Exclude
    val subtotal: Int
        get() = price * quantity
}