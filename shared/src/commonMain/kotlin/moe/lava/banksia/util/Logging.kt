package moe.lava.banksia.util

fun error(tag: String, throwable: Throwable) = error(tag, "", throwable)
expect fun log(tag: String, msg: String)
expect fun error(tag: String, msg: String, throwable: Throwable? = null)

class LogScope(private val tag: String) {
    fun log(msg: String) = log(tag, msg)
}
