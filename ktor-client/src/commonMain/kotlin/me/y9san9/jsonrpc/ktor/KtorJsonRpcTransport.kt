package me.y9san9.jsonrpc.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.IOException
import me.y9san9.jsonrpc.JsonRpcTransport

/**
 * Ktor Client adapter for jsonrpc.
 *
 * JsonRpcTransport represents an ongoing connection which may be closed
 * any time.
 *
 * May throw [JsonRpcTransportException] without further notice due to it
 * mostly being used inside [JsonRpcTransport.Connector.connect]
 * lambda.
 */
public class KtorClientJsonRpcTransport(
    isActive: StateFlow<Boolean>,
    private val session: DefaultClientWebSocketSession,
) : JsonRpcTransport {

    /**
     * Returns true if connection is still alive and false otherwise.
     *
     * Never changes after returning `false`. This might be used to free
     * allocated resources.
     */
    override val isActive: StateFlow<Boolean> = isActive

    /**
     * Send message using preferred protocol.
     * Since JsonRpc is basically a string-based protocol,
     * it's transport will work with strings and not bytes.
     */
    override suspend fun send(data: String) {
        if (!isActive.value) {
            error("Socket connection is closed")
        }
        session.send(data)
    }

    /**
     * Receive message using preferred protocol.
     * This method should throw CancellationException if
     * no further messages are expected.
     */
    override suspend fun receive(): String {
        if (!isActive.value) {
            error("Socket connection is closed")
        }

        val frame = session
            .incoming
            .receiveCatching()
            .getOrElse { throwable ->
                throw IOException("Closed by server", throwable)
            }

        if (frame !is Frame.Text) {
            error("Websocket should only send text frames")
        }

        return frame.readText()
    }

    /**
     * Represents a configured connector (with bound URL and other details),
     * which may be used to establish connection and manages lifecycle using
     * coroutines.
     */
    public class Connector(
        private val url: String,
        httpClient: HttpClient,
        private val request: HttpRequestBuilder.() -> Unit = {},
    ) : JsonRpcTransport.Connector {

        private val httpClient = httpClient.config {
            install(WebSockets)
        }

        /**
         * Establishes a connection which is closed as soon as [block] execution
         * is finished. It also may be closed any time due to external
         * circumstances and this will lead to the cancellation of [block].
         *
         * Any functions inside [block] may throw JsonRpcTransportException
         * without further notice.
         */
        override suspend fun <T> connect(
            block: suspend JsonRpcTransport.() -> T,
        ): JsonRpcTransport.Result<T> {
            val isActive = MutableStateFlow(true)
            return try {
                lateinit var result: JsonRpcTransport.Result<T>
                httpClient.webSocket(url, request) {
                    val session = this
                    val transport = KtorClientJsonRpcTransport(
                        session = session,
                        isActive = isActive,
                    )
                    val value = block(transport)
                    result = JsonRpcTransport.Result.Success(value)
                }
                result
            } catch (exception: IOException) {
                JsonRpcTransport.Result.TransportFailure(
                    message = exception.message,
                    cause = exception.cause,
                )
            } finally {
                isActive.value = false
            }
        }
    }
}
