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

@Database(entities = [SavedCalculation::class], version = 1, exportSchema = false)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun savedCalculationDao(): SavedCalculationDao

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
