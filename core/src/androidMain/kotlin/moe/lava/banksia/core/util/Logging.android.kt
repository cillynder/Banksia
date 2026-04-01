package moe.lava.banksia.core.util

import android.util.Log

actual fun log(tag: String, msg: String) {
    Log.i(tag, msg)
}

actual fun error(tag: String, msg: String, throwable: Throwable?) {
    Log.e(tag, msg)
    throwable?.let { Log.e(tag, it.stackTraceToString()) }
}
