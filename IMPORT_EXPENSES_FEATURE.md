# Import Expenses Feature

This document describes the CSV Import feature for the Piggy Android app.

## Feature Overview

The Import Expenses feature allows users to bulk import expenses from CSV files. The feature provides:

- CSV file selection via Android's file picker
- Real-time parsing and validation
- Interactive review and confirmation UI
- Category assignment for each expense
- Batch saving to Firestore

## CSV Format Requirements

### File Specifications

- **Encoding**: UTF-8
- **Delimiter**: Comma (,)
- **Header Row**: Required (exact column names)

### Column Headers (exact names required)

1. `data` - Date column (YYYY-MM-DD format)
2. `lançamento` - Description/merchant text
3. `valor` - Numeric value (decimal with dot separator)

### Example CSV

```csv
data,lançamento,valor
2025-10-30,EC *PACCOPET,200.71
2025-10-30,ARMAZEM DO PAULINHO,35.5
2025-10-28,DL*GOOGLE Google,96.99
2025-10-27,Café São Paulo,15.50
2025-10-26,Supermercado ABC,107.0
```

### Data Format Details

#### Date Format
- Pattern: `YYYY-MM-DD`
- Examples: `2025-10-30`, `2024-12-01`
- Invalid dates will cause the row to be skipped with an error

#### Description (lançamento)
- Any text allowed
- UTF-8 characters supported (e.g., ç, ã, õ)
- Leading/trailing whitespace is trimmed
- Multiple consecutive spaces are normalized to single spaces

#### Amount (valor)
- Decimal numbers only
- Use dot (.) as decimal separator (not comma)
- No currency symbols
- Examples: `200.71`, `35.5`, `107.0`, `15`
- Invalid amounts will cause the row to be skipped with an error

## User Flow

### 1. Access Import Screen

From the Expenses screen, tap the **Upload/Import** button in the top bar.

### 2. Select CSV File

- Tap "Select CSV File" button
- Choose a CSV file from device storage or cloud storage
- The file will be validated immediately

### 3. Review Parsed Expenses

After parsing, each expense is displayed in a card showing:
- Date (formatted for readability)
- Description (editable)
- Amount (formatted in local currency)
- Category selector (dropdown)
- Confirm/Skip buttons

### 4. Confirm or Skip Items

For each expense, you can:
- **Confirm**: Mark for import (turns green)
- **Skip**: Exclude from import (grayed out)
- **Edit Description**: Tap description to modify
- **Select Category**: Choose from dropdown

Or use the **"Confirm All"** button to confirm all items at once.

### 5. Save to Database

- Tap the floating action button (checkmark) to save
- Expenses are saved in batches of 50 for performance
- Success message shows count: "X imported, Y skipped"
- Returns to Expenses screen

## Error Handling

### Header Validation
If the CSV doesn't have the correct headers, you'll see:
> "Invalid CSV format. Expected header: data,lançamento,valor"

### Row-Level Errors
Rows with invalid data are skipped:
- Invalid date format (not YYYY-MM-DD)
- Invalid amount format (not a number)

### Empty Files
Empty CSV files (header only) are handled gracefully - no items to review.

## Performance Considerations

- **Streaming Parser**: Large files are parsed incrementally, not loaded entirely into memory
- **Batch Saving**: Expenses are saved in chunks of 50 to maintain responsiveness
- **LazyColumn**: UI efficiently handles thousands of items without performance degradation

## Implementation Details

### Architecture

- **MVVM Pattern**: Uses ViewModel with StateFlow for UI state
- **Coroutines**: All parsing and saving operations use Kotlin Coroutines
- **Dependency Injection**: Uses Hilt for DI
- **Repository Pattern**: Separates parsing logic from data persistence

### Key Components

1. **CsvExpenseParser**: Streaming CSV parser using Apache Commons CSV
2. **ImportExpenseRepository**: Handles parsing and saving operations
3. **ImportExpensesViewModel**: Manages UI state and user actions
4. **ImportExpensesScreen**: Jetpack Compose UI for the import flow

### Testing

Unit tests cover:
- Valid CSV parsing
- Header validation
- Invalid date/amount handling
- UTF-8 character support
- Whitespace normalization
- Empty file handling

Run tests with:
```bash
./gradlew :feature:expenses:testDebugUnitTest
```

## Sample Data

A sample CSV file is included at: `feature/expenses/src/test/resources/sample_expenses.csv`

You can also create your own following the format specifications above.
