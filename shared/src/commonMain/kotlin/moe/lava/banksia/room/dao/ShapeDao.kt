package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.room.entity.ShapeEntity

@Dao
interface ShapeDao {
    @Query("SELECT * FROM Shape WHERE id == :id")
    suspend fun get(id: String): ShapeEntity?

    @Insert
    suspend fun insertAll(vararg shapes: ShapeEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg shapes: ShapeEntity)

    @Delete
    suspend fun delete(shape: ShapeEntity)

    @Query("DELETE FROM Shape")
    suspend fun deleteAll()
}
