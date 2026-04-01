package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.room.entity.ServiceExceptionEntity

@Dao
interface ServiceExceptionDao {
    @Query("SELECT * FROM ServiceException")
    suspend fun getAll(): List<ServiceExceptionEntity>

    @Query("SELECT * FROM ServiceException WHERE serviceId == :id")
    suspend fun get(id: String): List<ServiceExceptionEntity>

    @Insert
    suspend fun insertAll(vararg exceptions: ServiceExceptionEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertOrReplaceAll(vararg exceptions: ServiceExceptionEntity)

    @Delete
    suspend fun delete(service: ServiceExceptionEntity)

    @Query("DELETE FROM ServiceException")
    suspend fun deleteAll()
}
