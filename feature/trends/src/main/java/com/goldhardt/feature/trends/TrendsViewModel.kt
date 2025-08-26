package com.goldhardt.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.feature.trends.domain.ObserveMonthlyTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TrendsViewModel @Inject constructor(
    private val observeMonthlyTrends: ObserveMonthlyTrendsUseCase
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<TrendsUiState> =
        selectedMonth
            .flatMapLatest { month ->
                observeMonthlyTrends(month).map { trends ->
                    TrendsUiState(
                        month = month,
                        isLoading = false,
                        total = trends.total,
                        slices = trends.slices.map { CategorySliceUi(it.categoryId, it.name, it.colorHex, it.total) },
                        categories = trends.categories.map { cat ->
                            CategoryGroupUi(
                                id = cat.id,
                                name = cat.name,
                                icon = cat.icon,
                                colorHex = cat.colorHex,
                                total = cat.total,
                                count = cat.items.size,
                                items = cat.items.map { item ->
                                    CategoryExpenseItemUi(
                                        name = item.name,
                                        amount = item.amount,
                                        dateLabel = item.dateLabel,
                                    )
                                }
                            )
                        },
                        dayStacks = trends.dayStacks.map { day ->
                            DayStackUi(
                                day = day.day,
                                pieces = day.pieces.map { p -> DayPieceUi(categoryId = p.categoryId, amount = p.amount, colorHex = p.colorHex) }
                            )
                        }
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TrendsUiState(isLoading = true)
            )

    fun setMonth(month: YearMonth) { selectedMonth.value = month }
}

// UI state + UI models

data class TrendsUiState(
    val month: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val total: Double = 0.0,
    val slices: List<CategorySliceUi> = emptyList(),
    val categories: List<CategoryGroupUi> = emptyList(),
    val dayStacks: List<DayStackUi> = emptyList(),
)

data class CategorySliceUi(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String?,
    val amount: Double,
)

// Daily stacked bars UI models

data class DayStackUi(
    val day: Int,
    val pieces: List<DayPieceUi>,
)

data class DayPieceUi(
    val categoryId: String,
    val amount: Double,
    val colorHex: String?,
)

data class CategoryGroupUi(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String?,
    val total: Double,
    val count: Int,
    val items: List<CategoryExpenseItemUi>,
)

data class CategoryExpenseItemUi(
    val name: String,
    val amount: Double,
    val dateLabel: String,
)
