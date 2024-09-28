package com.example.testapplication

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.example.testapplication.base.BaseViewModel
import com.example.testapplication.base.DispatchersProvider
import com.example.testapplication.base.VMScopes
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MainViewModel :
    BaseViewModel(VMScopes(DispatchersProvider.provideDispatchers())) {

    override val vmTag = "MainVM"

    private val _calculationsState = MutableStateFlow(EMPTY_STRING)

    val calculationsFlow = flow {
        while (true) {
            delay(100)
            emit(Unit)
        }
    }.map {
        _calculationsState.value
    }.distinctUntilChanged()

    private var job: Job? = null

    private val _errorChannel =
        Channel<String>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val errorFlow: Flow<String> = _errorChannel.receiveAsFlow()

    fun onButtonClick(text: String) {
        if (text.isDigitsOnly()) {
            val flowPool = createFlowPool(text.toInt())
            job?.cancel()
            job = launch {
                _calculationsState.emit(EMPTY_STRING)
                var prevAmount = 0
                flowPool.forEach { flow ->
                    val result = flow.first()
                    val currentAmount = prevAmount + result
                    _calculationsState.emit(_calculationsState.value + currentAmount.toString() + "\n")
                    prevAmount = currentAmount
                }
            }

        } else {
            onTypeError()
        }
    }

    private fun createFlowPool(rng: Int): List<Flow<Int>> {
        val list = mutableListOf<Flow<Int>>()
        for (i in 0 until rng) {
            list.add(createDelayedEmitFlow(i))
        }
        return list
    }

    private fun createDelayedEmitFlow(index: Int) = flow {
        val sum = index + 1
        val timeout = sum * 100
        Log.d(vmTag, "index: $index, sum: $sum, timeout: $timeout")
        delay(timeout.toLong())
        emit(sum)
    }

    private fun onTypeError() {
        launch {
            _errorChannel.send("You can enter only digits")
        }
    }

    private companion object {
        private const val EMPTY_STRING = ""
    }

}