package io.github.hosseinkarami_dev.penny.data.local.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "preferences")
data class PreferenceEntity(@PrimaryKey val key: String, val value: String)

@Dao
interface PreferenceDao {
    @Query("SELECT value FROM preferences WHERE `key` = :key")
    fun observeValue(key: String): Flow<String?>

    @Query("SELECT * FROM preferences")
    suspend fun getAll(): List<PreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(entity: PreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAll(entities: List<PreferenceEntity>)

    @Query("DELETE FROM preferences")
    suspend fun deleteAll()

    @Query("SELECT value FROM preferences WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Query("DELETE FROM preferences WHERE `key` LIKE :pattern")
    suspend fun deleteByKeyPattern(pattern: String)
}
