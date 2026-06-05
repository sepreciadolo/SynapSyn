package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_calculations")
data class SavedCalculation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scaleId: String,
    val scaleName: String,
    val scoreText: String,
    val interpretation: String,
    val details: String, // Comma-separated or serialized item breakdown
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_favorites")
data class UserFavorite(
    @PrimaryKey val featureId: String, // e.g., "nihss", "urgencias"
    val name: String,                  // Display name
    val category: String,              // Categorization name
    val type: String,                  // "calculator" or "criterio"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SavedCalculationDao {
    @Query("SELECT * FROM saved_calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<SavedCalculation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calc: SavedCalculation)

    @Query("DELETE FROM saved_calculations WHERE timestamp < :limitTime")
    suspend fun deleteOlderThan(limitTime: Long)

    @Query("DELETE FROM saved_calculations")
    suspend fun clearAll()
}

@Dao
interface UserFavoriteDao {
    @Query("SELECT * FROM user_favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<UserFavorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: UserFavorite)

    @Query("DELETE FROM user_favorites WHERE featureId = :featureId")
    suspend fun deleteFavorite(featureId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE featureId = :featureId LIMIT 1)")
    fun isFavoriteFlow(featureId: String): Flow<Boolean>
}

@Database(entities = [SavedCalculation::class, UserFavorite::class], version = 2, exportSchema = false)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun savedCalculationDao(): SavedCalculationDao
    abstract fun userFavoriteDao(): UserFavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDatabase(context: Context): AppRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "neuro_compendio_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class SavedCalculationRepository(private val dao: SavedCalculationDao) {
    val allCalculations: Flow<List<SavedCalculation>> = dao.getAllCalculations()

    suspend fun save(calc: SavedCalculation) {
        dao.insertCalculation(calc)
    }

    suspend fun pruneOldCalculations() {
        // Prune calculations older than 12 hours
        val twelveHoursAgo = System.currentTimeMillis() - (12 * 60 * 60 * 1000)
        dao.deleteOlderThan(twelveHoursAgo)
    }

    suspend fun clear() {
        dao.clearAll()
    }
}

class UserFavoriteRepository(private val dao: UserFavoriteDao) {
    val allFavorites: Flow<List<UserFavorite>> = dao.getAllFavorites()

    suspend fun saveFavorite(fav: UserFavorite) {
        dao.insertFavorite(fav)
    }

    suspend fun deleteFavorite(featureId: String) {
        dao.deleteFavorite(featureId)
    }

    fun isFavorite(featureId: String): Flow<Boolean> {
        return dao.isFavoriteFlow(featureId)
    }
}
