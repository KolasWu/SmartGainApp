package com.example.smartgain.features.managementimport

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Product
import com.example.smartgain.data.ProductRepository // 修正：改為導入 ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ManagementViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private var productListener: ListenerRegistration? = null

    fun fetchProducts() {
        val myId = auth.currentUser?.uid ?: return
        productListener?.remove()
        productListener = repository.getProductsQuery(myId).addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _products.value = it.toObjects(Product::class.java)
            }
        }
    }

    fun addProduct(name: String, price: Int, stock: Int, imageUrl: String = "", description: String = "") {
        // 1. 取得目前登入賣家的 UID
        val currentUserId = auth.currentUser?.uid ?: return

        val product = Product(
            productId = "", // 傳空，交給 Repository 處理 ID
            name = name,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            sellerId = currentUserId, // 自動帶入賣家 ID
            description = description
        )
        repository.addProduct(product)
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(productId)
    }

    fun updateProduct(id: String, name: String, price: Int, stock: Int, imageUrl: String = "", description: String = "") {
        val currentUserId = auth.currentUser?.uid ?: return
        val product = Product(
            productId = id, // 傳入現有的 ID，Repository 就會執行「覆蓋」動作
            name = name,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            sellerId = currentUserId,
            description = description
        )
        repository.addProduct(product)
    }

    fun uploadProductImage(
        imageUri: Uri,
        productName: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        val myId = auth.currentUser?.uid ?: return
        val storage = FirebaseStorage.getInstance()

        val name = productName.replace(" ", "_")
        val fileName = "${name}_${System.currentTimeMillis()}.jpg"

        val storageRef = storage.reference
            .child("product_images")
            .child(myId)
            .child(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    override fun onCleared() {
        super.onCleared()
        productListener?.remove()
    }
}