package com.goldhardt.piggy.notifications

import android.util.Log

object ExpenseNotificationProcessor {
    private const val TAG = "ExpenseNotifProcessor"

    // Regex for USD ($12.34) or BRL (R$ 12,34)
    // Captures group 1: currency symbol; group 2: amount
    private val amountRegex = Regex(
        pattern = """
            (?i)            # case-insensitive
            (?:^|[^A-Z0-9]) # boundary: start or non-alnum
            (?:\$|R\$)\s? # currency
            (\d{1,3}(?:[.,]\d{3})*|\d+)(?:[.,](\d{2}))? # number with optional decimals
        """.trimIndent(),
        options = setOf(RegexOption.IGNORE_CASE, RegexOption.COMMENTS)
    )

    fun parseSuggestion(title: String?, body: String?): ExpenseSuggestion? {
        val text = listOfNotNull(title, body).joinToString(" - ").take(500)
        val match = amountRegex.find(text) ?: return null

        // Extract amount parts
        val intPart = match.groups[1]?.value ?: return null
        val decimalPart = match.groups[2]?.value

        // Normalize: remove thousand separators, convert comma to dot
        val normalizedInt = intPart.replace(".", "").replace(",", "")
        val normalized = if (decimalPart != null) {
            "$normalizedInt.$decimalPart"
        } else normalizedInt

        val amount = normalized.toDoubleOrNull()
        if (amount == null) {
            Log.d(TAG, "Failed to parse amount from '$normalized'")
            return null
        }

        val description = buildDescription(title, body)
        return ExpenseSuggestion(amount = amount, description = description)
    }

    private fun buildDescription(title: String?, body: String?): String {
        // Prefer title; fallback to first 60 chars of body
        val raw = when {
            !title.isNullOrBlank() -> title
            !body.isNullOrBlank() -> body
            else -> "Expense"
        }
        // Keep it compact for the notification
        return raw.take(60)
    }
}
