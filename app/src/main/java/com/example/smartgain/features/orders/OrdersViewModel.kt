package com.example.smartgain.features.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository

class OrdersViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    fun fetchOrders() {
        repository.getOrdersQuery().addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _orders.value = it.toObjects(Order::class.java)
            }
        }
    }
}