package com.goldhardt.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.feature.trends.domain.DayStack
import com.goldhardt.feature.trends.domain.Slice
import com.goldhardt.feature.trends.domain.ObserveMonthlyTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TrendsViewModel @Inject constructor(
    private val observeMonthlyTrends: ObserveMonthlyTrendsUseCase
) : ViewModel() {

    private val expenseDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<TrendsUiState> =
        selectedMonth
            .flatMapLatest { month ->
                observeMonthlyTrends(month).map { trends ->
                    TrendsUiState(
                        month = month,
                        isLoading = false,
                        total = trends.total,
                        slices = trends.slices,
                        categories = trends.categories.map { cat ->
                            CategoryGroupUi(
                                id = cat.id,
                                name = cat.name,
                                icon = cat.icon,
                                colorHex = cat.colorHex,
                                total = cat.total,
                                items = cat.items.map { item ->
                                    CategoryExpenseItemUi(
                                        name = item.name,
                                        amount = item.amount,
                                        dateLabel = expenseDateFormatter.format(item.date),
                                    )
                                }
                            )
                        },
                        dayStacks = trends.dayStacks
                    )
                }.catch {
                    emit(TrendsUiState(month = month, isLoading = false, error = "Couldn't load trends. Please try again."))
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
    val slices: List<Slice> = emptyList(),
    val categories: List<CategoryGroupUi> = emptyList(),
    val dayStacks: List<DayStack> = emptyList(),
    val error: String? = null,
)

data class CategoryGroupUi(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String?,
    val total: Double,
    val items: List<CategoryExpenseItemUi>,
)

data class CategoryExpenseItemUi(
    val name: String,
    val amount: Double,
    val dateLabel: String,
)
