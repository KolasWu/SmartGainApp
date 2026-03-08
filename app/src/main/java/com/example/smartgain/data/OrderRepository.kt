package com.example.smartgain.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderRepository {
    private val db = FirebaseFirestore.getInstance()

    // 取得所有訂單，並按時間排序
    fun getOrdersQuery() = db.collection("orders")
        .orderBy("timestamp", Query.Direction.DESCENDING)
}