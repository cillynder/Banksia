package moe.lava.banksia.core.room.converter

import androidx.room.TypeConverter
import moe.lava.banksia.model.ShapePath
import moe.lava.banksia.util.Point

object ShapePathConverter {
    @TypeConverter
    fun from(value: ByteArray): ShapePath {
        return value
            .asIterable()
            .chunked(8) {
                (it[0].toLong() and 0xFF) or
                (it[1].toLong() and 0xFF shl 8) or
                (it[2].toLong() and 0xFF shl 16) or
                (it[3].toLong() and 0xFF shl 24) or
                (it[4].toLong() and 0xFF shl 32) or
                (it[5].toLong() and 0xFF shl 40) or
                (it[6].toLong() and 0xFF shl 48) or
                (it[7].toLong() and 0xFF shl 56)
            }
            .map { Double.fromBits(it) }
            .chunked(2)
            .map { (lat, lng) -> Point(lat, lng) }
    }

    @TypeConverter
    fun to(path: ShapePath): ByteArray {
        return path
            .flatMap { (lat, lng) -> listOf(lat.toBits(), lng.toBits()) }
            .flatMap { i -> listOf(
                i.toByte(),
                (i shr 8).toByte(),
                (i shr 16).toByte(),
                (i shr 24).toByte(),
                (i shr 32).toByte(),
                (i shr 40).toByte(),
                (i shr 48).toByte(),
                (i shr 56).toByte(),
            ) }
            .toByteArray()
    }
}
