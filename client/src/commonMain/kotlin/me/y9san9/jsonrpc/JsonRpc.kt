package me.y9san9.jsonrpc

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * JsonRpc is a complete implementation of JsonRpc 2.0 specification
 * (https://www.jsonrpc.org/specification). There are multiple entry points
 * to construct this class and you usually want to use extension functions
 * on the companion object.
 *
 * For example:
 *
 * ```kotlin
 * val rpc = JsonRpc.websocket("wss://example.org")
 * rpc.connect { this: JsonRpc
 *     // ...
 * }
 * ```
 *
 * May throw [JsonRpcTransportException] without further notice due to it
 * mostly being used inside [JsonRpc.Connector.connect]
 * lambda.
 */
public class JsonRpc private constructor(
    backgroundScope: CoroutineScope,
    scope: CoroutineScope,
    public val config: JsonRpcConfig,
    private val transport: JsonRpcTransport,
    private val responseEngine: JsonRpcResponseEngine,
    private val requestEngine: JsonRpcRequestEngine,
    private val incomingEngine: JsonRpcIncomingEngine,
) {
    private val incrementor = createIncrementor()

    /**
     * Background scope allows to launch jobs that should be cancelled
     * automatically when [scope] execution is finished. This happens when
     * user code (lamba of rpc.connect) is finished executing and all the
     * jobs launched there are finished.
     *
     * For example, you might log all the events in that scope and this job
     * will not hold you from continuing execution after all other jobs are
     * done.
     *
     * This scope has no practical difference with [scope] if your program is
     * expected to never terminate and rpc.connect never finished.
     */
    public val backgroundScope: CoroutineScope = backgroundScope

    /**
     * Launching jobs in this scope will postpone closing the socket until
     * all the jobs are finished. That means, launching a never-ending update
     * handler in that scope will lead to rpc.connect to never finish.
     */
    public val scope: CoroutineScope = scope

    /**
     * Process incoming requests and notifications using this property.
     */
    public val incoming: JsonRpcIncoming = JsonRpcIncoming(
        rpc = this,
        incomingEngine = incomingEngine,
        requestEngine = requestEngine,
    )

    /**
     * Unique IDs must be used for each request and
     * you get get one using this method.
     */
    public fun nextId(): JsonRpcRequestId.Long {
        val nextLong = incrementor.incrementAndGet()
        return JsonRpcRequestId.Long(nextLong)
    }

    /**
     * Execute [request] according to the specification.
     */
    public suspend fun execute(
        request: JsonRpcRequest,
    ): JsonRpcResponse {
        return executeOrThrow(listOf(request)).first()
    }

    /**
     * Execute [requests] according to the specification.
     *
     * If [requests] have more than a single element, it will be executed
     * in a batch request unless [config.batchRequests] is set to false.
     *
     * Throws if empty list was provided to execute.
     *
     * @returns a list of responses that has length the same as methods amount
     *  inside requests list (since notifications don't have response)
     */
    public suspend fun executeOrThrow(
        requests: List<JsonRpcRequest>,
    ): List<JsonRpcResponse> {
        require(requests.isNotEmpty()) { "Nothing to execute" }
        val serializable = requests.map { request -> request.serializable() }
        val json = if (config.batchRequests && requests.size > 1) {
            config.json.encodeToString(serializable)
        } else {
            config.json.encodeToString(serializable.first())
        }
        return coroutineScope {
            val deferred = requests
                .filterIsInstance<JsonRpcMethod>()
                .map { request -> async { responseEngine.await(request.id) } }
            transport.send(json)
            deferred.awaitAll()
        }
    }

    /**
     * Sends a [response] to the other side according to specification.
     * Response must contain id of request you are responding
     * to. You can't respond to notification.
     */
    public suspend fun respond(
        response: JsonRpcResponse,
    ) {
        respondOrThrow(listOf(response))
    }

    /**
     * Sends a list of [responses] to the other side according to
     * specification. Responses must contain ids of requests you are responding
     * to. You can't respond to notification.
     *
     * Throws if [responses] are empty.
     */
    public suspend fun respondOrThrow(
        responses: List<JsonRpcResponse>,
    ) {
        require(responses.isNotEmpty()) { "Nothing to respond with" }
        val serializable = responses.map { response -> response.serializable() }
        val json = if (config.batchRequests && responses.size > 1) {
            config.json.encodeToString(serializable)
        } else {
            config.json.encodeToString(serializable.first())
        }
        transport.send(json)
    }

    /**
     * Encode params for request.
     *
     * Throws if params serializer is not found or if it's not structure or list.
     */
    public inline fun <reified T> encodeParamsOrThrow(
        params: T
    ): JsonRpcParams {
        val encoded = config.json.encodeToJsonElement(params)
        if (encoded is JsonArray) {
            return JsonRpcParams.Array(encoded)
        }
        if (encoded is JsonObject) {
            return JsonRpcParams.Object(encoded)
        }
        error("Params must be either Array or Object, $encoded was instead")
    }

    /**
     * Decode result of response using [config.json].
     *
     * Throws [SerializationException] if can't decode.
     */
    public inline fun <reified T> decodeResultOrThrow(
        request: JsonRpcResponse.Success,
    ): T {
        return config.json.decodeFromJsonElement(request.result)
    }

    /**
     * Decode params of request using [config.json].
     *
     * Throws [SerializationException] if can't decode.
     */
    public inline fun <reified T> decodeParamsOrThrow(request: JsonRpcRequest): T {
        val params = request.params
        if (params == null) {
            if (null is T) {
                return null as T
            } else {
                throw SerializationException("No params to decode")
            }
        }
        return config.json.decodeFromJsonElement(params.json)
    }

    // Intentionally left empty to have the ability to add extension functions
    public companion object {}


    /**
     * Type-safe result that contains transport error if any occurred during
     * execution of [connect] function.
     */
    public sealed interface Result<out T> {
        public val value: T?

        /**
         * Type-safe result for [JsonRpcTransportException] that was thrown
         * during execution of [connect] function.
         */
        public data class TransportFailure(
            val message: String? = null,
            val cause: Throwable? = null,
        ) : Result<Nothing> {
            override val value: Nothing? = null
        }

        /**
         * A successful execution of [connect] function.
         */
        public data class Success<out T>(
            override val value: T,
        ) : Result<T>

    }

    /**
     * This class constructs a connector to JsonRpc which manages lifecycle
     * using coroutines.
     */
    public class Connector(
        private val transport: JsonRpcTransport.Connector,
        private val config: JsonRpcConfig,
    ) {
        /**
         * Connect to tranport and disconnect as soon as [block] execution
         * is finished. If transport will be disconnected earlier due to
         * external network conditions, [block] execution will be cancelled.
         *
         * Any functions inside [block] may throw JsonRpcTransportException
         * without further notice.
         */
        public suspend fun <T> connect(
            block: suspend JsonRpc.() -> T,
        ): Result<T> {
            val result = transport.connect {
                val transport = this
                coroutineScope {
                    // backgroundScope is cancelled whenever [block] and all
                    // jobs launched in that block are finished
                    //
                    // There are 3 main reasons why scope is created this way:
                    //
                    // * I want to pass this scope around and I need it in
                    //  variable, `launch` will not help me there
                    //
                    // * When I cancel background scope, I don't want to cancel
                    //  [this], so it will be able to return a success value
                    //
                    // * When there is an exception in backgroundScope, I want
                    //  to propagate it up, so I use [coroutineContex.job] to
                    //  create a parent-child relation
                    val backgroundScope = this + Job(coroutineContext.job)

                    // Using backgroundScope, since user launches jobs in
                    // [block] to access this engine, and therefore
                    // backgroundScope lives
                    val incomingEngine = JsonRpcIncomingEngine(
                        backgroundScope = backgroundScope,
                        config = config,
                        transport = transport,
                    )
                    incomingEngine.start()

                    // Using backgroundScope, since user awaits all the
                    // invocations to this engine, and therefore
                    // backgroundScope lives
                    val responseEngine = JsonRpcResponseEngine(
                        backgroundScope = backgroundScope,
                        incomingEngine = incomingEngine,
                    )
                    responseEngine.start()

                    // Using backgroundScope, since user uses collect
                    // to all the flows produced by this engine and
                    // backgroundScope lives
                    val requestEngine = JsonRpcRequestEngine(
                        backgroundScope = backgroundScope,
                        incomingEngine = incomingEngine,
                    )
                    requestEngine.start()

                    val result = coroutineScope {
                        // userScope awaits all the jobs before cleaning
                        // up backgroundScope.
                        val userScope = this
                        val rpc = JsonRpc(
                            backgroundScope = backgroundScope,
                            scope = userScope,
                            config = config,
                            transport = transport,
                            responseEngine = responseEngine,
                            requestEngine = requestEngine,
                            incomingEngine = incomingEngine,
                        )
                        block(rpc)
                    }
                    backgroundScope.cancel()
                    result
                }
            }
            return when (result) {
                is JsonRpcTransport.Result.Success -> with(result) {
                    Result.Success(value)
                }
                is JsonRpcTransport.Result.TransportFailure -> with(result) {
                    Result.TransportFailure(message, cause)
                }
            }
        }

    }
}

