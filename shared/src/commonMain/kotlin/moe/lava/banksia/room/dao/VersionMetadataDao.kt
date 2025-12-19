package moe.lava.banksia.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import moe.lava.banksia.room.entity.VersionMetadataEntity

@Dao
interface VersionMetadataDao {
    @Query("SELECT * FROM VersionMetadata WHERE type == :type")
    suspend fun get(type: String): VersionMetadataEntity?

    @Query("SELECT * FROM VersionMetadata")
    suspend fun getAll(): List<VersionMetadataEntity>

    @Insert(onConflict = REPLACE)
    suspend fun update(vararg data: VersionMetadataEntity)

    suspend fun update(vararg data: Pair<String, Long>) {
        update(*data.map { (type, lastUpdated) -> VersionMetadataEntity(type, lastUpdated) }.toTypedArray())
    }

    suspend fun update(lastUpdated: Long, types: Collection<String>) {
        update(*types.map { VersionMetadataEntity(it, lastUpdated) }.toTypedArray())
    }
}
