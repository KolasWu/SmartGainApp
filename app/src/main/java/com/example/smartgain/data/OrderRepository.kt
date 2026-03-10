package com.example.smartgain.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderRepository {
//Repository (搬運工)：負責跟 Firebase 講話。它不關心畫面要長怎樣，只負責提供一個 Query（查詢指令）給別人。

    //檢查是否有連線實例，沒有就建立
    private val db = FirebaseFirestore.getInstance()

    // 取得所有訂單，並按時間排序
    fun getOrdersQuery() = db
        .collection("orders")
        .orderBy("timestamp", Query.Direction.DESCENDING)

    // 取得商品，並按名稱排序
    fun getProductsQuery() = db
        .collection("products")
        .orderBy("name", Query.Direction.ASCENDING) // 按名稱 A-Z 排序

    // 新增產品 upsert = update or insert
    fun addProduct(product: Product) {
        // 如果 productId 為空，讓 Firestore 自動生成 ID
        val docRef = if (product.productId.isEmpty()) { db
            .collection("products")
            .document()
        } else { db
            .collection("products")
            .document(product.productId)
        }

        // 更新物件內的 ID 並寫入
        val finalProduct = product.copy(productId = docRef.id)
        docRef.set(finalProduct)
    }

    fun deleteProduct(productId: String) { db
        .collection("products")
        .document(productId)
        .delete()
    }
}