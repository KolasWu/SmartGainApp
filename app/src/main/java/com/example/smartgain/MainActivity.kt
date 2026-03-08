package com.example.smartgain

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.smartgain.databinding.ActivityMainBinding
import com.example.smartgain.features.management.ManagementFragment
import com.example.smartgain.features.orders.OrdersFragment
import com.example.smartgain.features.overview.OverviewFragment
import com.example.smartgain.features.settings.SettingsFragment

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
        // 設定選單點擊監聽
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_overview -> replaceFragment(OverviewFragment())
                R.id.nav_orders -> replaceFragment(OrdersFragment())
                R.id.nav_management -> replaceFragment(ManagementFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
            }
            true
        }
        // 預設顯示首頁
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_overview
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}