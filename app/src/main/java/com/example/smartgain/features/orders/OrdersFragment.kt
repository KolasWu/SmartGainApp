package com.example.smartgain.features.orders

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartgain.R
import com.example.smartgain.data.CartItem
import com.example.smartgain.data.Product
import com.example.smartgain.databinding.DialogManualOrderBinding
import com.example.smartgain.databinding.FragmentOrdersBinding

class OrdersFragment : Fragment(R.layout.fragment_orders) {
    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var orderAdapter: OrderAdapter

    // 暫存目前選中的商品，供對話框邏輯使用
    private var selectedProduct: Product? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOrdersBinding.bind(view)

        // 1. 初始化主畫面的訂單列表
        orderAdapter = OrderAdapter(emptyList())
        binding.rvOrders.adapter = orderAdapter

        // 2. 觀察訂單 LiveData
        viewModel.orders.observe(viewLifecycleOwner) { orderList ->
            orderAdapter.updateData(orderList)
        }

        // 3. 設定「新增訂單」按鈕（假設你在 xml 裡有一個 id 為 fabAddOrder 的按鈕）
        binding.fabAddOrder.setOnClickListener {
            showManualOrderDialog()
        }

        viewModel.fetchOrders()
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
        viewModel.products.observe(viewLifecycleOwner) { products ->
            val productNames = products.map { it.name }
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, productNames)
            dialogBinding.spinnerProducts.adapter = spinnerAdapter

            dialogBinding.spinnerProducts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    selectedProduct = products[pos]
                    dialogBinding.tvCurrentStock.text = "目前庫存：${selectedProduct?.stock}"
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
        viewModel.fetchProducts() // 記得在 ViewModel 寫這個方法來抓商品

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
            // 手動觸發一次總金額更新
            val total = cartList.sumOf { it.subtotal }
            dialogBinding.tvTotalAmount.text = "總計：$$total"
        }

        // D. 顯示對話框
        AlertDialog.Builder(requireContext())
            .setTitle("手動新增訂單")
            .setView(dialogBinding.root)
            .setPositiveButton("確認下單") { _, _ ->
                if (cartList.isNotEmpty()) {
                    showFinalConfirmation(cartList)
                } else {
                    Toast.makeText(context, "清單是空的！", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showFinalConfirmation(cartList: List<CartItem>) {
        val total = cartList.sumOf { it.subtotal }
        AlertDialog.Builder(requireContext())
            .setTitle("最後確認")
            .setMessage("確認建立這筆訂單嗎？總金額：$$total")
            .setPositiveButton("送出") { _, _ ->
                viewModel.executeBatchOrder(cartList) { warning ->
                    Toast.makeText(context, warning, Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("返回", null)
            .show()
    }
}