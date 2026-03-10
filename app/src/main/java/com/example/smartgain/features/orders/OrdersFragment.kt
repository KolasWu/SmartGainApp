package com.example.smartgain.features.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.smartgain.R
import com.example.smartgain.databinding.FragmentOrdersBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class OrdersFragment : Fragment(R.layout.fragment_orders) {
    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var adapter: OrderAdapter
    private var param1: String? = null
    private var param2: String? = null

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
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOrdersBinding.bind(view)

        // 初始化 Adapter
        adapter = OrderAdapter(emptyList())
        binding.rvOrders.adapter = adapter

        // 觀察資料變化
        viewModel.orders.observe(viewLifecycleOwner) { orderList ->
            adapter.updateData(orderList)
        }

        viewModel.fetchOrders()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OrdersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}