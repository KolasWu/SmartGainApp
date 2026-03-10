package com.example.smartgain.features.overview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.smartgain.databinding.FragmentOverviewBinding
import com.example.smartgain.features.overviewimport.OverviewViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class OverviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OverviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 觀察營收與待處理量
        viewModel.revenue.observe(viewLifecycleOwner) { total ->
            binding.layoutSummary.tvRevenue.text = "$$total"
        }

        viewModel.pendingCount.observe(viewLifecycleOwner) { count ->
            binding.layoutSummary.tvPendingCount.text = count.toString()
        }

        // 觀察庫存警告數字
        viewModel.lowStockCount.observe(viewLifecycleOwner) { count ->
            binding.layoutSummary.tvLowStock.text = count.toString()
        }

        // 新增：觀察庫存不足清單並更新「最近動態」
        viewModel.lowStockProducts.observe(viewLifecycleOwner) { products ->
            if (products.isEmpty()) {
                binding.tvRecentActivity.text = "目前沒有特別動態"
                binding.tvRecentActivity.setTextColor(android.graphics.Color.GRAY)
            } else {
                // 組合警告文字
                val warningText = products.joinToString("\n") { p ->
                    "⚠️ 警告：${p.name} 庫存僅剩 ${p.stock} 件！"
                }
                binding.tvRecentActivity.text = warningText
                binding.tvRecentActivity.setTextColor(android.graphics.Color.RED)
            }
        }

        viewModel.fetchTodaySummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OverviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}