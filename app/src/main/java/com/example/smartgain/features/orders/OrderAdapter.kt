package com.example.smartgain.features.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgain.R
import com.example.smartgain.data.Order
import com.example.smartgain.databinding.ItemOrderBinding

class OrderAdapter(private var orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // 建立 ViewHolder，綁定每一列的 XML
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    // 把資料塞進對應的元件中
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    // 當有新資料時，用來更新清單的方法
    fun updateData(newOrders: List<Order>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }

    class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.tvBuyerName.text = order.buyerName
            binding.tvOrderId.text = "#${order.orderId}"
            binding.tvTotalPrice.text = "$${order.totalPrice}"
            binding.tvStatusTag.text = order.status

            // 根據狀態改變標籤顏色
            val context = binding.root.context
            if (order.status == "DONE") {
                binding.tvStatusTag.setBackgroundResource(R.drawable.bg_status_tag) // 你可以再做一個綠色的
                binding.tvStatusTag.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_green_dark)
            } else {
                binding.tvStatusTag.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_red_light)
            }
        }
    }
}