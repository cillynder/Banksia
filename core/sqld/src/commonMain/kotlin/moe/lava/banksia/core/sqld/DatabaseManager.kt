package moe.lava.banksia.core.sqld

internal const val DBNAME = "timetable"

expect class DatabaseManager() {
    val database: BanksiaDatabase
}
