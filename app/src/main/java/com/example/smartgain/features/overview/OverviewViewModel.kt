package com.example.smartgain.features.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.OrderStatus
import com.example.smartgain.data.Product
import com.example.smartgain.data.ProductRepository
import com.google.firebase.auth.FirebaseAuth

class OverviewViewModel : ViewModel() {
    // 拆分為兩個專門的 Repository
    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()

    //營收與訂單
    private val _revenue = MutableLiveData<Int>(0)
    val revenue: LiveData<Int> = _revenue

    private val _pendingCount = MutableLiveData<Int>(0)
    val pendingCount: LiveData<Int> = _pendingCount

    fun fetchTodaySummary() {
        val myId = auth.currentUser?.uid ?: "TEST_SELLER_001"

        // 監聽訂單以計算營收與待處理 (使用 orderRepository)
        orderRepository.getOrdersQuery(myId).addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                val orders = it.toObjects(Order::class.java)
                // 只有「非刪除」且「非退回」的訂單才計入營收
                _revenue.value = orders.filter { o ->
                    o.status != OrderStatus.DELETED.name && o.status != OrderStatus.RETURNED.name
                }.sumOf { o -> o.totalPrice }
            }
        }

        // 監聽商品以更新庫存警告
        productRepository.getProductsQuery(myId).addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                val allProducts = it.toObjects(Product::class.java)

                // 過濾出庫存 <= 5 的商品
                val lowStockList = allProducts.filter { p -> p.stock <= 5 }

                _lowStockCount.value = lowStockList.size
                _lowStockProducts.value = lowStockList
            }
        }
    }

    // 庫存相關
    private val _lowStockCount = MutableLiveData<Int>(0)
    val lowStockCount: LiveData<Int> = _lowStockCount
    private val _lowStockProducts = MutableLiveData<List<Product>>()
    val lowStockProducts: LiveData<List<Product>> = _lowStockProducts
}