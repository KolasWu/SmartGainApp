package com.example.smartgain.features.management

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartgain.data.Product
import com.example.smartgain.databinding.ItemProductBinding

class ProductAdapter(
    private val onLongClick: (Product) -> Unit, // 長按回調
    private val onClick: (Product) -> Unit // 點擊回調
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position), onLongClick, onClick)
        val currentProduct = getItem(position) // 先取得當前物件
        Log.d("GlideCheck", "商品: ${currentProduct.name}, 網址: ${currentProduct.imageUrl}")
    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(product: Product, onLongClick: (Product) -> Unit, onClick: (Product) -> Unit) {

            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "$${product.price}"

            binding.root.setOnClickListener { onClick(product) }
            binding.root.setOnLongClickListener { onLongClick(product); true }

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

            Glide.with(binding.root)
                .load(product.imageUrl.ifEmpty { null })
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.ivProductIcon)
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {

    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.name == newItem.name &&
                oldItem.price == newItem.price &&
                oldItem.stock == newItem.stock &&
                oldItem.imageUrl == newItem.imageUrl &&
                oldItem.sellerId == newItem.sellerId
    }
}