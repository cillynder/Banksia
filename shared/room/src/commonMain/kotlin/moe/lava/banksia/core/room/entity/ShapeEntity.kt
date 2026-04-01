package moe.lava.banksia.core.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import moe.lava.banksia.core.room.converter.ShapePathConverter
import moe.lava.banksia.model.Shape
import moe.lava.banksia.model.ShapePath

@Entity("Shape")
@TypeConverters(ShapePathConverter::class)
data class ShapeEntity(
    @PrimaryKey val id: String,
    val path: ShapePath,
) {
    fun asModel() = Shape(id, path)
}

fun Shape.asEntity() = ShapeEntity(id, path)
