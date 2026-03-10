package com.example.smartgain.features.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgain.data.CartItem
import com.example.smartgain.databinding.ItemSelectedProductBinding

class SelectedProductAdapter(
    private val items: MutableList<CartItem>,
    private val onTotalChanged: (Int) -> Unit // 當清單變動時，通知外面更新總金額
) : RecyclerView.Adapter<SelectedProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSelectedProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectedProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 顯示：名稱 x 數量 = 小計
        holder.binding.tvItemInfo.text = "${item.name} x ${item.quantity} = $${item.subtotal}"

        // 移除按鈕邏輯
        holder.binding.btnRemove.setOnClickListener {
            items.removeAt(position)
            notifyDataSetChanged()
            updateTotal() // 重新計算總金額
        }
    }

    override fun getItemCount(): Int = items.size

    // 計算並回傳總金額給 Fragment UI
    private fun updateTotal() {
        val total = items.sumOf { it.subtotal }
        onTotalChanged(total)
    }
}