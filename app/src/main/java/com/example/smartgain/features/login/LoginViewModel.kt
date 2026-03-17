package com.example.smartgain.features.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val _loginResult = MutableLiveData<Result<FirebaseUser>>()
    val loginResult: LiveData<Result<FirebaseUser>> = _loginResult

    fun loginOrRegister(email: String, password: String) {
        // 先嘗試登入
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = Result.success(auth.currentUser!!)
                } else {
                    // 登入失敗則自動註冊
                    register(email, password)
                }
            }
    }

    private fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = Result.success(auth.currentUser!!)
                } else {
                    _loginResult.value = Result.failure(task.exception ?: Exception("認證失敗"))
                }
            }
    }
}