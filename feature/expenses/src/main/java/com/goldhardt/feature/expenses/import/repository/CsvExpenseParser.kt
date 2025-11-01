package com.goldhardt.feature.expenses.import.repository

import com.goldhardt.feature.expenses.import.model.ParsedExpense
import com.goldhardt.feature.expenses.import.model.ParseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID
import javax.inject.Inject

/**
 * Parser for expense CSV files in the exact format:
 * - Encoding: UTF-8
 * - Delimiter: comma (,)
 * - Header: data,lançamento,valor
 * - Date format: YYYY-MM-DD
 * - Value format: decimal with dot separator
 */
class CsvExpenseParser @Inject constructor() {

    companion object {
        private const val HEADER_DATE = "data"
        private const val HEADER_DESCRIPTION = "lançamento"
        private const val HEADER_AMOUNT = "valor"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    /**
     * Parse CSV from InputStream and emit parsed expenses as a Flow.
     * Validates header and emits ParseResult for each row.
     */
    fun parseStream(inputStream: InputStream): Flow<ParseResult> = flow {
        try {
            val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()

            CSVParser(reader, csvFormat).use { parser ->
                // Validate headers
                val headers = parser.headerNames
                if (!validateHeaders(headers)) {
                    emit(
                        ParseResult.Error(
                            rowNumber = 0,
                            message = "Invalid CSV format. Expected header: data,lançamento,valor"
                        )
                    )
                    return@flow
                }

                // Parse each record
                parser.forEachIndexed { index, record ->
                    val rowNumber = index + 2 // +2 because index is 0-based and header is row 1
                    try {
                        val date = parseDate(record.get(HEADER_DATE), rowNumber)
                        val description = parseDescription(record.get(HEADER_DESCRIPTION))
                        val amount = parseAmount(record.get(HEADER_AMOUNT), rowNumber)

                        if (date != null && amount != null) {
                            val expense = ParsedExpense(
                                id = UUID.randomUUID().toString(),
                                date = date,
                                description = description,
                                amount = amount,
                                rowNumber = rowNumber
                            )
                            emit(ParseResult.Success(expense))
                        } else {
                            // Emit error for invalid date or amount
                            val errorMessage = when {
                                date == null && amount == null -> "Invalid date and amount format"
                                date == null -> "Invalid date format. Expected YYYY-MM-DD"
                                else -> "Invalid amount format. Expected decimal number"
                            }
                            emit(
                                ParseResult.Error(
                                    rowNumber = rowNumber,
                                    message = errorMessage
                                )
                            )
                        }
                    } catch (e: Exception) {
                        emit(
                            ParseResult.Error(
                                rowNumber = rowNumber,
                                message = "Error parsing row: ${e.message}"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            emit(
                ParseResult.Error(
                    rowNumber = 0,
                    message = "Failed to read CSV file: ${e.message}"
                )
            )
        }
    }

    /**
     * Validate that headers match the expected format.
     */
    private fun validateHeaders(headers: Set<String>): Boolean {
        return headers.contains(HEADER_DATE) &&
                headers.contains(HEADER_DESCRIPTION) &&
                headers.contains(HEADER_AMOUNT)
    }

    /**
     * Parse date from string in YYYY-MM-DD format.
     */
    private fun parseDate(dateStr: String, rowNumber: Int): LocalDate? {
        return try {
            LocalDate.parse(dateStr.trim(), DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null.also {
                // Error will be handled by caller
            }
        }
    }

    /**
     * Parse and normalize description text.
     */
    private fun parseDescription(description: String): String {
        return description.trim().replace("\\s+".toRegex(), " ")
    }

    /**
     * Parse amount from string (decimal with dot separator).
     */
    private fun parseAmount(amountStr: String, rowNumber: Int): Double? {
        return try {
            amountStr.trim().toDouble()
        } catch (e: NumberFormatException) {
            null.also {
                // Error will be handled by caller
            }
        }
    }
}
