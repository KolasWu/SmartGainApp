package com.example.smartgain.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()

    // 取得所有訂單，並按時間排序
    fun getOrdersQuery() = db.collection("orders")
        .orderBy("timestamp", Query.Direction.DESCENDING)

    // --- 新增：商品查詢 ---
    fun getProductsQuery() = db.collection("products")
        .orderBy("name", Query.Direction.ASCENDING) // 按名稱 A-Z 排序

    // 新增產品
    fun addProduct(product: Product) {
        // 如果 productId 為空，讓 Firestore 自動生成 ID
        val docRef = if (product.productId.isEmpty()) {
            db.collection("products").document()
        } else {
            db.collection("products").document(product.productId)
        }

        // 更新物件內的 ID 並寫入
        val finalProduct = product.copy(productId = docRef.id)
        docRef.set(finalProduct)
    }

    fun deleteProduct(productId: String) {
        db.collection("products").document(productId).delete()
    }
}