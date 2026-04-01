package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.room.entity.StopTimeEntity

@Dao
interface StopTimeDao {
    @Query("SELECT * FROM StopTime")
    suspend fun getAll(): List<StopTimeEntity>

    @Query("SELECT * FROM StopTime WHERE tripId == :tripId")
    suspend fun getForTrip(tripId: String): StopTimeEntity?

    @Query("SELECT * FROM StopTime WHERE tripId IN (:tripIds)")
    suspend fun getForTrips(tripIds: List<String>): List<StopTimeEntity>

    @Query("SELECT * FROM StopTime WHERE stopId == :stopId")
    suspend fun getForStop(stopId: String): List<StopTimeEntity>

    @Query("""
        SELECT DISTINCT StopTime.* FROM StopTime
        INNER JOIN Service ON Service.days & :days = :days AND :date BETWEEN Service.start AND Service.`end`
        INNER JOIN Trip ON Trip.serviceId == Service.id
        LEFT JOIN ServiceException ON ServiceException.serviceId == Service.id AND ServiceException.date == :date
        WHERE StopTime.tripId == Trip.id
            AND StopTime.stopId == :stopId
            AND ServiceException.type IS NULL
    """)
    suspend fun getForStopDated(stopId: String, days: Int, date: Int): List<StopTimeEntity>

    @Insert
    suspend fun insertAll(vararg stopTimes: StopTimeEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg stopTimes: StopTimeEntity)

    @Delete
    suspend fun delete(stopTime: StopTimeEntity)

    @Query("DELETE FROM StopTime")
    suspend fun deleteAll()
}
