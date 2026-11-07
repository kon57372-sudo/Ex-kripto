package com.ham.exchange.data.db

import androidx.room.*
import com.ham.exchange.data.model.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "holdings")
data class HoldingEntity(@PrimaryKey val coinId: String, val symbol: String, val amount: Double, val avgBuyPrice: Double)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val coinId: String, val symbol: String, val side: String, val type: String,
    val price: Double, val amount: Double, val status: String, val timestamp: Long
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(@PrimaryKey val coinId: String)

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings") fun all(): Flow<List<HoldingEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(h: HoldingEntity)
    @Query("DELETE FROM holdings WHERE coinId=:id") suspend fun delete(id: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC") fun all(): Flow<List<OrderEntity>>
    @Insert suspend fun insert(o: OrderEntity): Long
    @Query("UPDATE orders SET status=:status WHERE id=:id") suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist") fun all(): Flow<List<WatchlistEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun add(w: WatchlistEntity)
    @Query("DELETE FROM watchlist WHERE coinId=:id") suspend fun remove(id: String)
}

@Database(entities = [HoldingEntity::class, OrderEntity::class, WatchlistEntity::class], version = 1)
abstract class HamDatabase : RoomDatabase() {
    abstract fun holdings(): HoldingDao
    abstract fun orders(): OrderDao
    abstract fun watchlist(): WatchlistDao
}
