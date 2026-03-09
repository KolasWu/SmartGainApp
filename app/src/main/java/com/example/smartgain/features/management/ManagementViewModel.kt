package com.example.smartgain.features.management

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartgain.data.Product
import com.google.firebase.firestore.FirebaseFirestore

class ManagementViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    fun fetchProducts() {
        db.collection("products").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _products.value = it.toObjects(Product::class.java)
            }
        }
    }
}