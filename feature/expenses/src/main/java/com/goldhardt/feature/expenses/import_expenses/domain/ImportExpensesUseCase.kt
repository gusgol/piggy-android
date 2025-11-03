package com.goldhardt.feature.expenses.import_expenses.domain

import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.repository.ExpenseRepository
import javax.inject.Inject

/**
 * Use case for batch importing expenses with progress tracking.
 */
class ImportExpensesUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(
        expenses: List<ImportedExpense>,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): ImportResult {
        val user = authRepository.currentUser
            ?: return ImportResult.Error("User must be signed in")

        val successfulImports = mutableListOf<String>()
        val failures = mutableListOf<ImportFailure>()

        expenses.forEachIndexed { index, importedExpense ->
            try {
                // Validate that category is assigned
                if (importedExpense.categoryId == null) {
                    failures.add(ImportFailure(
                        expense = importedExpense,
                        reason = "Category not assigned"
                    ))
                } else {
                    val formData = ExpenseFormData(
                        name = importedExpense.name,
                        amount = importedExpense.amount,
                        date = importedExpense.date,
                        categoryId = importedExpense.categoryId,
                        isFixed = importedExpense.isFixed
                    )

                    val expense = expenseRepository.addExpense(user.id, formData)
                    successfulImports.add(expense.id)
                }
            } catch (e: Exception) {
                failures.add(ImportFailure(
                    expense = importedExpense,
                    reason = e.message ?: "Unknown error"
                ))
            }

            onProgress(index + 1, expenses.size)
        }

        return if (failures.isEmpty()) {
            ImportResult.Success(successfulImports.size)
        } else if (successfulImports.isEmpty()) {
            ImportResult.Error("All imports failed")
        } else {
            ImportResult.PartialSuccess(
                successCount = successfulImports.size,
                failures = failures
            )
        }
    }
}

sealed class ImportResult {
    data class Success(val count: Int) : ImportResult()
    data class PartialSuccess(
        val successCount: Int,
        val failures: List<ImportFailure>
    ) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

data class ImportFailure(
    val expense: ImportedExpense,
    val reason: String
)

