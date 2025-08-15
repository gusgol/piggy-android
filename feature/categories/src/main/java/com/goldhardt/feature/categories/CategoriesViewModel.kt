package com.goldhardt.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.data.model.Category
import com.goldhardt.feature.categories.domain.ObserveUserCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val observeUserCategories: ObserveUserCategoriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeCategories()
    }

    private fun observeCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            observeUserCategories()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { categories ->
                    _uiState.update { it.copy(isLoading = false, categories = categories, error = null) }
                }
        }
    }
}
