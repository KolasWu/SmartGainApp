package com.example.smartgain.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderRepository {
//Repository (搬運工)：負責跟 Firebase 講話。它不關心畫面要長怎樣，只負責提供一個 Query（查詢指令）給別人。

    //檢查是否有連線實例，沒有就建立
    private val db = FirebaseFirestore.getInstance()

    // 取得所有訂單，並按時間排序
    fun getOrdersQuery(sellerId: String): Query = db
        .collection("orders")
        .whereEqualTo("seller_id", sellerId) // 只抓屬於我的單
        .orderBy("timestamp", Query.Direction.DESCENDING)


    //新增訂單
    fun addOrder(order: Order) {
        val docRef = if (order.orderId.isEmpty()) {
            db.collection("orders").document() // 自動生成訂單 ID
        } else {
            db.collection("orders").document(order.orderId)
        }

        val finalOrder = order.copy(orderId = docRef.id)
        docRef.set(finalOrder)
    }

    fun deleteOrder(orderId: String) {
        if (orderId.isEmpty()) return
        db
            .collection("orders")
            .document(orderId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Order deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting order", e)
            }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        if (orderId.isEmpty()) return
        db.collection("orders")
            .document(orderId)
            .update("status", newStatus.name) // 只更新 status 欄位
    }

    // 原本的 deleteOrder 可以保留，但邏輯改為將狀態設為 DELETED
    fun markOrderAsDeleted(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.DELETED)
    }

    fun getNewOrdersQuery(sellerId: String) = db
        .collection("orders")
        .whereEqualTo("seller_id", sellerId)
        .whereEqualTo("status", "NEW")
        .orderBy("timestamp", Query.Direction.DESCENDING)
}