package com.example.smartgain.features.orders // 1. 修正 package 名稱

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.CartItem
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.OrderStatus
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
    // 修改為即時監聽，這樣 _products 永遠有最新數據
    fun fetchProducts() {    productRepository.getProductsQuery().addSnapshotListener { snapshot, error ->
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
        orderRepository.updateOrderStatus(orderId, newStatus)
    }

    fun deleteOrder(orderId: String, checked: Boolean) {
        val targetOrder = _orders.value?.find { it.orderId == orderId }

        // Log 1: 檢查有沒有找到訂單
        android.util.Log.d("OrdersViewModel", "嘗試刪除訂單: $orderId, 找到物件: ${targetOrder != null}")

        if (checked && targetOrder != null) {
            targetOrder.items.forEach { item ->
                // Log 2: 檢查清單中的商品 ID
                val currentProduct = _products.value?.find { it.productId == item.productId }

                android.util.Log.d("OrdersViewModel", "商品: ${item.name}, ID: ${item.productId}, 當前庫存緩存: ${currentProduct?.stock}")

                if (currentProduct != null) {
                    val restoredStock = currentProduct.stock + item.quantity
                    productRepository.updateStock(item.productId, restoredStock)
                    android.util.Log.d("OrdersViewModel", "成功送出更新請求: $restoredStock")
                } else {
                    // 如果 _products 沒資料，嘗試直接從數據庫加回 (這是最後的保險)
                    android.util.Log.e("OrdersViewModel", "緩存中找不到產品，改用備用方案")
                    // 注意：這裡如果用 item.stock + item.quantity 可能會回到 17 個的問題
                    // 建議直接印出錯誤，並檢查為什麼 _products 是空的
                }
            }
        }
        orderRepository.markOrderAsDeleted(orderId)
    }

    /*
    fun deleteOrder(orderId: String, shouldRestock: Boolean) {
        // 1. 先從目前的 LiveData 列表中找到該筆訂單的完整資料
        val orderToDelete = _orders.value?.find { it.orderId == orderId }

        // 2. 執行刪除動作
        orderRepository.deleteOrder(orderId)

        // 3. 如果需要退回庫存，且有找到訂單資料
        if (shouldRestock && orderToDelete != null && orderToDelete.items.isNotEmpty()) {
            orderToDelete.items.forEach { item ->
                // 從目前的產品列表中找到該商品，獲取最新庫存
                val currentProduct = _products.value?.find { it.productId == item.productId }

                if (currentProduct != null) {
                    // 計算退回後的庫存：目前庫存 + 訂單當初扣除的數量
                    val newStock = currentProduct.stock + item.quantity
                    productRepository.updateStock(item.productId, newStock)
                } else {
                    // 如果產品列表沒緩存，則退回到下單時記錄的原始庫存加上去（保險做法）
                    productRepository.updateStock(item.productId, item.stock + item.quantity)
                }
            }
        }
    }*/

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
            val currentProd = _products.value?.find { it.productId == item.productId }
            val latestStock = currentProd?.stock ?: item.stock // 優先用最新的
            val remaining = latestStock - item.quantity

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