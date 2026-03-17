package com.example.smartgain.data

import com.google.firebase.firestore.PropertyName

data class Order(
    @get:PropertyName("order_id") @set:PropertyName("order_id")
    var orderId: String = "",

    @get:PropertyName("seller_id") @set:PropertyName("seller_id")
    var sellerId: String = "",

    @get:PropertyName("buyer_name") @set:PropertyName("buyer_name")
    var buyerName: String = "",

    @get:PropertyName("total_price") @set:PropertyName("total_price")
    var totalPrice: Int = 0,

    @get:PropertyName("contact") @set:PropertyName("contact")
    var contact: String = "",

    @set:PropertyName("status") @get:PropertyName("status")
    var status: String = "NEW",

    @set:PropertyName("timestamp") @get:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis(),

    @set:PropertyName("items") @get:PropertyName("items")
    var items: List<CartItem> = emptyList()
)