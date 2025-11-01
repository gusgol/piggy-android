package com.goldhardt.feature.expenses.import.repository

import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.repository.ExpenseRepository
import com.goldhardt.feature.expenses.import.model.ImportExpenseItem
import com.goldhardt.feature.expenses.import.model.ParseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.time.ZoneId
import javax.inject.Inject

/**
 * Repository for importing expenses from CSV files.
 */
class ImportExpenseRepository @Inject constructor(
    private val csvParser: CsvExpenseParser,
    private val expenseRepository: ExpenseRepository,
) {

    /**
     * Parse CSV stream and return a flow of parse results.
     */
    fun parseStream(inputStream: InputStream): Flow<ParseResult> {
        return csvParser.parseStream(inputStream)
    }

    /**
     * Save confirmed expenses to Firestore in batches.
     * 
     * @param userId Current user ID
     * @param items List of confirmed import items
     * @param defaultCategoryId Fallback category ID for items without a selected category
     * @return Pair of (saved count, skipped count)
     */
    suspend fun saveExpenses(
        userId: String,
        items: List<ImportExpenseItem>,
        defaultCategoryId: String
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val confirmedItems = items.filter { it.isConfirmed && !it.isSkipped }
        val skippedCount = items.size - confirmedItems.size
        var savedCount = 0
        val errors = mutableListOf<Pair<Int, String>>()

        // Process in batches of 50 for performance
        confirmedItems.chunked(50).forEach { batch ->
            batch.forEach { item ->
                try {
                    val categoryId = item.selectedCategoryId ?: defaultCategoryId
                    val formData = ExpenseFormData(
                        name = item.editedDescription,
                        amount = item.parsedExpense.amount,
                        date = item.parsedExpense.date
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant(),
                        categoryId = categoryId,
                        isFixed = false
                    )
                    expenseRepository.addExpense(userId, formData)
                    savedCount++
                } catch (e: Exception) {
                    // Collect error for this row
                    errors.add(item.parsedExpense.rowNumber to (e.message ?: "Unknown error"))
                    // Log error for debugging
                    android.util.Log.e("ImportExpenseRepository", 
                        "Failed to save expense from row ${item.parsedExpense.rowNumber}", e)
                }
            }
        }

        // If there were errors, throw exception with details
        if (errors.isNotEmpty() && savedCount == 0) {
            throw Exception("Failed to save all expenses. First error: ${errors.first().second}")
        } else if (errors.isNotEmpty()) {
            // Some succeeded, some failed - log but don't throw
            android.util.Log.w("ImportExpenseRepository", 
                "Saved $savedCount expenses, but ${errors.size} failed to save")
        }

        savedCount to skippedCount
    }
}
