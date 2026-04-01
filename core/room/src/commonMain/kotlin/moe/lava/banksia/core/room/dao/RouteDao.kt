package moe.lava.banksia.core.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.core.room.entity.RouteEntity
import moe.lava.banksia.core.room.entity.StopEntity

@Dao
interface RouteDao {
    @Query("SELECT * FROM Route")
    suspend fun getAll(): List<RouteEntity>

    @Query("SELECT * FROM Route WHERE id == :id")
    suspend fun get(id: String): RouteEntity?

    @Insert
    suspend fun insertAll(vararg routes: RouteEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg routes: RouteEntity)

    @Delete
    suspend fun delete(route: RouteEntity)

    @Query("DELETE FROM Route")
    suspend fun deleteAll()

    @Query("""
        SELECT Stop.* FROM Stop
        INNER JOIN StopTime ON StopTime.stopId == Stop.id
        INNER JOIN Trip ON Trip.id == StopTime.tripId
        WHERE Trip.routeId == :id
        GROUP BY Stop.id
    """)
    suspend fun stops(id: String): List<StopEntity>

    // I vibecoded this, sorry
    @Query("""
        WITH Tree AS (
            SELECT Stop.* FROM Stop
            INNER JOIN StopTime ON StopTime.stopId == Stop.id
            INNER JOIN Trip ON Trip.id == StopTime.tripId
            WHERE Trip.routeId == :id
            GROUP BY Stop.id

            UNION ALL

            SELECT s.*
            FROM Stop s
            INNER JOIN Tree t ON s.id = t.parent
        )
        SELECT DISTINCT * FROM Tree WHERE parent IS NULL;
    """)
    suspend fun stopsParent(id: String): List<StopEntity>
}
