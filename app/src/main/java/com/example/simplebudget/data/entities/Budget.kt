package com.example.simplebudget.data.entities


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["monthYear", "categoryName"], unique = true)
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val monthYear: String,      // e.g. "2025-10"
    val categoryName: String,   // Enum name from BudgetCategory
    val budgetAmount: Double
)

/**
 * Hard-coded budget categories for the app.
 */
enum class BudgetCategory(val displayName: String) {
    Housing("Housing"),
    Food("Food"),
    Transportation("Transportation"),
    Entertainment("Entertainment"),
    Savings("Savings");

    companion object {
        val defaultCategories: List<BudgetCategory>
            get() = values().toList()
    }
}
