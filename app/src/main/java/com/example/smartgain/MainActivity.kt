package com.example.smartgain

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.smartgain.databinding.ActivityMainBinding
import com.example.smartgain.features.login.LoginFragment
import com.example.smartgain.features.management.ManagementFragment
import com.example.smartgain.features.orders.OrdersFragment
import com.example.smartgain.features.overview.OverviewFragment
import com.example.smartgain.features.settings.SettingsFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        checkUserStatus() // 啟動時檢查身分
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_overview -> replaceFragment(OverviewFragment())
                R.id.nav_orders -> replaceFragment(OrdersFragment())
                R.id.nav_management -> replaceFragment(ManagementFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
            }
            true
        }
    }

    // 提供給 LoginFragment 呼叫或在 onCreate 使用
    fun checkUserStatus() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // 未登入：隱藏導覽列並顯示登入頁
            binding.bottomNavigation.visibility = View.GONE
            replaceFragment(LoginFragment())
        } else {
            // 已登入：顯示導覽列並載入首頁
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.bottomNavigation.selectedItemId = R.id.nav_overview
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}