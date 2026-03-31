package com.example.smartgain.features.orders

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderStatus
import com.example.smartgain.databinding.ItemOrderBinding

class OrderAdapter(
    private val onLongClick: (Order) -> Unit,
    private val onClick: (Order) -> Unit,
    private val onStatusClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    //建立外殼
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    //把資料填進外殼
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)

        // 呼叫 bind 處理資料與狀態
        holder.bind(order, onClick, onLongClick, onStatusClick)
    }

    class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            order: Order,
            onClick: (Order) -> Unit,
            onLongClick: (Order) -> Unit,
            onStatusClick: (Order) -> Unit
        ) {
            val statusEnum = OrderStatus.fromString(order.status)

            binding.tvBuyerName.text = order.buyerName
            binding.tvOrderId.text = "#${order.orderId}"
            binding.tvTotalPrice.text = "$${order.totalPrice}"

            // 1. 設定狀態標籤
            binding.tvStatusTag.text = statusEnum.label
            binding.tvStatusTag.backgroundTintList = android.content.res.ColorStateList.valueOf(statusEnum.color)

            // 2. 處理特定狀態的視覺回饋
            val isInactive = statusEnum == OrderStatus.DELETED ||
                    statusEnum == OrderStatus.RETURNED ||
                    statusEnum == OrderStatus.DONE

            binding.root.alpha = if (isInactive) 0.5f else 1.0f
            // 注意：若設為 isEnabled = false，則該 Item 可能無法再接收任何點擊（包含長按）
            // 建議保留點擊功能，但在點擊回調中做檢查，或僅禁用狀態按鈕
            binding.tvStatusTag.isEnabled = !isInactive

            // 3. 設定監聽器 (集中在 bind 處理更簡潔)
            binding.root.setOnClickListener { onClick(order) }
            binding.root.setOnLongClickListener {
                onLongClick(order)
                true
            }
            binding.tvStatusTag.setOnClickListener {
                if (!isInactive) onStatusClick(order)
            }
        }
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {

    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.buyerName == newItem.buyerName &&
                oldItem.totalPrice == newItem.totalPrice &&
                oldItem.contact == newItem.contact &&
                oldItem.status == newItem.status &&
                oldItem.timestamp == newItem.timestamp &&
                oldItem.items == newItem.items
    }
}