package com.example.smartgain.features.orders

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartgain.R
import com.example.smartgain.data.CartItem
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderStatus
import com.example.smartgain.data.Product
import com.example.smartgain.databinding.DialogManualOrderBinding
import com.example.smartgain.databinding.DialogOrderDetailsBinding
import com.example.smartgain.databinding.FragmentOrdersBinding
import kotlinx.coroutines.launch

class OrdersFragment : Fragment(R.layout.fragment_orders) {
    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var orderAdapter: OrderAdapter

    // 暫存目前選中的商品，供對話框邏輯使用
    private var selectedProduct: Product? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOrdersBinding.bind(view)

        // 1. 初始化主畫面的訂單列表
        orderAdapter = OrderAdapter(
            emptyList(),
            onLongClick = { order ->
                val currentStatus = OrderStatus.fromString(order.status)
                val restrictedStatuses = setOf(
                    OrderStatus.DONE,
                    OrderStatus.DELETED,
                    OrderStatus.RETURNED
                )

                if(currentStatus in restrictedStatuses)
                    Toast.makeText(context, "訂單目前為[${currentStatus.label}]，無法刪除", Toast.LENGTH_SHORT).show()
                else
                    showDeleteConfirmDialog(order) },
            onClick = { order -> showOrderContent(order) },
            onStatusClick = { order ->
                val currentStatus = OrderStatus.fromString(order.status)
                val restrictedStatuses = setOf(
                    OrderStatus.DONE,
                    OrderStatus.DELETED,
                    OrderStatus.RETURNED
                )
                if(currentStatus !in restrictedStatuses) showStatusUpdateDialog(order)
                else Toast.makeText(context, "此狀態不可修改", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvOrders.adapter = orderAdapter

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.orders.collect{ orderList ->
                    orderAdapter.updateData(orderList)
                }
            }
        }

        // 3. 設定「新增訂單」按鈕
        binding.fabAddOrder.setOnClickListener {
            showManualOrderDialog()
        }

        viewModel.fetchOrders()         //監聽訂單
        viewModel.fetchProducts()       //監聽產品
    }

    private fun showOrderContent(order: Order) {
        // 1. 建立對話框 Binding
        val dialogBinding = DialogOrderDetailsBinding.inflate(layoutInflater)

        // 2. 設定基本資訊
        dialogBinding.tvDetailTitle.text = "訂單：${order.orderId}"
        dialogBinding.tvDetailBuyer.text = "買家：${order.buyerName}"
        dialogBinding.tvDetailTotal.text = "總計：$${order.totalPrice}"

        // 3. 組合商品明細字串
        // 這裡我們把 List<CartItem> 轉換成易讀的文字列
        if (order.items.isNotEmpty()) {
            val details = order.items.joinToString("\n") { item ->
                "• ${item.name} x ${item.quantity}  ($${item.subtotal})"
            }
            dialogBinding.tvItemsList.text = details
        } else {
            dialogBinding.tvItemsList.text = "無商品明細資料"
        }

        // 4. 彈出對話框
        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("確定", null)
            .show()
    }

