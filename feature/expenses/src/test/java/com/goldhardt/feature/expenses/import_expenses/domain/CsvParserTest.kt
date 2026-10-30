package com.goldhardt.feature.expenses.import_expenses.domain

import java.io.ByteArrayInputStream
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvParserTest {

    private val parser = CsvParser()

    @Test
    fun `parse returns Success for valid CSV lines`() = runTest {
        val csv = """
            date,name,amount
            2025-10-30,Coffee Shop,12.50
            2025-10-31,Grocery Store,105.99
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result is CsvParseResult.Success)
        val expenses = (result as CsvParseResult.Success).expenses
        assertEquals(2, expenses.size)
        assertEquals("Coffee Shop", expenses[0].name)
        assertEquals(12.50, expenses[0].amount, 0.001)
        assertEquals("Grocery Store", expenses[1].name)
        assertEquals(105.99, expenses[1].amount, 0.001)
    }

    @Test
    fun `parse returns Error when CSV is empty`() = runTest {
        val result = parser.parse(ByteArrayInputStream(ByteArray(0)))

        assertTrue(result is CsvParseResult.Error)
        assertEquals("CSV file is empty", (result as CsvParseResult.Error).message)
    }

    @Test
    fun `parse returns Error when date format is invalid`() = runTest {
        val csv = """
            date,name,amount
            30/10/2025,Coffee Shop,12.50
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result is CsvParseResult.Error)
        assertTrue((result as CsvParseResult.Error).message.contains("Invalid date format"))
    }

    @Test
    fun `parse returns Error when amount is invalid`() = runTest {
        val csv = """
            date,name,amount
            2025-10-30,Coffee Shop,not-a-number
        """.trimIndent()

        val result = parser.parse(ByteArrayInputStream(csv.toByteArray()))

        assertTrue(result is CsvParseResult.Error)
        assertTrue((result as CsvParseResult.Error).message.contains("Invalid amount"))
    }
}

