package moe.lava.banksia.util

import platform.Foundation.NSLog

// TODO: use better logging functions maybe(?)
actual fun log(tag: String, msg: String) {
    NSLog("$tag: $msg")
}

actual fun error(tag: String, msg: String, throwable: Throwable?) {
    NSLog("$tag: $msg: ${throwable?.stackTraceToString()}")
}
