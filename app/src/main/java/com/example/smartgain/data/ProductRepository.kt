package com.example.smartgain.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    // 更新商品（用於扣庫存）
    fun updateProduct(product: Product) {
        productsCollection.document(product.productId).set(product)
    }

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

    fun updateStock(productId: String, newStock: Int) {
        db.collection("products").document(productId)
            .update("stock", newStock) // 僅更新庫存欄位
    }
}