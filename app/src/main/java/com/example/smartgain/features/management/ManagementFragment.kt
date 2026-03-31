package com.example.smartgain.features.management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smartgain.R
import com.example.smartgain.data.Product
import com.example.smartgain.databinding.DialogAddProductBinding
import com.example.smartgain.databinding.FragmentManagementBinding
import com.example.smartgain.features.managementimport.ManagementViewModel
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class ManagementFragment : Fragment(R.layout.fragment_management) {

    //確保 ViewModel 存活在記憶體中，讓你的資料在旋轉後依然存在
    private val viewModel: ManagementViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //只負責把 XML 充氣成 View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_management, container, false)
    }

    //View 已經準備好、可以安全操作元件
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentManagementBinding.bind(view)

        adapter = ProductAdapter(
            onLongClick = { product -> showDeleteConfirmDialog(product) },
            onClick = { product -> showProductDialog(product) })
        binding.rvProducts.adapter = adapter

        //callback 改成 coroutine寫法
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.products.collect{ list ->
                    adapter.submitList(list)
                }
            }
        }

        //取得商品
        viewModel.fetchProducts()

        //新增商品按鈕
        binding.fabAddProduct.setOnClickListener {
            showProductDialog()
        }
    }

    private fun showDeleteConfirmDialog(product: Product) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("刪除商品")
            .setMessage("確定要刪除「${product.name}」嗎？此動作無法復原。")
            .setPositiveButton("確定刪除") { _, _ ->
                viewModel.deleteProduct(product.productId)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showProductDialog(product: Product? = null) {
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)
        val isEdit = product != null

        // 如果是編輯模式，先預填資料
        if (isEdit) {
            dialogBinding.etName.setText(product?.name)
            dialogBinding.etPrice.setText(product?.price.toString())
            dialogBinding.etStock.setText(product?.stock.toString())
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) "編輯商品" else "新增商品")
            .setView(dialogBinding.root)
            .setPositiveButton(if (isEdit) "儲存修改" else "新增") { _, _ ->
                val name = dialogBinding.etName.text.toString()
                val price = dialogBinding.etPrice.text.toString().toIntOrNull() ?: 0
                val stock = dialogBinding.etStock.text.toString().toIntOrNull() ?: 0

                if (name.isNotEmpty()) {
                    if (isEdit) {
                        // 呼叫更新
                        viewModel.updateProduct(product!!.productId, name, price, stock)
                    } else {
                        // 呼叫新增
                        viewModel.addProduct(name, price, stock)
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ManagementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}