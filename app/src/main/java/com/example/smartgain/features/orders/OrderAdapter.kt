package com.example.smartgain.features.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgain.data.Order
import com.example.smartgain.data.OrderStatus
import com.example.smartgain.databinding.ItemOrderBinding

class OrderAdapter(
    private var orders: List<Order>,
    private val onLongClick: (Order) -> Unit,   // 長按回調
    private val onClick: (Order) -> Unit,       // 點擊回調
    private val onStatusClick: (Order) -> Unit    // 點擊狀態回調
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // 建立 ViewHolder，綁定每一列的 XML
    // 準備格子
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    // 把資料塞進對應的元件中
    // 資料放進格子
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])

        // 1. 點擊整塊卡片 (Root View) -> 查看訂單內容
        holder.itemView.setOnClickListener {
            onClick(orders[position])
        }

        // 2. 點擊狀態標籤 (tvStatusTag) -> 彈出修改狀態選單
        // 注意：這裡直接抓 ViewHolder 裡的 binding 元件
        holder.getBinding().tvStatusTag.setOnClickListener {
            onStatusClick(orders[position])
        }

        // 3. 長按整塊卡片 -> 刪除邏輯
        holder.itemView.setOnLongClickListener {
            onLongClick(orders[position])
            true
        }
    }

    // 告訴系統總共有幾件貨物
    override fun getItemCount(): Int = orders.size

    // 當有新資料時更新清單
    fun updateData(newOrders: List<Order>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }

    class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun getBinding() = binding // 增加一個方法讓外部可以拿到標籤元件
        fun bind(order: Order) {
            val statusEnum = OrderStatus.fromString(order.status)

            binding.tvBuyerName.text = order.buyerName
            binding.tvOrderId.text = "#${order.orderId}"
            binding.tvTotalPrice.text = "$${order.totalPrice}"

            // 動態設定文字與顏色
            binding.tvStatusTag.text = statusEnum.label
            binding.tvStatusTag.backgroundTintList = android.content.res.ColorStateList.valueOf(statusEnum.color)

            // 如果是已刪除，可以讓整列變半透明或灰色
            if (statusEnum == OrderStatus.DELETED ||
            statusEnum == OrderStatus.RETURNED ||
            statusEnum == OrderStatus.DONE)
            {
                binding.root.alpha = 0.5f
                binding.root.isEnabled = false
            } else {
                binding.root.alpha = 1.0f
                binding.root.isEnabled = true
            }
        }
    }
}