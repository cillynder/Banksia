package moe.lava.banksia.core.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.core.room.entity.TripEntity

@Dao
interface TripDao {
    @Query("SELECT * FROM Trip")
    suspend fun getAll(): List<TripEntity>

    @Query("SELECT * FROM Trip WHERE id == :id")
    suspend fun get(id: String): TripEntity?

    @Query("SELECT * FROM Trip WHERE routeId == :id")
    suspend fun getByRoute(id: String): List<TripEntity>

    @Insert
    suspend fun insertAll(vararg trips: TripEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg trips: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("DELETE FROM Trip")
    suspend fun deleteAll()
}
