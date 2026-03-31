package moe.lava.banksia.util

import kotlinx.datetime.DayOfWeek

private fun Int.check(other: Int) = (this and other) != 0

fun Int.deserialiseDaysBitflag(): List<DayOfWeek> = buildList {
    val days = this@deserialiseDaysBitflag
    if (days.check(1))
        add(DayOfWeek.MONDAY)
    if (days.check(1 shl 1))
        add(DayOfWeek.TUESDAY)
    if (days.check(1 shl 2))
        add(DayOfWeek.WEDNESDAY)
    if (days.check(1 shl 3))
        add(DayOfWeek.THURSDAY)
    if (days.check(1 shl 4))
        add(DayOfWeek.FRIDAY)
    if (days.check(1 shl 5))
        add(DayOfWeek.SATURDAY)
    if (days.check(1 shl 6))
        add(DayOfWeek.SUNDAY)
}

fun List<DayOfWeek>.serialise(): Int =
    this.fold(0) { vl, n ->
        vl + when (n) {
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 1 shl 1
            DayOfWeek.WEDNESDAY -> 1 shl 2
            DayOfWeek.THURSDAY -> 1 shl 3
            DayOfWeek.FRIDAY -> 1 shl 4
            DayOfWeek.SATURDAY -> 1 shl 5
            DayOfWeek.SUNDAY -> 1 shl 6
        }
    }
