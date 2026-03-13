package com.example.smartgain.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch

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

    /**
     * 使用 Firestore Batch 處理下單邏輯
     * 確保：建立訂單 + 扣除所有相關庫存 是同一個原子操作
     */
    fun executeOrderBatch(order: Order, cartList: List<CartItem>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        // 1. 處理訂單寫入
        val orderRef = if (order.orderId.isEmpty()) {
            db.collection("orders").document()
        } else {
            db.collection("orders").document(order.orderId)
        }
        val finalOrder = order.copy(orderId = orderRef.id)
        batch.set(orderRef, finalOrder)

        // 2. 循環處理庫存扣除
        cartList.forEach { item ->
            val productRef = db.collection("products").document(item.productId)
            // 使用 increment(-item.quantity) 是最安全的方法，能避開「17個」那種覆蓋錯誤
            // 它會直接在資料庫現有的數值上做減法，不需要先讀取目前的數值
            batch.update(productRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong()))
        }

        // 3. 提交 Batch
        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}