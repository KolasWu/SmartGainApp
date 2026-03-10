package com.example.smartgain.features.management

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgain.data.Product
import com.example.smartgain.databinding.ItemProductBinding

class ProductAdapter(
    private var products: List<Product>,
    private val onLongClick: (Product) -> Unit, // 長按回調
    private val onClick: (Product) -> Unit // 點擊回調
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)

        // 設定一般點擊 -> 觸發編輯
        holder.itemView.setOnClickListener {
            onClick(product)
        }

        // 設定長按監聽
        holder.itemView.setOnLongClickListener {
            onLongClick(product)
            true
        }
    }

    override fun getItemCount() = products.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {

            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "$${product.price}"

            // 設定庫存文字與顏色警告
            binding.tvProductStock.text = "庫存：${product.stock}"
            if (product.stock <= 5) {
                // 庫存不足：顯示紅色
                binding.tvProductStock.setTextColor(android.graphics.Color.RED)
                binding.tvProductStock.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                // 庫存正常：顯示灰色
                binding.tvProductStock.setTextColor(android.graphics.Color.GRAY)
                binding.tvProductStock.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }
}