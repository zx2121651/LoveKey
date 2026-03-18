package com.example.lovekey_clone

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class LoveKeyInputMethodService : InputMethodService() {

    private lateinit var keyboardView: View
    private lateinit var inputEditText: EditText
    private lateinit var aiReplyContainer: LinearLayout

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)

        inputEditText = keyboardView.findViewById(R.id.input_edit_text)
        val btnGenerateAi = keyboardView.findViewById<Button>(R.id.btn_generate_ai)
        val btnSend = keyboardView.findViewById<Button>(R.id.btn_send)
        aiReplyContainer = keyboardView.findViewById(R.id.ai_reply_container)

        btnGenerateAi.setOnClickListener {
            val inputText = inputEditText.text.toString()
            if (inputText.isNotEmpty()) {
                generateAiReplies(inputText)
            }
        }

        btnSend.setOnClickListener {
            val textToCommit = inputEditText.text.toString()
            if (textToCommit.isNotEmpty()) {
                val ic = currentInputConnection
                ic?.commitText(textToCommit, 1)
                inputEditText.text.clear()
            }
        }

        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Reset view when keyboard opens
        if (::inputEditText.isInitialized) {
            inputEditText.text.clear()
        }
        if (::aiReplyContainer.isInitialized) {
            aiReplyContainer.removeAllViews()
        }
    }

    private fun generateAiReplies(inputText: String) {
        aiReplyContainer.removeAllViews()

        // Mock AI replies based on input
        val replies = listOf(
            "收到你的消息啦，感觉有点小开心呢！",
            "哇，这也太巧了吧，我刚刚也在想你！",
            "你的话让我觉得心里暖暖的，哈哈。",
            "你这么说我会害羞的啦～",
            "真是一个高情商的回答呢！"
        )

        for (reply in replies) {
            val replyView = layoutInflater.inflate(R.layout.item_ai_reply, aiReplyContainer, false) as TextView
            replyView.text = reply
            replyView.setOnClickListener {
                // When an AI reply is clicked, directly commit it to the input connection
                val ic = currentInputConnection
                ic?.commitText(reply, 1)
            }
            aiReplyContainer.addView(replyView)
        }
    }
}
