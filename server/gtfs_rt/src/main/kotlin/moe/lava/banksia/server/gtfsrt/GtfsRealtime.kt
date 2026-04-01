package moe.lava.banksia.server.gtfsrt

import com.google.transit.realtime.FeedMessage

abstract class GtfsRealtime(protected val data: FeedMessage) {
    companion object {
        inline fun <T: GtfsRealtime> parse(ctor: (FeedMessage) -> T, data: ByteArray): T {
            val message = FeedMessage.ADAPTER.decode(data)
            return ctor(message)
        }
    }
}
