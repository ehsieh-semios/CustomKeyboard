package com.semios.customkeyboard

import android.content.Context
import android.os.CountDownTimer
import android.text.InputType
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.semios.hexkeyboard.Keyboard
import com.semios.hexkeyboard.KeyboardView

class HexKeyboard(keyboardView: KeyboardView) : KeyboardView.OnKeyboardActionListener {
    private var editTexts: Set<EditText> = emptySet()
    private var mKeyboardView: KeyboardView? = keyboardView

    init {
        val keyboard = Keyboard(keyboardView.context, R.xml.hexkeyboard)
        mKeyboardView!!.keyboard = keyboard
        mKeyboardView!!.setOnKeyboardActionListener(this)
        mKeyboardView!!.isPreviewEnabled = false
    }

    override fun onPress(i: Int) {}
    override fun onRelease(i: Int) {}
    override fun onKey(i: Int, ints: IntArray) {
        val editText = editTexts.firstOrNull { it.isFocused } ?:return
        // Get the EditText and its Editable
        val editable = editText.text
        val start = editText.selectionStart
        when (i) {
            CODE_CANCEL -> hideHexKeyboard()
            CODE_DELETE -> if (editable != null && start > 0) editable.delete(start - 1, start)
            CODE_CLEAR -> editable?.clear()
            CODE_LEFT -> if (start > 0) editText.setSelection(start - 1)
            CODE_RIGHT -> if (start < editText.length()) editText.setSelection(start + 1)
            CODE_001C2C -> editable!!.insert(start, "001C2C")
            else -> editable!!.insert(start, i.toChar().toString())
        }
        // send event when max mac length has been reached
        // this is for mac only
        if (editText.text.length == MAC_MAX_LENGTH) {
            // push notifications to activities
        }
    }

    override fun onText(charSequence: CharSequence) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
    fun registerEditText(ed: EditText?) {
        val editText = ed ?: return
        editText.inputType = EditorInfo.TYPE_NULL
        // Make the custom keyboard appear
        editText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showHexKeyboard(v)
            } else if(editTexts.none { it.isFocused }){
                hideHexKeyboard()
            }
        }
        editText.setOnClickListener { v -> showHexKeyboard(v) }
        // Disable standard keyboard hard way
        editText.setOnTouchListener { v, event ->
            val view = v as EditText
            val str = view.text.toString()
            val index = str.length
            val inType = view.inputType // Backup the input type
            view.inputType = InputType.TYPE_NULL // Disable standard keyboard
            view.onTouchEvent(event) // Call native handler
            view.inputType = inType // Restore input type
            view.setSelection(index)
            true
        }
        // Disable spell check (hex strings look like words to Android)
        editText.inputType = editText.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        addEditText(editText)
    }

    private fun addEditText(editText: EditText) {
        editTexts = editTexts + editText
    }

    fun unregisterEditText() {
        editTexts.forEach {
            it.setOnClickListener(null)
            it.setOnTouchListener(null)
        }
        editTexts = emptySet()
    }

    private fun hideHexKeyboard() {
        mKeyboardView?.visibility = View.GONE
        mKeyboardView?.isEnabled = false
    }

    private fun showHexKeyboard(v: View?) {
        if (v != null) {
            val imm = v.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
        // Let the hex keyboard shows up slower for better UI effect
        object : CountDownTimer(500, 500) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                mKeyboardView!!.isEnabled = true
                mKeyboardView!!.visibility = View.VISIBLE
            }
        }.start()
    }

    val isHexKeyboardVisible: Boolean
        get() = mKeyboardView!!.visibility == View.VISIBLE

    companion object {
        private const val MAC_MAX_LENGTH = 16
        private const val CODE_DELETE = -5 // Keyboard.KEYCODE_DELETE
        private const val CODE_CANCEL = -3 // Keyboard.KEYCODE_CANCEL
        private const val CODE_001C2C = 55001
        private const val CODE_LEFT = 55002
        private const val CODE_RIGHT = 55003
        private const val CODE_CLEAR = 55006
    }
}