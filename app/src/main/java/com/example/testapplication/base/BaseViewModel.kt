package com.example.testapplication.base

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class VMScopes(dispatchers: IDispatchers) {
    val onClearedScope = CoroutineScope(dispatchers.computation + SupervisorJob())
}

open class BaseViewModel(private val vmScopes: VMScopes) : ViewModel() {

    open val vmTag: String = "BaseVM"

    protected fun launch(
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            onError(throwable.message.toString())
        }
        val scope = vmScopes.onClearedScope.plus(coroutineExceptionHandler)
        return scope.launch {
            block()
        }
    }

    private fun onError(message: String) {
        Log.e(vmTag, message)
    }

    override fun onCleared() {
        super.onCleared()
        vmScopes.onClearedScope.cancel()
    }
}