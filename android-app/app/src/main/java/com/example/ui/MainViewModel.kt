package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entities ───────────────────────────────────────────

@Entity(tableName = "repairs")
data class Repair(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val device: String,
    val customerLocation: String,
    val status: String
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val trxBalance: Double = 12400.0,
    val lastCheckIn: Long = 0L,
    val lastScratch: Long = 0L
)

// ─── DAOs ────────────────────────────────────────────────

@Dao
interface RepairDao {
    @Query("SELECT * FROM repairs ORDER BY id DESC")
    fun getAllRepairs(): Flow<List<Repair>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepair(repair: Repair)

    @Query("DELETE FROM repairs")
    suspend fun clearAllRepairs()
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)
}

// ─── Database ────────────────────────────────────────────

@Database(
    entities = [Repair::class, UserStats::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repairDao(): RepairDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emobies_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
