package com.example.simplebudget.data.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,           // System.currentTimeMillis()
    val amount: Double,
    val categoryName: String,      // Enum name from BudgetCategory
    val description: String? = null
)
