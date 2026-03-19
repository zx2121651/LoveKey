package com.example.lovekey_clone

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button

class LoveKeyIME : InputMethodService() {

    private lateinit var keyboardView: View

    override fun onCreateInputView(): View {
        // Inflate our custom keyboard layout
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)

        setupButtons()

        return keyboardView
    }

    private fun setupButtons() {
        val btnAiReply = keyboardView.findViewById<Button>(R.id.btnAiReply)
        val btnPersona = keyboardView.findViewById<Button>(R.id.btnPersona)
        val btnLoveQuote1 = keyboardView.findViewById<Button>(R.id.btnLoveQuote1)
        val btnLoveQuote2 = keyboardView.findViewById<Button>(R.id.btnLoveQuote2)
        val btnLoveQuote3 = keyboardView.findViewById<Button>(R.id.btnLoveQuote3)
        val btnDelete = keyboardView.findViewById<Button>(R.id.btnDelete)

        // "AI 帮我回" - Simulate reading last message and generating reply
        btnAiReply.setOnClickListener {
            val inputConnection = currentInputConnection
            if (inputConnection != null) {
                // In a real scenario, you'd try to read surrounding text.
                // For this clone, we just output a generated high-EQ mock reply.
                inputConnection.commitText("我觉得你说的很有道理，那么周末要不要出来喝杯咖啡继续聊？", 1)
            }
        }

        // Persona button - Might just open the main app or change keyboard state
        btnPersona.setOnClickListener {
            val inputConnection = currentInputConnection
            inputConnection?.commitText("【正在使用：贴心暖男 人设】", 1)
        }

        // Quick Quotes
        btnLoveQuote1.setOnClickListener { commitString(btnLoveQuote1.text.toString()) }
        btnLoveQuote2.setOnClickListener { commitString(btnLoveQuote2.text.toString()) }
        btnLoveQuote3.setOnClickListener { commitString(btnLoveQuote3.text.toString()) }

        // Backspace / Delete
        btnDelete.setOnClickListener {
            val inputConnection = currentInputConnection
            inputConnection?.deleteSurroundingText(1, 0)
        }
    }

    private fun commitString(text: String) {
        currentInputConnection?.commitText(text, 1)
    }
}
