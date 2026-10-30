package com.goldhardt.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.domain.model.Category
import com.goldhardt.feature.categories.domain.AddCategoryUseCase
import com.goldhardt.feature.categories.domain.DeleteCategoryUseCase
import com.goldhardt.feature.categories.domain.ObserveUserCategoriesUseCase
import com.goldhardt.feature.categories.domain.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    observeUserCategories: ObserveUserCategoriesUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : ViewModel() {

    private val mutationError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> =
        observeUserCategories()
            .combine(mutationError) { categories, error ->
                UiState(isLoading = false, categories = categories, error = error)
            }
            .catch {
                emit(UiState(isLoading = false, error = "Couldn't load categories. Please try again."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState(isLoading = true)
            )

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                updateCategoryUseCase(category)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutationError.value = "Couldn't update category. Please try again."
            }
        }
    }

    fun addCategory(name: String, icon: String, color: String) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                addCategoryUseCase(name, icon, color)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutationError.value = "Couldn't add category. Please try again."
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                deleteCategoryUseCase(categoryId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutationError.value = "Couldn't delete category. Please try again."
            }
        }
    }
}
