package moe.lava.banksia.room

import androidx.room.AutoMigration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import moe.lava.banksia.room.converter.RouteTypeConverter
import moe.lava.banksia.room.dao.VersionMetadataDao
import moe.lava.banksia.room.dao.RouteDao
import moe.lava.banksia.room.dao.ShapeDao
import moe.lava.banksia.room.dao.StopDao
import moe.lava.banksia.room.dao.StopTimeDao
import moe.lava.banksia.room.dao.TripDao
import moe.lava.banksia.room.entity.RouteEntity
import moe.lava.banksia.room.entity.ShapeEntity
import moe.lava.banksia.room.entity.StopEntity
import moe.lava.banksia.room.entity.StopTimeEntity
import moe.lava.banksia.room.entity.TripEntity
import moe.lava.banksia.room.entity.VersionMetadataEntity
import androidx.room.Database as DatabaseAnnotation

@DatabaseAnnotation(
    version = 3,
    entities = [
        RouteEntity::class,
        ShapeEntity::class,
        StopEntity::class,
        StopTimeEntity::class,
        TripEntity::class,
        VersionMetadataEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
@TypeConverters(RouteTypeConverter::class)
abstract class Database : RoomDatabase() {
    abstract val versionMetadataDao: VersionMetadataDao
    abstract val routeDao: RouteDao
    abstract val shapeDao: ShapeDao
    abstract val stopDao: StopDao
    abstract val stopTimeDao: StopTimeDao
    abstract val tripDao: TripDao

    companion object {
        fun build(base: Builder<Database>) =
            base.fallbackToDestructiveMigration(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
//                .fallbackToDestructiveMigration(true)
                .build()
    }
}
