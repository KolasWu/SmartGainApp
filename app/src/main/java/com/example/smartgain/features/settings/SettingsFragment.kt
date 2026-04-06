package com.example.smartgain.features.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smartgain.databinding.FragmentSettingsBinding
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.smartgain.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var auth: FirebaseAuth
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = auth.currentUser?.uid

        // 如果有 UID，就組合成 SmartGain 網址，否則給一個空字串或錯誤提示
        val myShopUrl = if (currentUserId != null) {
            "https://smartgain-aa826.web.app/?shopId=$currentUserId"
        } else {
            "https://smartgain-aa826.web.app"
        }

        // 點擊預覽賣場
        binding.btnOpenMyStore.setOnClickListener {
            if (myShopUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(myShopUrl))
                startActivity(intent)
            } else {
                Toast.makeText(context, "請先登入帳號以啟用賣場", Toast.LENGTH_SHORT).show()
            }
        }

        // 點擊複製連結
        binding.btnCopyLink.setOnClickListener {
            if (myShopUrl.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("SmartGain Shop Link", myShopUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "SmartGain 賣場連結已複製", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "無法複製連結，請確認登入狀態", Toast.LENGTH_SHORT).show()
            }
        }

        // 登出按鈕邏輯
        binding.btnLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("登出帳號")
                .setMessage("您確定要登出 SmartGain 嗎？")
                .setPositiveButton("確定登出") { _, _ ->
                    // 執行 Firebase 登出
                    com.google.firebase.Firebase.auth.signOut()

                    // 呼叫 MainActivity 的方法來檢查狀態並切換 Fragment
                    (activity as? MainActivity)?.checkUserStatus()

                    android.widget.Toast.makeText(context, "已成功登出", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}