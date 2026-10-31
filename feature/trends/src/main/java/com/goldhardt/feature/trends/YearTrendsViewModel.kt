package com.goldhardt.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.feature.trends.domain.ObserveYearlyTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class YearTrendsViewModel @Inject constructor(
    observeYearlyTrends: ObserveYearlyTrendsUseCase,
) : ViewModel() {

    private val selectedCategoryId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<YearTrendsUiState> =
        combine(observeYearlyTrends(YearMonth.now()), selectedCategoryId) { trends, categoryId ->
            YearTrendsUiState(
                isLoading = false,
                selectedCategoryId = categoryId,
                months = trends.months.map { month ->
                    YearlyMonthUi(
                        month = month.month,
                        total = month.total,
                        categories = month.categories.map { category ->
                            MonthlyCategorySpendUi(
                                id = category.id,
                                name = category.name,
                                colorHex = category.colorHex,
                                total = category.total,
                            )
                        },
                    )
                },
            )
        }
            .catch { error ->
                emit(YearTrendsUiState(isLoading = false, error = error.message))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = YearTrendsUiState(),
            )

    fun setCategory(categoryId: String?) {
        selectedCategoryId.value = categoryId
    }
}

data class YearTrendsUiState(
    val isLoading: Boolean = true,
    val selectedCategoryId: String? = null,
    val months: List<YearlyMonthUi> = emptyList(),
    val error: String? = null,
)

data class YearlyMonthUi(
    val month: YearMonth,
    val total: Double,
    val categories: List<MonthlyCategorySpendUi>,
)

data class MonthlyCategorySpendUi(
    val id: String,
    val name: String,
    val colorHex: String?,
    val total: Double,
)
