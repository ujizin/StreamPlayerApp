package com.codandotv.streamplayerapp.feature_list_streams.list.presentation.screens

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codandotv.streamplayerapp.core_networking.handleError.catchFailure
import com.codandotv.streamplayerapp.feature_list_streams.list.domain.ListStreamUseCase
import com.codandotv.streamplayerapp.feature_list_streams.list.presentation.ListStreamUimodel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListStreamViewModel(
    private val uiModel: ListStreamUimodel,
    private val useCase: ListStreamUseCase,
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(
        ListStreamsUIState(
            carousels = emptyList(),
            isLoading = false
        )
    )
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = _uiState.value
    )

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            if (uiState.value.carousels.isEmpty()) {
                useCase.getMovies()
                    .onStart { onLoading() }
                    .catchFailure {
                        println(">>>> ${it.errorMessage}")
                    }
                    .onCompletion { loaded() }
                    .collect { listStream ->
                        _uiState.update {
                            uiModel.convertToCardContent(listStream)
                        }
                    }
            }
        }
    }

    private fun loaded() {
        this._uiState.update {
            it.copy(isLoading = false)
        }
    }

    private fun onLoading() {
        this._uiState.update {
            it.copy(isLoading = true)
        }
    }
}