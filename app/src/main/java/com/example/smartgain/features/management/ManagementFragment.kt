package com.example.smartgain.features.management

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.smartgain.R
import com.example.smartgain.data.Product
import com.example.smartgain.databinding.DialogAddProductBinding
import com.example.smartgain.databinding.FragmentManagementBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ManagementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManagementFragment : Fragment(R.layout.fragment_management) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentManagementBinding.bind(view)

        adapter = ProductAdapter(emptyList()) { product ->
            showDeleteConfirmDialog(product)
        }
        binding.rvProducts.adapter = adapter

        viewModel.products.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

        viewModel.fetchProducts()

        binding.fabAddProduct.setOnClickListener {
            showAddProductDialog()
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

    private fun showAddProductDialog() {
        val dialogBinding = DialogAddProductBinding.inflate(layoutInflater)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("新增商品")
            .setView(dialogBinding.root)
            .setPositiveButton("新增") { _, _ ->
                val name = dialogBinding.etName.text.toString()
                val price = dialogBinding.etPrice.text.toString().toIntOrNull() ?: 0
                val stock = dialogBinding.etStock.text.toString().toIntOrNull() ?: 0

                if (name.isNotEmpty()) {
                    viewModel.addProduct(name, price, stock)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ManagementFragment.
         */
        // TODO: Rename and change types and number of parameters
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