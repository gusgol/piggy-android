package com.goldhardt.piggy.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.repository.CategoryRepository
import com.goldhardt.core.data.repository.ExpenseRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant

@HiltWorker
class SaveExpenseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val amount = inputData.getDouble(KEY_AMOUNT, Double.NaN)
        val description = inputData.getString(KEY_DESCRIPTION)
        if (amount.isNaN() || description.isNullOrBlank()) {
            Log.w(TAG, "Missing data; amount=$amount, description=$description")
            return Result.failure()
        }

        val user = authRepository.currentUser
        if (user == null) {
            Log.w(TAG, "No authenticated user; cannot save expense")
            return Result.retry()
        }

        return try {
            val categories = categoryRepository.getCategories(user.id)

            /**
             * TODO Getting the first one for now, but ideally we can add some intelligence here.
             */
            val categoryId = categories.firstOrNull()?.id
            if (categoryId == null) {
                Log.w(TAG, "No categories available for user=${user.id}; cannot save expense")
                return Result.retry()
            }

            val form = ExpenseFormData(
                name = description, // map description to title/name
                amount = amount,
                date = Instant.now(),
                categoryId = categoryId,
                isFixed = false,
            )

            val expense = expenseRepository.addExpense(user.id, form)
            Log.i(TAG, "Saved expense: ${expense.name} - ${expense.amount} in category ${expense.categoryId}")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to save expense", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SaveExpenseWorker"
        const val KEY_AMOUNT = "input_amount"
        const val KEY_DESCRIPTION = "input_description"
    }
}
