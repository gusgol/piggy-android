package com.goldhardt.feature.expenses.import.repository

import com.goldhardt.feature.expenses.import.model.ParseResult
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class CsvExpenseParserTest {

    private lateinit var parser: CsvExpenseParser

    @Before
    fun setup() {
        parser = CsvExpenseParser()
    }

    @Test
    fun `parseStream with valid CSV returns success results`() = runTest {
        val csv = """
            data,lançamento,valor
            2025-10-30,EC *PACCOPET,200.71
            2025-10-30,ARMAZEM DO PAULINHO,35.5
            2025-10-28,DL*GOOGLE Google,96.99
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        assertEquals(3, results.size)
        assertTrue(results.all { it is ParseResult.Success })

        val firstExpense = (results[0] as ParseResult.Success).expense
        assertEquals("EC *PACCOPET", firstExpense.description)
        assertEquals(200.71, firstExpense.amount, 0.001)
        assertEquals("2025-10-30", firstExpense.date.toString())
        assertEquals(2, firstExpense.rowNumber)
    }

    @Test
    fun `parseStream with invalid header returns error`() = runTest {
        val csv = """
            date,description,amount
            2025-10-30,EC *PACCOPET,200.71
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        assertEquals(1, results.size)
        assertTrue(results[0] is ParseResult.Error)
        val error = results[0] as ParseResult.Error
        assertEquals(0, error.rowNumber)
        assertTrue(error.message.contains("Invalid CSV format"))
    }

    @Test
    fun `parseStream trims and normalizes whitespace in description`() = runTest {
        val csv = """
            data,lançamento,valor
            2025-10-30,  STORE   WITH   SPACES  ,100.0
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        assertEquals(1, results.size)
        val expense = (results[0] as ParseResult.Success).expense
        assertEquals("STORE WITH SPACES", expense.description)
    }

    @Test
    fun `parseStream with invalid date format emits error`() = runTest {
        val csv = """
            data,lançamento,valor
            2025-10-30,Valid Row,100.0
            30-10-2025,Invalid Date Format,50.0
            2025-10-31,Another Valid Row,75.0
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        // Should have 2 success results and 1 error
        val successResults = results.filterIsInstance<ParseResult.Success>()
        val errorResults = results.filterIsInstance<ParseResult.Error>()
        
        assertEquals(2, successResults.size)
        assertEquals(1, errorResults.size)
        assertEquals(3, errorResults[0].rowNumber)
        assertTrue(errorResults[0].message.contains("Invalid date format"))
    }

    @Test
    fun `parseStream with invalid amount format emits error`() = runTest {
        val csv = """
            data,lançamento,valor
            2025-10-30,Valid Row,100.0
            2025-10-30,Invalid Amount,not-a-number
            2025-10-31,Another Valid Row,75.5
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        val successResults = results.filterIsInstance<ParseResult.Success>()
        val errorResults = results.filterIsInstance<ParseResult.Error>()
        
        assertEquals(2, successResults.size)
        assertEquals(1, errorResults.size)
        assertTrue(errorResults[0].message.contains("Invalid amount format"))
    }

    @Test
    fun `parseStream handles decimal values correctly`() = runTest {
        val csv = """
            data,lançamento,valor
            2025-10-30,Integer Amount,107.0
            2025-10-30,Decimal Amount,35.5
            2025-10-28,Precise Decimal,96.99
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        val expenses = results.filterIsInstance<ParseResult.Success>().map { it.expense }
        assertEquals(107.0, expenses[0].amount, 0.001)
        assertEquals(35.5, expenses[1].amount, 0.001)
        assertEquals(96.99, expenses[2].amount, 0.001)
    }

    @Test
    fun `parseStream handles empty CSV gracefully`() = runTest {
        val csv = """
            data,lançamento,valor
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        assertEquals(0, results.size)
    }

    @Test
    fun `parseStream with UTF-8 characters works correctly`() = runTest {
        val csv = """
            data,lançamento,valor
            2025-10-30,Café São Paulo,50.0
            2025-10-30,Açougue Três Irmãos,100.0
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8))
        val results = parser.parseStream(inputStream).toList()

        assertEquals(2, results.size)
        val expenses = results.filterIsInstance<ParseResult.Success>().map { it.expense }
        assertEquals("Café São Paulo", expenses[0].description)
        assertEquals("Açougue Três Irmãos", expenses[1].description)
    }
}
