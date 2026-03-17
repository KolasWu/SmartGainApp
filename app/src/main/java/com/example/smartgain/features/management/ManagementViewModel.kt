package com.example.smartgain.features.managementimport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Product
import com.example.smartgain.data.ProductRepository // 修正：改為導入 ProductRepository
import com.google.firebase.auth.FirebaseAuth

class ManagementViewModel : ViewModel() {
    // 修正：這裡應該改用 ProductRepository 而非 OrderRepository
    private val repository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    fun fetchProducts() {
        val myId = auth.currentUser?.uid ?: return
        // 修正：現在由 ProductRepository 負責提供產品查詢
        repository.getProductsQuery(myId).addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _products.value = it.toObjects(Product::class.java)
            }
        }
    }

    fun addProduct(name: String, price: Int, stock: Int, imageUrl: String = "") {
        // 1. 取得目前登入賣家的 UID
        val currentUserId = auth.currentUser?.uid ?: return

        val product = Product(
            productId = "", // 傳空，交給 Repository 處理 ID
            name = name,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            sellerId = currentUserId // 自動帶入賣家 ID
        )
        repository.addProduct(product)
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
    }

    fun updateProduct(id: String, name: String, price: Int, stock: Int, imageUrl: String = "") {
        val currentUserId = auth.currentUser?.uid ?: return
        val product = Product(
            productId = id, // 傳入現有的 ID，Repository 就會執行「覆蓋」動作
            name = name,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            sellerId = currentUserId
        )
        repository.addProduct(product)
    }
}