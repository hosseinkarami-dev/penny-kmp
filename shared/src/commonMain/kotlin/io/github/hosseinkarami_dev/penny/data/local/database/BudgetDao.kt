package io.github.hosseinkarami_dev.penny.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM category_budgets ORDER BY category COLLATE NOCASE")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM category_budgets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM category_budgets ORDER BY category COLLATE NOCASE")
    suspend fun getAll(): List<BudgetEntity>

    @Query("DELETE FROM category_budgets")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)
}
