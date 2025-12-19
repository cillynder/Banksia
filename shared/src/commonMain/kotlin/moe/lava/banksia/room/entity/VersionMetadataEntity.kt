package moe.lava.banksia.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import moe.lava.banksia.model.VersionMetadata

@Entity(
    "VersionMetadata",
)
data class VersionMetadataEntity(
    /** Entity type this metadata applies to */
    @PrimaryKey val type: String,
    /** Last updated */
    val lastUpdated: Long,
) {
    fun asModel() = VersionMetadata(type, lastUpdated)
}

fun VersionMetadata.asEntity() = VersionMetadataEntity(type, lastUpdated)
