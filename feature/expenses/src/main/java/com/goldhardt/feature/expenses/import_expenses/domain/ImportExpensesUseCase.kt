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

        val validForms = mutableListOf<ExpenseFormData>()
        val failures = mutableListOf<ImportFailure>()

        expenses.forEach { importedExpense ->
            if (importedExpense.categoryId == null) {
                failures.add(
                    ImportFailure(
                        expense = importedExpense,
                        reason = "Category not assigned"
                    )
                )
            } else {
                validForms.add(
                    ExpenseFormData(
                        name = importedExpense.name,
                        amount = importedExpense.amount,
                        date = importedExpense.date,
                        categoryId = importedExpense.categoryId,
                        isFixed = importedExpense.isFixed
                    )
                )
            }
        }

        if (validForms.isEmpty()) {
            return ImportResult.Error("All imports failed")
        }

        return try {
            val created = expenseRepository.addExpenses(user.id, validForms)
            onProgress(expenses.size, expenses.size)
            if (failures.isEmpty()) {
                ImportResult.Success(created.size)
            } else {
                ImportResult.PartialSuccess(
                    successCount = created.size,
                    failures = failures
                )
            }
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Batch import failed")
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

