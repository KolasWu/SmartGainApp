package com.example.smartgain.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class TransactionRepository {
    private val db = FirebaseFirestore.getInstance()

    // 從 ProductRepository 搬來
    fun executeOrderBatch(order: Order, cartList: List<CartItem>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        val orderRef = if (order.orderId.isEmpty()) {
            db.collection("orders").document()
        } else {
            db.collection("orders").document(order.orderId)
        }
        val finalOrder = order.copy(orderId = orderRef.id)
        batch.set(orderRef, finalOrder)

        cartList.forEach { item ->
            val productRef = db.collection("products").document(item.productId)
            batch.update(productRef, "stock",
                com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong()))
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun cancelOrderAndRestoreStock(order: Order, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        val orderRef = db.collection("orders").document(order.orderId)
        batch.update(orderRef, "status", OrderStatus.DELETED.name)

        order.items.forEach { item ->
            val productRef = db.collection("products").document(item.productId)
            batch.update(productRef, "stock",
                com.google.firebase.firestore.FieldValue.increment(item.quantity.toLong()))
        }

        batch.commit().addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}