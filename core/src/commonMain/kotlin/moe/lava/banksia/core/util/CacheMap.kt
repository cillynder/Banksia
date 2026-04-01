package moe.lava.banksia.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CacheMap<K, V>(
    coroutineScope: CoroutineScope,
    val expiryMinutes: Int = 5,
    private val innerMap: MutableMap<K, V> = mutableMapOf()
) : MutableMap<K, V> by innerMap {
    val keyExpiries = mutableMapOf<K, Int>()
    var counter = 0

    init {
        coroutineScope.launch {
            while (true) {
                delay(60000)
                counter += 1
                keyExpiries
                    .filterValues { expiry -> expiry >= counter }
                    .keys
                    .forEach { key ->
                        innerMap.remove(key)
                        keyExpiries.remove(key)
                    }
            }
        }
    }

    override fun put(key: K, value: V): V? {
        keyExpiries[key] = counter + expiryMinutes + 1
        return innerMap.put(key, value)
    }

    override fun putAll(from: Map<out K, V>) {
        keyExpiries.putAll(from.map { it.key to (counter + expiryMinutes + 1) })
        innerMap.putAll(from)
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            error("CacheMap", ".entries accessed, cloning..", IllegalStateException())
            return this.entries.toMutableSet()
        }

}
