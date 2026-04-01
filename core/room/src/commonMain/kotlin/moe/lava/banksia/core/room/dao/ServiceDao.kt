package moe.lava.banksia.core.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.core.room.entity.ServiceEntity

@Dao
interface ServiceDao {
    @Query("SELECT * FROM Service")
    suspend fun getAll(): List<ServiceEntity>

    @Query("SELECT * FROM Service WHERE id == :id")
    suspend fun get(id: String): ServiceEntity?

    @Insert
    suspend fun insertAll(vararg services: ServiceEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg services: ServiceEntity)

    @Delete
    suspend fun delete(service: ServiceEntity)

    @Query("DELETE FROM Service")
    suspend fun deleteAll()
}
