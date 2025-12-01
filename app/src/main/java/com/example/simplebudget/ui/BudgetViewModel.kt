package com.example.simplebudget.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplebudget.data.AppDatabase
import com.example.simplebudget.data.entities.Budget
import com.example.simplebudget.data.entities.BudgetCategory
import com.example.simplebudget.data.entities.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class CategorySummary(
    val category: BudgetCategory,
    val budgetAmount: Double,
    val spentAmount: Double
) {
    val remainingAmount: Double
        get() = budgetAmount - spentAmount
}

data class BudgetUiState(
    val monthYear: String = currentMonthYear(),
    val summaries: List<CategorySummary> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).budgetDao()

    val categories: List<BudgetCategory> = BudgetCategory.defaultCategories

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    // 🔽 NEW: keep full list of transactions for the current month
    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions.asStateFlow()

    init {
        observeCurrentMonth()
    }

    private fun observeCurrentMonth() {
        val monthYear = _uiState.value.monthYear
        val (startMillis, endMillis) = getMonthStartEnd(monthYear)

        viewModelScope.launch {
            combine(
                dao.getBudgetsForMonth(monthYear),
                dao.getTransactionsForPeriod(startMillis, endMillis)
            ) { budgets, transactions ->
                val summaries = buildCategorySummaries(budgets, transactions)
                summaries to transactions
            }.collect { (summaries, transactions) ->
                _transactions.value = transactions
                _uiState.update {
                    it.copy(
                        summaries = summaries,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun buildCategorySummaries(
        budgets: List<Budget>,
        transactions: List<TransactionEntity>
    ): List<CategorySummary> {
        return categories.map { category ->
            val budgetAmount =
                budgets.find { it.categoryName == category.name }?.budgetAmount ?: 0.0
            val spentAmount = transactions
                .filter { it.categoryName == category.name }
                .sumOf { it.amount }

            CategorySummary(
                category = category,
                budgetAmount = budgetAmount,
                spentAmount = spentAmount
            )
        }
    }

    fun setBudgetForCategory(category: BudgetCategory, amount: Double) {
        val monthYear = _uiState.value.monthYear
        viewModelScope.launch {
            val budget = Budget(
                monthYear = monthYear,
                categoryName = category.name,
                budgetAmount = amount
            )
            dao.insertBudget(budget)
        }
    }

    fun addTransaction(
        amount: Double,
        category: BudgetCategory,
        description: String?
    ) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            val tx = TransactionEntity(
                timestamp = now,
                amount = amount,
                categoryName = category.name,
                description = description?.takeIf { it.isNotBlank() }
            )
            dao.insertTransaction(tx)
        }
    }

    // 🔽 NEW: delete a single transaction
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    // 🔽 NEW: optional - clear all
    fun deleteAllTransactions() {
        viewModelScope.launch {
            dao.deleteAllTransactions()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

fun currentMonthYear(): String {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    return String.format(Locale.US, "%04d-%02d", year, month)
}

private fun getMonthStartEnd(monthYear: String): Pair<Long, Long> {
    val parts = monthYear.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt() - 1

    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis - 1
    return start to end
}
