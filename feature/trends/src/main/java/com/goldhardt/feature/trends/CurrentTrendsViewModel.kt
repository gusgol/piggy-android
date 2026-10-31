package com.goldhardt.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.feature.trends.domain.ObserveMonthlyTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CurrentTrendsViewModel @Inject constructor(
    private val observeMonthlyTrends: ObserveMonthlyTrendsUseCase,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<CurrentTrendsUiState> =
        selectedMonth
            .flatMapLatest { month ->
                observeMonthlyTrends(month).map { trends ->
                    CurrentTrendsUiState(
                        month = month,
                        isLoading = false,
                        total = trends.total,
                        slices = trends.slices.map {
                            CategorySliceUi(it.categoryId, it.name, it.colorHex, it.total)
                        },
                        categories = trends.categories.map { category ->
                            CategoryGroupUi(
                                id = category.id,
                                name = category.name,
                                icon = category.icon,
                                colorHex = category.colorHex,
                                total = category.total,
                                count = category.items.size,
                                items = category.items.map { item ->
                                    CategoryExpenseItemUi(
                                        name = item.name,
                                        amount = item.amount,
                                        dateLabel = item.dateLabel,
                                    )
                                },
                            )
                        },
                        dayStacks = trends.dayStacks.map { day ->
                            DayStackUi(
                                day = day.day,
                                pieces = day.pieces.map { piece ->
                                    DayPieceUi(
                                        categoryId = piece.categoryId,
                                        amount = piece.amount,
                                        colorHex = piece.colorHex,
                                    )
                                },
                            )
                        },
                    )
                }.catch { error ->
                    emit(CurrentTrendsUiState(month = month, isLoading = false, error = error.message))
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CurrentTrendsUiState(isLoading = true),
            )

    fun setMonth(month: YearMonth) {
        selectedMonth.value = month
    }
}

data class CurrentTrendsUiState(
    val month: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true,
    val total: Double = 0.0,
    val slices: List<CategorySliceUi> = emptyList(),
    val categories: List<CategoryGroupUi> = emptyList(),
    val dayStacks: List<DayStackUi> = emptyList(),
    val error: String? = null,
)

data class CategorySliceUi(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String?,
    val amount: Double,
)

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
