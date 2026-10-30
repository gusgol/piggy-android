package com.goldhardt.feature.expenses.import_expenses.domain

import com.goldhardt.feature.expenses.di.IoDispatcher
import java.io.InputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Parses CSV files in the format:
 * date,name,amount
 * 2025-10-30,EC *PACCOPET,200.71
 */
class CsvParser @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun parse(openStream: () -> InputStream?): CsvParseResult = withContext(ioDispatcher) {
        try {
            val inputStream = openStream()
                ?: return@withContext CsvParseResult.Error("Failed to open CSV file")
            val expenses = mutableListOf<ImportedExpense>()
            val lines = inputStream.bufferedReader().use { it.readLines() }

            if (lines.isEmpty()) {
                return@withContext CsvParseResult.Error("CSV file is empty")
            }

            // Skip header (first line)
            val dataLines = lines.drop(1)

            for ((index, line) in dataLines.withIndex()) {
                if (line.isBlank()) continue

                try {
                    val expense = parseLine(line)
                    expenses.add(expense)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    return@withContext CsvParseResult.Error(
                        "Error parsing line ${index + 2}: ${e.message}"
                    )
                }
            }

            if (expenses.isEmpty()) {
                CsvParseResult.Error("No valid expenses found in CSV")
            } else {
                CsvParseResult.Success(expenses)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            CsvParseResult.Error("Unable to read the CSV file.")
        }
    }

    private fun parseLine(line: String): ImportedExpense {
        // Split by comma, handling potential CSV edge cases
        val parts = line.split(",")

        if (parts.size < 3) {
            throw IllegalArgumentException("Invalid CSV format. Expected 3 columns, found ${parts.size}")
        }

        // Parse date (format: YYYY-MM-DD)
        val dateString = parts[0].trim()
        val date = try {
            LocalDate.parse(dateString, dateFormatter)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        } catch (_: Exception) {
            throw IllegalArgumentException("Invalid date format: $dateString")
        }

        // Parse name
        val name = parts[1].trim()
        if (name.isBlank()) {
            throw IllegalArgumentException("Expense name cannot be empty")
        }

        // Parse amount
        val amountString = parts[2].trim()
        val amount = try {
            amountString.toDouble()
        } catch (_: Exception) {
            throw IllegalArgumentException("Invalid amount: $amountString")
        }

        return ImportedExpense(
            name = name,
            amount = amount,
            date = date,
            originalLine = line
        )
    }
}
