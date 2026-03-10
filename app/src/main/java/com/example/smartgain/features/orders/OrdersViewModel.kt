package com.example.smartgain.features.orders // 1. 修正 package 名稱

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.CartItem
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

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

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
    fun fetchProducts() {
        productRepository.getProductsQuery().get().addOnSuccessListener { snapshot ->
            _products.value = snapshot.toObjects(Product::class.java)
        }
    }

    fun executeBatchOrder(cartList: List<CartItem>, onWarning: (String) -> Unit) {
        if (cartList.isEmpty()) return

        // 1. 計算整張訂單的總金額
        val totalAmount = cartList.sumOf { it.subtotal }

        // 2. 建立一筆總體訂單
        val newOrder = Order(
            orderId = "",
            buyerName = "手動 Key 單", // 或是讓使用者輸入姓名
            totalPrice = totalAmount,
            status = "NEW",
            timestamp = System.currentTimeMillis()
        )
        orderRepository.addOrder(newOrder)

        // 3. 逐一處理庫存扣除與警示
        cartList.forEach { item ->
            val remaining = item.stock - item.quantity

            // 觸發低庫存警示
            if (remaining < 5) {
                onWarning("商品 [${item.name}] 下單後庫存剩餘 $remaining，請注意補貨！")
            }

            // 呼叫 ProductRepository 更新該商品的剩餘數量
            // 這裡我們直接傳入 productId 和新庫存量
            productRepository.updateStock(item.productId, remaining)
        }
    }
}