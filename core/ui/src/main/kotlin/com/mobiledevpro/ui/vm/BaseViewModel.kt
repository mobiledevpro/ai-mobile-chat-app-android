package com.mobiledevpro.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiledevpro.ui.state.UIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

abstract class BaseViewModel<State : UIState> : ViewModel() {

    abstract val initialState: State

    protected open val scope: CoroutineScope = viewModelScope

    abstract fun observeState(): Flow<State>

    val uiState: StateFlow<State> by lazy {
        observeState()
            .onCompletion {
                Log.d("debug", "${this@BaseViewModel::class.simpleName}: state onCompletion")
            }
            .onStart {
                Log.d("debug", "${this@BaseViewModel::class.simpleName}: state onStart")
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = scope,
                initialValue = initialState,
                started = SharingStarted.WhileSubscribed(
                    stopTimeoutMillis = 0,
                    replayExpirationMillis = 9_000L, // 6
                ),
            )
    }
}