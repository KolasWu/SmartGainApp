package com.example.smartgain.features.ordersimport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.Product

class OrdersViewModel : ViewModel() {
    // 透過 repository 取得數據，ViewModel 不再直接持有 db 實例
    private val repository = OrderRepository()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _lowStockCount = MutableLiveData<Int>()
    val lowStockCount: LiveData<Int> = _lowStockCount

    // 監聽訂單數據
    fun fetchOrders() {
        repository.getOrdersQuery().addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            snapshot?.let {
                _orders.value = it.toObjects(Order::class.java)
            }
        }
    }

    // 監聽庫存數據 (Overview)
    fun fetchOverviewData() {
        repository.getProductsQuery().addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            snapshot?.let {
                val products = it.toObjects(Product::class.java)
                // 計算庫存小於等於 5 的產品數量
                val count = products.count { product -> product.stock <= 5 }
                _lowStockCount.value = count
            }
        }
    }
}