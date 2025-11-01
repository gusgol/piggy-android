package com.goldhardt.feature.expenses.import.model

import java.time.LocalDate

/**
 * Represents an expense parsed from CSV, ready for user review and confirmation.
 */
data class ParsedExpense(
    val id: String, // Temporary ID for UI tracking
    val date: LocalDate,
    val description: String,
    val amount: Double,
    val rowNumber: Int,
    val parseError: String? = null,
)

/**
 * UI state for a parsed expense awaiting confirmation.
 */
data class ImportExpenseItem(
    val parsedExpense: ParsedExpense,
    val editedDescription: String = parsedExpense.description,
    val selectedCategoryId: String? = null,
    val isConfirmed: Boolean = false,
    val isSkipped: Boolean = false,
)

/**
 * Result of parsing a single CSV row.
 */
sealed class ParseResult {
    data class Success(val expense: ParsedExpense) : ParseResult()
    data class Error(val rowNumber: Int, val message: String) : ParseResult()
}
