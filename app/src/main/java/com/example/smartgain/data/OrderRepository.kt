package com.example.smartgain.data

import android.util.Log
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
}