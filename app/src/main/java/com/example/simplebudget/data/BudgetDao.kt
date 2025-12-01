package com.example.simplebudget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simplebudget.data.entities.Budget
import com.example.simplebudget.data.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>>

    @Query(
        "SELECT * FROM transactions " +
                "WHERE timestamp BETWEEN :startMillis AND :endMillis " +
                "ORDER BY timestamp DESC"
    )
    fun getTransactionsForPeriod(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)


    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)


    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
