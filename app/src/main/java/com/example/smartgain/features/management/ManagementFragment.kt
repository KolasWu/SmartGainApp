package com.example.smartgain.features.management

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.smartgain.R
import com.example.smartgain.data.Product
import com.example.smartgain.databinding.DialogAddProductBinding
import com.example.smartgain.databinding.FragmentManagementBinding
import com.example.smartgain.features.managementimport.ManagementViewModel
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class ManagementFragment : Fragment(R.layout.fragment_management) {
    private val viewModel: ManagementViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private var param1: String? = null
    private var param2: String? = null

    private var selectedImageUri: Uri? = null
    private var dialodImagePreview: ImageView? = null


    private val pickImageLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()) { uri ->
            android.util.Log.d("SmartGain", "選到圖片了：$uri")
            uri?.let { it->
                    selectedImageUri = it
                    dialodImagePreview?.let{ preview ->
                        Glide.with(this)
                            .load(it)                          //本機uri
                            .into(preview)
                    }
                }
    }
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

        dialodImagePreview = dialogBinding.imagePreview

        dialogBinding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        // 如果是編輯模式，先預填資料
        if (isEdit) {
            dialogBinding.etName.setText(product?.name)
            dialogBinding.etPrice.setText(product?.price.toString())
            dialogBinding.etStock.setText(product?.stock.toString())
            dialogBinding.etDescription.setText(product?.description)
            //載入現有圖片
            if(!product?.imageUrl.isNullOrEmpty()){
                dialodImagePreview?.let{ preview ->
                    Glide.with(this)
                        .load(product?.imageUrl)
                        .into(preview)
                }
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) "編輯商品" else "新增商品")
            .setView(dialogBinding.root)
            .setPositiveButton(if (isEdit) "儲存修改" else "新增") { _, _ ->
                val name = dialogBinding.etName.text.toString()
                val price = dialogBinding.etPrice.text.toString().toIntOrNull() ?: 0
                val stock = dialogBinding.etStock.text.toString().toIntOrNull() ?: 0
                val description = dialogBinding.etDescription.text.toString()

                // name 是空的就直接離開，不繼續
                if (name.isEmpty()) return@setPositiveButton

                val uri = selectedImageUri

                if (uri == null) {
                    // 沒有選圖：直接存，imageUrl 給空字串
                    if (isEdit) {
                        viewModel.updateProduct(product!!.productId, name, price, stock, product.imageUrl, description)
                    } else {
                        viewModel.addProduct(name, price, stock, "", description)
                    }
                } else {
                    // 有選圖：先上傳，拿到 URL 再存
                    viewModel.uploadProductImage(uri, name,
                        onSuccess = { imageUrl ->
                            if (isEdit) {
                                viewModel.updateProduct(product!!.productId, name, price, stock, imageUrl, description)
                            } else {
                                viewModel.addProduct(name, price, stock, imageUrl, description)
                            }
                        },
                        onFailure = {
                            // 之後補上失敗提示
                        }
                    )
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