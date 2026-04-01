package moe.lava.banksia.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalCoroutinesApi::class)
class LoopFlow<T>(private val block: suspend FlowCollector<T>.() -> Unit) : AbstractFlow<T>() {
    private var delayMs: Long = 5000
    private var init: (suspend FlowCollector<T>.() -> Unit)? = null
    private var waiter: (suspend () -> Unit)? = null

    override suspend fun collectSafely(collector: FlowCollector<T>) {
        init?.invoke(collector)
        while (true) {
            waiter?.invoke()
            collector.block()
            delay(delayMs)
        }
    }

    companion object {
        fun <T> Flow<T>.delayFor(delay: Long) = apply {
            @Suppress("UnusedFlow")
            if (this is LoopFlow)
                this.delayMs = delay
            else
                throw IllegalStateException()
        }

        fun <T> Flow<T>.initWith(block: suspend FlowCollector<T>.() -> Unit) = apply {
            @Suppress("UnusedFlow")
            if (this is LoopFlow)
                this.init = block
            else
                throw IllegalStateException()
        }

        fun <T> Flow<T>.waitFor(waiter: suspend () -> Unit) = apply {
            @Suppress("UnusedFlow")
            if (this is LoopFlow)
                this.waiter = waiter
            else
                throw IllegalStateException()
        }

        fun <T> Flow<T>.waitUntilSubscribed(other: MutableStateFlow<*>) = waitFor {
            val blocked = other.subscriptionCount.value == 0
            if (blocked)
                log("LoopFlow", "blocking flow")
            other.subscriptionCount.first { it > 0 }
            if (blocked)
                log("LoopFlow", "unblocking flow")
        }
    }
}

@OptIn(ExperimentalTypeInference::class)
fun <T> loopFlow(@BuilderInference block: suspend FlowCollector<T>.() -> Unit) = LoopFlow(block)
