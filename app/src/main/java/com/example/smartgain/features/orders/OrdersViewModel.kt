package com.example.smartgain.features.orders // 1. 修正 package 名稱

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.CartItem
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.Product
import com.example.smartgain.data.ProductRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun deleteOrder(orderId: String) {
        orderRepository.deleteOrder(orderId)
    }

    /**
     * 新增：生成訂單編號邏輯
     * 規則：前綴 + yyyyMMdd + 三位序號 (例如 H20260312001)
     */
    private fun generateOrderNumber(prefix: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // 隨日重新從 1 開始計算：過濾出今天且符合該前綴的訂單數量
        val todayCount = _orders.value?.count {
            it.orderId.contains(todayStr) && it.orderId.startsWith(prefix)
        } ?: 0

        val sequence = String.format("%03d", todayCount + 1)
        return "$prefix$todayStr$sequence"
    }

    // 更新：加入 buyerName 與 prefix 參數，並儲存商品明細
    fun executeBatchOrder(
        cartList: List<CartItem>,
        buyerName: String,
        prefix: String,
        onWarning: (String) -> Unit
    ) {
        if (cartList.isEmpty()) return

        // 1. 計算整張訂單的總金額
        val totalAmount = cartList.sumOf { it.subtotal }

        // 新增：根據規則生成自動編號
        val customOrderId = generateOrderNumber(prefix)

        // 2. 建立一筆總體訂單
        val newOrder = Order(
            orderId = customOrderId, // 使用自動生成的編號
            buyerName = if (buyerName.isBlank()) "一般散客" else buyerName, // 或是讓使用者輸入姓名
            totalPrice = totalAmount,
            items = cartList, // 【關鍵新增】：將購物車明細存入訂單，供後續 showOrderContent 顯示
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