package com.example.simplebudget.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Hard-coded categories
enum class BudgetCategory(val displayName: String) {
    HOUSING("Housing"),
    FOOD("Food"),
    TRANSPORTATION("Transportation"),
    ENTERTAINMENT("Entertainment"),
    SAVINGS("Savings");

    companion object {
        val defaultCategories: List<BudgetCategory> = values().toList()
    }
}

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val monthYear: String,        // e.g. "2025-12"
    val categoryName: String,     // BudgetCategory.name
    val maxBudget: Double,        // Max allowed budget
    val spentAmount: Double,
    val isActive: Boolean = true// Total spent so far
)
