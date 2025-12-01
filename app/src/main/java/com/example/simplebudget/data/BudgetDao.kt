package com.example.simplebudget.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simplebudget.data.entities.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>>

    @Query(
        "SELECT * FROM budgets " +
                "WHERE monthYear = :monthYear AND categoryName = :categoryName " +
                "LIMIT 1"
    )
    suspend fun getBudgetForCategory(
        monthYear: String,
        categoryName: String
    ): Budget?

    // Insert or update (we always replace the whole row)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: Budget)
}