    private fun showStatusUpdateDialog(order: Order) {
        // 1. 取得除了 DELETED 以外的所有狀態（刪除應走長按流程）
        val statusOptions = OrderStatus.entries.filter { it != OrderStatus.DELETED }
        val labels = statusOptions.map { it.label }.toTypedArray()

        // 2. 彈出 Material 選擇視窗
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("變更訂單狀態")
            .setItems(labels) { _, which ->
                val selectedStatus = statusOptions[which]

                // 3. 呼叫 ViewModel 更新 Firestore
                viewModel.updateStatus(order.orderId, selectedStatus)

                Toast.makeText(context, "狀態已更新為：${selectedStatus.label}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteConfirmDialog(order: Order) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("刪除訂單")
            .setMessage("確定要刪除訂單 #${order.orderId} 嗎？\n刪除後將自動補回庫存。")
            .setPositiveButton("確定") { _, _ ->
                // 執行新版的刪除邏輯
                viewModel.deleteOrder(order)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showManualOrderDialog() {
        val dialogBinding = DialogManualOrderBinding.inflate(layoutInflater)
        val cartList = mutableListOf<CartItem>() // 這是我們的「購物車」

        // A. 設定對話框內的 RecyclerView (購物車清單)
        val cartAdapter = SelectedProductAdapter(cartList) { total ->
            dialogBinding.tvTotalAmount.text = "總計：$$total"
        }
        dialogBinding.rvSelectedItems.layoutManager = LinearLayoutManager(context)
        dialogBinding.rvSelectedItems.adapter = cartAdapter

        // B. 獲取商品清單並設定 Spinner
        val products = viewModel.products.value

        // 1. 提取產品名稱列表
        val productNames = products.map { it.name }

        // 2. 設定 Spinner Adapter
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            productNames
        )
        dialogBinding.spinnerProducts.adapter = spinnerAdapter

        // 3. 設定選取監聽器
        dialogBinding.spinnerProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                if (products.isNotEmpty()) {
                    selectedProduct = products[pos]
                    dialogBinding.tvCurrentStock.text = "目前庫存：${selectedProduct?.stock}"
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // C. 「加入清單」按鈕邏輯
        dialogBinding.btnAddItem.setOnClickListener {
            val qty = dialogBinding.etQuantity.text.toString().toIntOrNull() ?: 0
            val product = selectedProduct ?: return@setOnClickListener

            if (qty <= 0 || qty > product.stock) {
                dialogBinding.tilQuantity.error = "數量不合法或超過庫存"
                return@setOnClickListener
            }

            // 檢查是否重複加入，有的話就累加
            val existingItem = cartList.find { it.productId == product.productId }
            if (existingItem != null) {
                if (existingItem.quantity + qty > product.stock) {
                    dialogBinding.tilQuantity.error = "總數超過庫存"
                } else {
                    existingItem.quantity += qty
                    dialogBinding.tilQuantity.error = null
                }
            } else {
                cartList.add(CartItem(product.productId, product.name, qty, product.price, product.stock))
                dialogBinding.tilQuantity.error = null
            }

            cartAdapter.notifyDataSetChanged()
            dialogBinding.etQuantity.text?.clear()
            val total = cartList.sumOf { it.subtotal }
            dialogBinding.tvTotalAmount.text = "總計：$$total"
        }

        // D. 顯示對話框
        AlertDialog.Builder(requireContext())
            .setTitle("手動新增訂單")
            .setView(dialogBinding.root)
            .setPositiveButton("確認下單") { _, _ ->
                if (cartList.isNotEmpty()) {
                    // 新增：抓取買家名稱 (從你剛才在 XML 新增的 etBuyerName)
                    val buyerName = dialogBinding.etBuyerName.text.toString()

                    // 這裡預設為 "H" (手動)，若你有加 RadioGroup 則可動態判斷
                    val prefix = "H"

                    showFinalConfirmation(cartList, buyerName, prefix)
                } else {
                    Toast.makeText(context, "清單是空的！", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 更新：加入 buyerName 與 prefix 的傳遞
    private fun showFinalConfirmation(cartList: List<CartItem>, buyerName: String, prefix: String) {
        val total = cartList.sumOf { it.subtotal }
        AlertDialog.Builder(requireContext())
            .setTitle("最後確認")
            .setMessage("確認建立這筆訂單嗎？\n買家：${if(buyerName.isBlank()) "一般散客" else buyerName}\n總金額：$$total")
            .setPositiveButton("送出") { _, _ ->
                // 呼叫 ViewModel 更新後的方法
                viewModel.executeBatchOrder(cartList, buyerName, prefix) { warning ->
                    Toast.makeText(context, warning, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("返回", null)
            .show()
    }
}