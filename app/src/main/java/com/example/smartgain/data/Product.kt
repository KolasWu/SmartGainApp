package com.example.smartgain.data

import com.google.firebase.firestore.PropertyName

data class Product(
    @get:PropertyName("product_id") @set:PropertyName("product_id")
    var productId: String = "",       // 商品編號

    var name: String = "",            // 商品名稱

    var price: Int = 0,               // 單價

    var stock: Int = 0,               // 庫存數量

    @get:PropertyName("image_url") @set:PropertyName("image_url")
    var imageUrl: String = ""         // 商品圖片路徑
)