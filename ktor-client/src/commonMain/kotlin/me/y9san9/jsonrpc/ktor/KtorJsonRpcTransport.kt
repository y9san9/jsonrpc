package me.y9san9.jsonrpc.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.IOException
import me.y9san9.jsonrpc.JsonRpcTransport

/**
 * Ktors default is 'no ping'. This is unacceptable, because there is no way to
 * determine whether connection was dropped or not without pings. That is why we
 * introduce a new default.
 */
public const val DEFAULT_PING_INTERVAL_MILLIS: Long = 5_000

/**
 * Ktor Client adapter for jsonrpc.
 *
 * JsonRpcTransport represents an ongoing connection which may be closed any
 * time.
 *
 * May throw [JsonRpcTransportException] without further notice due to it mostly
 * being used inside [JsonRpcTransport.Connector.connect] lambda.
 */
public class KtorJsonRpcTransport(
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
     * Send message using preferred protocol. Since JsonRpc is basically a
     * string-based protocol, it's transport will work with strings and not
     * bytes.
     *
     * This method can throw IOException to indicate that transport was closed.
     */
    override suspend fun send(data: String) {
        session.outgoing.send(Frame.Text(data))
    }

    /**
     * Receive message using preferred protocol.
     *
     * This method can throw IOException to indicate that transport was closed.
     */
    override suspend fun receive(): String {
        val frame = session.incoming.receive()

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
        pingIntervalMillis: Long? = DEFAULT_PING_INTERVAL_MILLIS,
        private val request: HttpRequestBuilder.() -> Unit = {},
    ) : JsonRpcTransport.Connector {

        private val httpClient = httpClient.config {
            install(WebSockets) {
                if (pingIntervalMillis != null) {
                    this.pingIntervalMillis = pingIntervalMillis
                }
            }
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
                    val transport = KtorJsonRpcTransport(isActive, session)
                    val value = block(transport)
                    result = JsonRpcTransport.Result.Success(value)
                }
                result
            } catch (exception: IOException) {
                JsonRpcTransport.Result.TransportFailure(
                    message = exception.message,
                    cause = exception,
                )
            } finally {
                isActive.value = false
            }
        }
    }
}
