package com.example.testapplication.base

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object DispatchersProvider {

    fun provideDispatchers(): IDispatchers {
        return object : IDispatchers {
            override val computation = Dispatchers.Default
            override val io = Dispatchers.IO
        }
    }
}

interface IDispatchers {
    val computation: CoroutineContext
    val io: CoroutineContext
}