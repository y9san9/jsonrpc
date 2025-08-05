package me.y9san9.jsonrpc

internal expect fun createIncrementor(): Incrementor

internal fun interface Incrementor {
    fun incrementAndGet(): Long
}
