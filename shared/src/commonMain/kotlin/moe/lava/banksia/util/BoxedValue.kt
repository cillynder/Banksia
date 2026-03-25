package moe.lava.banksia.util

/** Wraps an arbitrary value, such that equality checks are forced to be done by reference */
class BoxedValue<T>(val value: T) {
    operator fun component1() = value

    companion object {
        fun <T> T.box() = BoxedValue(this)
    }
}
