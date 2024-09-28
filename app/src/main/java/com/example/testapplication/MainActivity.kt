package com.example.testapplication

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var resultTv: TextView
    private lateinit var amountEt: EditText
    private lateinit var startB: Button
    private lateinit var scrollV: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        resultTv = findViewById(R.id.resultTv)
        amountEt = findViewById(R.id.amountEt)
        startB = findViewById(R.id.startB)
        scrollV = findViewById(R.id.scrollV)
        amountEt.setInputType(InputType.TYPE_CLASS_NUMBER)
    }

    override fun onStart() {
        super.onStart()

        viewModel.calculationsFlow bindTo { result ->
            resultTv.text = result
            scrollV.fullScroll(ScrollView.FOCUS_DOWN)
        }

        viewModel.errorFlow bindTo { errorMessage ->
            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT)
                .show()
        }

        startB.setOnClickListener {
            viewModel.onButtonClick(amountEt.text.toString())
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private infix fun <T> Flow<T>.bindTo(block: (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect {
                    try {
                        block(it)
                    } catch (t: Throwable) {
                        Log.e("MainActivityCollect", t.message.toString())
                    }
                }
            }
        }
    }
}
