package com.example.smartgain.data

import com.google.firebase.firestore.PropertyName

data class Product(
    @get:PropertyName("product_id") @set:PropertyName("product_id")
    var productId: String = "",        // 商品編號

    var name: String = "",             // 商品名稱

    var price: Int = 0,                // 單價

    var stock: Int = 0,                // 庫存數量

    @get:PropertyName("image_url") @set:PropertyName("image_url")
    @field:PropertyName("image_url")
    var imageUrl: String = "",         // 商品圖片路徑

    @get:PropertyName("seller_id") @set:PropertyName("seller_id")
    var sellerId: String = "", // 建議變數名改為 sellerId 但對應名稱為 seller_id

    var description : String = ""      //商品描述
){
    // 必須要有一個無參數建構子，Firestore 才能轉換
    constructor() : this("", "", 0, 0, "", "", "")
}