package me.y9san9.jsonrpc

/**
 * Type-safe result that is a generic wrapper around [JsonRpcResponse].
 */
public sealed interface JsonRpcResult<out T> {
    public fun getOrThrow(): T

    /**
     * Contains a value in case if it was successfully
     * parsed (either a success or one of decoded error codes).
     */
    public data class Value<out T>(
        public val value: T,
    ) : JsonRpcResult<T> {
        override fun getOrThrow(): T = value
    }

    /**
     * Unknown error code occurred that was not handled
     * properly.
     */
    public data class UnknownError(
        public val error: JsonRpcError,
    ) : JsonRpcResult<Nothing> {
        override fun getOrThrow(): Nothing = error("Unknown error occurred: $error")
    }
}
