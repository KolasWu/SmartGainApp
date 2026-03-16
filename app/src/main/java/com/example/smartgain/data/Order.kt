package com.example.smartgain.data

import com.google.firebase.firestore.PropertyName

data class Order(
    @get:PropertyName("order_id") @set:PropertyName("order_id")
    var orderId: String = "",        // 訂單編號

    @get:PropertyName("seller_id") @set:PropertyName("seller_id")
    var sellerId: String = "",        // 賣家 ID

    @get:PropertyName("buyer_name") @set:PropertyName("buyer_name")
    var buyerName: String = "",      // 買家名稱

    @get:PropertyName("total_price") @set:PropertyName("total_price")
    var totalPrice: Int = 0,         // 價格 (用於計算營收)

    val contact: String = "",
    var status: String = "NEW",      // 狀態：NEW (待確認), DONE (已完成)
    var timestamp: Long = System.currentTimeMillis(), // 下單時間
    var items: List<CartItem> = emptyList() //存入下單時的明細
)