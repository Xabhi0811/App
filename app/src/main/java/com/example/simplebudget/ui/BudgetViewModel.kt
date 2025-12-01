package com.example.simplebudget.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplebudget.data.AppDatabase
import com.example.simplebudget.data.entities.Budget
import com.example.simplebudget.data.entities.BudgetCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
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

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        observeBudgetsForCurrentMonth()
    }

    private fun observeBudgetsForCurrentMonth() {
        val monthYear = _uiState.value.monthYear

        viewModelScope.launch {
            dao.getBudgetsForMonth(monthYear).collectLatest { budgets ->
                val summaries = buildCategorySummaries(budgets)
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

    // ✅ Only ACTIVE budgets become cards
    private fun buildCategorySummaries(
        budgets: List<Budget>
    ): List<CategorySummary> {

        val activeBudgets = budgets.filter { it.isActive }

        return activeBudgets.map { budget ->
            CategorySummary(
                category = BudgetCategory.valueOf(budget.categoryName),
                budgetAmount = budget.maxBudget,
                spentAmount = budget.spentAmount
            )
        }
    }

    // ✅ DELETE = hide the card completely
    fun deleteCategoryBudget(category: BudgetCategory) {
        val monthYear = _uiState.value.monthYear

        viewModelScope.launch {
            val existing = dao.getBudgetForCategory(monthYear, category.name)

            if (existing != null) {
                val hidden = existing.copy(
                    maxBudget = 0.0,
                    spentAmount = 0.0,
                    isActive = false   // ✅ completely hide the card
                )
                dao.insertOrUpdateBudget(hidden)
            }
        }
    }

    /** ✅ Set or update the MAX budget for this category */
    fun setMaxBudgetForCategory(category: BudgetCategory, amount: Double) {
        val monthYear = _uiState.value.monthYear

        viewModelScope.launch {
            val existing = dao.getBudgetForCategory(monthYear, category.name)

            val updated = if (existing == null) {
                Budget(
                    monthYear = monthYear,
                    categoryName = category.name,
                    maxBudget = amount,
                    spentAmount = 0.0,
                    isActive = true      // ✅ IMPORTANT: card becomes visible
                )
            } else {
                existing.copy(
                    maxBudget = amount,
                    isActive = true     // ✅ Reactivate if previously deleted
                )
            }

            dao.insertOrUpdateBudget(updated)
        }
    }

    /** ✅ Add expense = increase spent AND reactivate category if deleted */
    fun addTransaction(
        amount: Double,
        category: BudgetCategory,
        @Suppress("UNUSED_PARAMETER") description: String?
    ) {
        val monthYear = _uiState.value.monthYear

        viewModelScope.launch {
            val existing = dao.getBudgetForCategory(monthYear, category.name)

            val updated = if (existing == null) {
                Budget(
                    monthYear = monthYear,
                    categoryName = category.name,
                    maxBudget = 0.0,
                    spentAmount = amount,
                    isActive = true     // ✅ ensure card is visible
                )
            } else {
                existing.copy(
                    spentAmount = existing.spentAmount + amount,
                    isActive = true     // ✅ re-show if previously deleted
                )
            }

            dao.insertOrUpdateBudget(updated)
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
