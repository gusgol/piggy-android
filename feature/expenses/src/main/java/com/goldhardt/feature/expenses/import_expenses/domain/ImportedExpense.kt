package com.goldhardt.feature.expenses.import_expenses.domain

import java.time.Instant

/**
 * Represents a parsed expense from CSV that needs category assignment.
 */
data class ImportedExpense(
    val name: String,
    val amount: Double,
    val date: Instant,
    val categoryId: String? = null,
    val isFixed: Boolean = false,
    val originalLine: String, // For error tracking
)

/**
 * Result of parsing a CSV file.
 */
sealed class CsvParseResult {
    data class Success(val expenses: List<ImportedExpense>) : CsvParseResult()
    data class Error(val message: String) : CsvParseResult()
}

