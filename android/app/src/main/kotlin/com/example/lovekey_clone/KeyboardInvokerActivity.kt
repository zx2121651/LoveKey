package com.example.lovekey_clone

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback

/**
 * 透明的输入代理页：持有隐藏输入框并请求软键盘。
 * 悬浮球点击后启动本页，唤起当前输入法（LoveKey）的键盘。
 */
class KeyboardInvokerActivity : Activity() {

    private lateinit var hiddenInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hiddenInput = EditText(this)
        window.addContentView(
            hiddenInput,
            FrameLayout.LayoutParams(1, 1)
        )
        hiddenInput.requestFocus()

        // 系统返回键：收起软键盘并结束本页（兼容 API 33+ 预测性返回手势）
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(hiddenInput.windowToken, 0)
                    finish()
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(hiddenInput, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onPause() {
        // 页面失焦（例如键盘收起/切走）时结束自己
        if (!isFinishing) {
            finish()
        }
        super.onPause()
    }
}