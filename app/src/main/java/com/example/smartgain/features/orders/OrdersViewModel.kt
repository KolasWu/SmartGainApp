package com.example.smartgain.features.orders // 1. 修正 package 名稱

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.CartItem
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.OrderStatus
import com.example.smartgain.data.Product
import com.example.smartgain.data.ProductRepository
import com.example.smartgain.data.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val productRepository = ProductRepository()
    private val transactionRepository = TransactionRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private var orderListener : ListenerRegistration ?= null
    private var productListener : ListenerRegistration ?= null
    private var overviewListener : ListenerRegistration ?= null

    // 監聽訂單數據
    fun fetchOrders() {
        val myId = auth.currentUser?.uid ?: return
        orderListener?.remove()

        orderListener = orderRepository.getOrdersQuery(myId).addSnapshotListener { snapshot, error ->
            if (error != null){
                return@addSnapshotListener
            }
            try {
                snapshot?.toObjects(Order::class.java)?.let{
                    _orders.value = it
                }
            } catch (e: Exception) {
                Log.e("SmartGainDebug", "資料轉換成 Order 物件時發生錯誤: ${e.message}")
            }
        }
    }

    // 抓取商品供選單使用
    // 修改為即時監聽，這樣 _products 永遠有最新數據
    fun fetchProducts() {
        val myId = auth.currentUser?.uid ?: return
        productListener?.remove()
        productListener = productRepository.getProductsQuery(myId).addSnapshotListener { snapshot, error ->
        if (error != null) {
            android.util.Log.e("OrdersViewModel", "監聽產品失敗", error)
            return@addSnapshotListener
        }
        snapshot?.let {
            _products.value = it.toObjects(Product::class.java)
        }
    }
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        val currentOrder = _orders.value.find { it.orderId == orderId }

        // 如果訂單已經是 DELETED，就不允許透過這個管道修改
        if (currentOrder?.status == OrderStatus.DELETED.name) {
            android.util.Log.w("OrdersViewModel", "無法修改已刪除訂單的狀態")
            return
        }
        orderRepository.updateOrderStatus(orderId, newStatus)
    }

    fun deleteOrder(order: Order) {
        // 這裡直接傳入整筆訂單物件
        transactionRepository.cancelOrderAndRestoreStock(order) { success ->
            if (success) {
                android.util.Log.d("OrdersViewModel", "訂單取消與庫存回補成功")
            } else {
                android.util.Log.e("OrdersViewModel", "操作失敗")
            }
        }
    }

    /**
     * 新增：生成訂單編號邏輯
     * 規則：前綴 + yyyyMMdd + 三位序號 (例如 H20260312001)
     */
    @SuppressLint("DefaultLocale")
    private fun generateOrderNumber(prefix: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        // 隨日重新從 1 開始計算：過濾出今天且符合該前綴的訂單數量
        val todayCount = _orders.value.count {
            it.orderId.contains(todayStr) && it.orderId.startsWith(prefix)
        }

        val sequence = String.format("%03d", todayCount + 1)
        return "$prefix$todayStr$sequence"
    }

    fun executeBatchOrder(
        cartList: List<CartItem>,
        buyerName: String,
        prefix: String,
        onWarning: (String) -> Unit
    ) {
        if (cartList.isEmpty()) return

        val myId = auth.currentUser?.uid ?: return // 取得目前 ID

        // 1. 生成訂單基本資料
        val totalAmount = cartList.sumOf { it.subtotal }
        val customOrderId = generateOrderNumber(prefix)

        val newOrder = Order(
            orderId = customOrderId,
            sellerId = myId,
            buyerName = if (buyerName.isBlank()) "一般散客" else buyerName,
            totalPrice = totalAmount,
            items = cartList,
            status = "NEW",
            timestamp = System.currentTimeMillis()
        )

        // 2. 僅用於「警告提示」的迴圈 (不執行資料庫寫入)
        cartList.forEach { item ->
            val currentProd = _products.value.find { it.productId == item.productId }
            val latestStock = currentProd?.stock ?: item.stock
            val estimatedRemaining = latestStock - item.quantity

            if (estimatedRemaining < 5) {
                onWarning("商品 [${item.name}] 下單後庫存將剩餘 $estimatedRemaining，請注意補貨！")
            }
        }

        // 3. 【核心修正】：將 Batch 寫入移到迴圈外！一次性處理整張單與所有庫存
        // 注意：這裡不再呼叫 orderRepository.addOrder(newOrder)，因為 Batch 已經包辦了
        transactionRepository.executeOrderBatch(newOrder, cartList) { success ->
            if (success) {
                android.util.Log.d("OrdersViewModel", "Batch 訂單成立且庫存已自動扣除 (Atomic)")
            } else {
                android.util.Log.e("OrdersViewModel", "Batch 寫入失敗")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        orderListener?.remove()
        productListener?.remove()
        overviewListener?.remove()
    }
}