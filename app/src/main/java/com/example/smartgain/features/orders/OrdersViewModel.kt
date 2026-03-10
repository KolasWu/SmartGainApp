package com.example.smartgain.features.orders // 1. 修正 package 名稱

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.Product
import com.example.smartgain.data.ProductRepository

class OrdersViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _lowStockCount = MutableLiveData<Int>()
    val lowStockCount: LiveData<Int> = _lowStockCount

    private val _productList = MutableLiveData<List<Product>>()
    val productList: LiveData<List<Product>> = _productList

    // 監聽訂單數據
    fun fetchOrders() {
        orderRepository.getOrdersQuery().addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let {
                _orders.value = it.toObjects(Order::class.java)
            }
        }
    }

    // 監聽庫存數據 (修正點：改用 productRepository)
    fun fetchOverviewData() {
        productRepository.getProductsQuery().addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let {
                val products = it.toObjects(Product::class.java)
                val count = products.count { product -> product.stock <= 5 }
                _lowStockCount.value = count
            }
        }
    }

    // 抓取商品供選單使用
    fun fetchProductsForOrder() {
        productRepository.getProductsQuery().get().addOnSuccessListener { snapshot ->
            _productList.value = snapshot.toObjects(Product::class.java)
        }
    }

    // 核心邏輯：手動下單
    fun createManualOrder(product: Product, quantity: Int, onWarning: (String) -> Unit) {
        if (quantity > product.stock) {
            onWarning("錯誤：下單數量不能超過庫存 (${product.stock})")
            return
        }
        if (product.stock - quantity < 5) {
            onWarning("警告：庫存即將低於 5 個！")
        }

        val newOrder = Order(
            orderId = "",
            buyerName = "手動 Key 單",
            totalPrice = product.price * quantity,
            status = "NEW",
            timestamp = System.currentTimeMillis()
        )

        // 2. 確保 OrderRepository 有 addOrder
        orderRepository.addOrder(newOrder)

        // 3. 確保 ProductRepository 有 updateStock
        productRepository.updateStock(product.productId, product.stock - quantity)
    }
}