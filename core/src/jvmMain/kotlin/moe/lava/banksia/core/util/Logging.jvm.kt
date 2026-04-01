package moe.lava.banksia.core.util

actual fun log(tag: String, msg: String) {
    println("[$tag] $msg")
}

actual fun error(tag: String, msg: String, throwable: Throwable?) {
    println("[$tag] $msg")
    throwable?.let { println(it.stackTraceToString()) }
}
