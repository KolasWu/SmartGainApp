package com.example.smartgain.data

import android.graphics.Color

enum class OrderStatus(val label: String, val color: Int) {
    NEW("新訂單", Color.parseColor("#2196F3")),        // 藍色
    MODIFYING("修改中", Color.parseColor("#FF9800")),  // 橘色
    IN_PROGRESS("製作中", Color.parseColor("#9C27B0")),// 紫色
    AWAITING_PICKUP("待取貨", Color.parseColor("#00BCD4")), // 淺藍
    DONE("已完成", Color.parseColor("#4CAF50")),       // 綠色
    DELETED("已刪除", Color.parseColor("#757575")),    // 灰色
    RETURNED("已退回", Color.parseColor("#F44336"));   // 紅色

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.find { it.name == value } ?: NEW
        }
    }
}