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
        val currentOrder = _orders.value?.find { it.orderId == orderId }

        // 如果訂單已經是 DELETED，就不允許透過這個管道修改
        if (currentOrder?.status == OrderStatus.DELETED.name) {
            android.util.Log.w("OrdersViewModel", "無法修改已刪除訂單的狀態")
            return
        }
        orderRepository.updateOrderStatus(orderId, newStatus)
    }

    fun deleteOrder(order: Order) {
        // 這裡直接傳入整筆訂單物件
        productRepository.cancelOrderAndRestoreStock(order) { success ->
            if (success) {
                android.util.Log.d("OrdersViewModel", "訂單取消與庫存回補成功")
            } else {
                android.util.Log.e("OrdersViewModel", "操作失敗")
            }
        }
    }

    /* 第二版
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
    }*/

    /* 第一版
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

    fun executeBatchOrder(
        cartList: List<CartItem>,
        buyerName: String,
        prefix: String,
        onWarning: (String) -> Unit
    ) {
        if (cartList.isEmpty()) return

        // 1. 生成訂單基本資料
        val totalAmount = cartList.sumOf { it.subtotal }
        val customOrderId = generateOrderNumber(prefix)

        val newOrder = Order(
            orderId = customOrderId,
            buyerName = if (buyerName.isBlank()) "一般散客" else buyerName,
            totalPrice = totalAmount,
            items = cartList,
            status = "NEW",
            timestamp = System.currentTimeMillis()
        )

        // 2. 僅用於「警告提示」的迴圈 (不執行資料庫寫入)
        cartList.forEach { item ->
            val currentProd = _products.value?.find { it.productId == item.productId }
            val latestStock = currentProd?.stock ?: item.stock
            val estimatedRemaining = latestStock - item.quantity

            if (estimatedRemaining < 5) {
                onWarning("商品 [${item.name}] 下單後庫存將剩餘 $estimatedRemaining，請注意補貨！")
            }
        }

        // 3. 【核心修正】：將 Batch 寫入移到迴圈外！一次性處理整張單與所有庫存
        // 注意：這裡不再呼叫 orderRepository.addOrder(newOrder)，因為 Batch 已經包辦了
        productRepository.executeOrderBatch(newOrder, cartList) { success ->
            if (success) {
                android.util.Log.d("OrdersViewModel", "Batch 訂單成立且庫存已自動扣除 (Atomic)")
            } else {
                android.util.Log.e("OrdersViewModel", "Batch 寫入失敗")
            }
        }
    }
}