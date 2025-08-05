package me.y9san9.jsonrpc

import java.util.concurrent.atomic.AtomicLong

internal actual fun createIncrementor(): Incrementor {
    val int = AtomicLong()
    return Incrementor(int::incrementAndGet)
}
