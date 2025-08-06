package me.y9san9.jsonrpc

/**
 * This is the exception that is thrown when transport disconnects due to
 * external circumstances. It may be useful if you want to setup client
 * reconnection.
 *
 * It is exposed as public only is you want to filter it out. Do not throw it
 * unless you implement a custom transport adapter.
 */
public class JsonRpcTransportException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
