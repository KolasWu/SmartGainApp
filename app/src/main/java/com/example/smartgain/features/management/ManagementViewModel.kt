package com.example.smartgain.features.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.OrderRepository
import com.example.smartgain.data.Product
import com.google.firebase.firestore.FirebaseFirestore

class ManagementViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    fun fetchProducts() {
        repository.getProductsQuery().addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _products.value = it.toObjects(Product::class.java)
            }
        }
    }

    fun addProduct(name: String, price: Int, stock: Int) {
        val product = Product(
            productId = "", // 傳空，交給 Repository 處理 ID
            name = name,
            price = price,
            stock = stock
        )
        repository.addProduct(product)
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
    }
}