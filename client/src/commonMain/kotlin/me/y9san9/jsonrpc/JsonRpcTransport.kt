package me.y9san9.jsonrpc

import kotlinx.coroutines.flow.StateFlow

/**
 * You can use this library with any underlying transport or library by
 * implementing JsonRpcTransport.Connector interface.
 *
 * JsonRpcTransport represents an ongoing connection which may be closed any
 * time.
 *
 * May throw [JsonRpcTransportException] without further notice due to it mostly
 * being used inside [JsonRpcTransport.Connector.connect] lambda.
 */
public interface JsonRpcTransport {
    /**
     * Returns true if connection is still alive and false otherwise.
     *
     * Never changes after returning `false`. This might be used to free
     * allocated resources.
     */
    public val isActive: StateFlow<Boolean>

    /**
     * Send message using preferred protocol. Since JsonRpc is basically a
     * string-based protocol, it's transport will work with strings and not
     * bytes.
     */
    public suspend fun send(data: String)

    /**
     * Receive message using preferred protocol. This method should throw
     * CancellationException if no further messages are expected.
     */
    public suspend fun receive(): String

    /**
     * Type-safe result that contains transport error if any occurred during
     * execution of [connect] function.
     */
    public sealed interface Result<out T> {

        /**
         * Type-safe result for [JsonRpcTransportException] that was thrown
         * during execution of [connect] function.
         */
        public data class TransportFailure(
            val message: String? = null,
            val cause: Throwable? = null,
        ) : Result<Nothing>

        /** A successful execution of [connect] function. */
        public data class Success<out T>(val value: T) : Result<T>
    }

    /**
     * Represents a configured connector (with bound URL and other details),
     * which may be used to establish connection and manages lifecycle using
     * coroutines.
     */
    public interface Connector {

        /**
         * Establishes a connection which is closed as soon as [block] execution
         * is finished. It also may be closed any time due to external
         * circumstances and this will lead to the cancellation of [block].
         *
         * Any functions inside [block] may throw JsonRpcTransportException
         * without further notice.
         */
        public suspend fun <T> connect(
            block: suspend JsonRpcTransport.() -> T
        ): Result<T>
    }
}
