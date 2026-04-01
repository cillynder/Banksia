package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.room.entity.StopEntity

@Dao
interface StopDao {
    @Query("SELECT * FROM Stop")
    suspend fun getAll(): List<StopEntity>

    @Query("""
        SELECT * FROM Stop
        WHERE platformCode <> ""
        AND parent == ""
    """)
    suspend fun getAllParentless(): List<StopEntity>

    @Query("SELECT * FROM Stop WHERE id == :id")
    suspend fun get(id: String): StopEntity?

    @Query("SELECT * FROM Stop WHERE id IN (:ids)")
    suspend fun get(ids: List<String>): List<StopEntity>

    @Insert
    suspend fun insertAll(vararg stops: StopEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg stops: StopEntity)

    @Delete
    suspend fun delete(stop: StopEntity)

    @Query("DELETE FROM Stop")
    suspend fun deleteAll()

    @Query("UPDATE Stop SET parent = :parent WHERE id IN (:ids)")
    suspend fun updateParents(ids: List<String>, parent: String)
}
