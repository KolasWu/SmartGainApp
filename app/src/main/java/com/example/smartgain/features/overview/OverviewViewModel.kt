package com.example.smartgain.features.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository

class OverviewViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _revenue = MutableLiveData<Int>(0)
    val revenue: LiveData<Int> = _revenue

    private val _pendingCount = MutableLiveData<Int>(0)
    val pendingCount: LiveData<Int> = _pendingCount

    fun fetchTodaySummary() {
        // 監聽 Firestore 訂單集合
        repository.getOrdersQuery().addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener

            val orders = snapshot.toObjects(Order::class.java)

            // 計算今日營收 (total_price)
            val total = orders.sumOf { it.totalPrice }
            // 計算狀態為 NEW 的訂單數量
            val pending = orders.count { it.status == "NEW" }

            _revenue.value = total
            _pendingCount.value = pending
        }
    }
}