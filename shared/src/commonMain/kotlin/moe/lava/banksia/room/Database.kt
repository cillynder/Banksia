package moe.lava.banksia.room

import androidx.room.AutoMigration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.room.util.foreignKeyCheck
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import moe.lava.banksia.room.converter.RouteTypeConverter
import moe.lava.banksia.room.dao.RouteDao
import moe.lava.banksia.room.dao.ServiceDao
import moe.lava.banksia.room.dao.ServiceExceptionDao
import moe.lava.banksia.room.dao.ShapeDao
import moe.lava.banksia.room.dao.StopDao
import moe.lava.banksia.room.dao.StopTimeDao
import moe.lava.banksia.room.dao.TripDao
import moe.lava.banksia.room.dao.VersionMetadataDao
import moe.lava.banksia.room.entity.RouteEntity
import moe.lava.banksia.room.entity.ServiceEntity
import moe.lava.banksia.room.entity.ServiceExceptionEntity
import moe.lava.banksia.room.entity.ShapeEntity
import moe.lava.banksia.room.entity.StopEntity
import moe.lava.banksia.room.entity.StopTimeEntity
import moe.lava.banksia.room.entity.TripEntity
import moe.lava.banksia.room.entity.VersionMetadataEntity
import androidx.room.Database as DatabaseAnnotation

@DatabaseAnnotation(
    version = 11,
    entities = [
        RouteEntity::class,
        ServiceEntity::class,
        ServiceExceptionEntity::class,
        ShapeEntity::class,
        StopEntity::class,
        StopTimeEntity::class,
        TripEntity::class,
        VersionMetadataEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 9, to = 10),
    ]
)
@TypeConverters(RouteTypeConverter::class)
abstract class Database : RoomDatabase() {
    abstract val versionMetadataDao: VersionMetadataDao
    abstract val routeDao: RouteDao
    abstract val serviceDao: ServiceDao
    abstract val serviceExceptionDao: ServiceExceptionDao
    abstract val shapeDao: ShapeDao
    abstract val stopDao: StopDao
    abstract val stopTimeDao: StopTimeDao
    abstract val tripDao: TripDao

    companion object {
        fun build(base: Builder<Database>) =
            base.fallbackToDestructiveMigration(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .addMigrations(MIGRATION_10_11)
//                .fallbackToDestructiveMigration(true)
                .build()
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `_new_Stop` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `lat` REAL NOT NULL, `lng` REAL NOT NULL, `parent` TEXT, `hasWheelChairBoarding` INTEGER NOT NULL, `level` TEXT NOT NULL, `platformCode` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`parent`) REFERENCES `Stop`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED)")
        connection.execSQL("INSERT INTO `_new_Stop` (`id`,`name`,`lat`,`lng`,`parent`,`hasWheelChairBoarding`,`level`,`platformCode`) SELECT `id`,`name`,`lat`,`lng`,`parent`,`hasWheelChairBoarding`,`level`,`platformCode` FROM `Stop`")
        connection.execSQL("UPDATE `_new_Stop` SET `parent` = NULL WHERE `parent` == \"\"")
        connection.execSQL("DROP TABLE `Stop`")
        connection.execSQL("ALTER TABLE `_new_Stop` RENAME TO `Stop`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_Stop_parent` ON `Stop` (`parent`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_Trip_serviceId` ON `Trip` (`serviceId`)")
        foreignKeyCheck(connection, "Stop")
    }
}
