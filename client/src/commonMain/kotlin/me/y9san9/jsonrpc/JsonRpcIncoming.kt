package me.y9san9.jsonrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * This is a high-level wrapper around low-level [JsonRpcIncomingEngine].
 *
 * Use this class to access incoming requests and notifications as well as
 * respond to them.
 */
public class JsonRpcIncoming internal constructor(
    public val rpc: JsonRpc,
    private val incomingEngine: JsonRpcIncomingEngine,
    private val requestEngine: JsonRpcRequestEngine,
) {
    /**
     * This may be dangerous to use this property.
     * See `docs/FlowPitfall.md` for details.
     */
    public val requestsRaw: SharedFlow<JsonRpcRequest> get() = incomingEngine.requests

    /**
     * Simple accessor for [requestsRaw] that launches a job and collect
     * requests in a safe manner. It also returns [Job], so you can cancel
     * this job at some point.
     *
     * You can additionally pass [scope] to be [rpc.backgroundScope] to make
     * your job cancelled automatically. Otherwise, it will hold [JsonRpc]
     * alive until returned [Job] is cancelled.
     *
     * It's safe to write such code and no elements will be skipped under any
     * circumstances:
     *
     * ```kotlin
     * rpc.incoming.onRequest { request ->
     *     println(request)
     * }
     *
     * rpc.execute(...) // Send jsonrpc request to subscribe to something
     * ```
     */
    public suspend fun onRequest(
        scope: CoroutineScope = rpc.scope,
        block: suspend (JsonRpcRequest) -> Unit,
    ): Job {
        return scope.launch(start = UNDISPATCHED) {
            requestsRaw.collect(block)
        }
    }

    /**
     * This may be dangerous to use this property.
     * See `docs/FlowPitfall.md` for details.
     *
     * Underlying implementation uses hashmap to route requests by name which
     * is a huge optimization instead of doing filters.
     */
    public fun requestsRaw(method: JsonRpcMethodName): Flow<JsonRpcRequest> {
        return requestEngine.flow(method)
    }

    /**
     * Simple accessor for [requestsRaw] that launches a job and collect
     * requests in a safe manner. It also returns [Job], so you can cancel
     * this job at some point.
     *
     * You can additionally pass [scope] to be [rpc.backgroundScope] to make
     * your job cancelled automatically. Otherwise, it will hold [JsonRpc]
     * alive until returned [Job] is cancelled.
     *
     * It's safe to write such code and no elements will be skipped under any
     * circumstances:
     *
     * ```kotlin
     * rpc.incoming.onRequest { request ->
     *     println(request)
     * }
     *
     * rpc.execute(...) // Send jsonrpc request to subscribe to something
     * ```
     */
    public suspend fun onRequest(
        method: JsonRpcMethodName,
        scope: CoroutineScope = rpc.scope,
        block: suspend (JsonRpcRequest) -> Unit,
    ): Job {
        return scope.launch(start = UNDISPATCHED) {
            requestsRaw(method).collect(block)
        }
    }

    /**
     * This may be dangerous to use this property.
     * See `docs/FlowPitfall.md` for details.
     */
    public val responsesRaw: SharedFlow<JsonRpcResponse> get() = incomingEngine.responses

    /**
     * Simple accessor for [responsesRaw] that launches a job and collect
     * respnoses in a safe manner. It also returns [Job], so you can cancel
     * this job at some point.
     *
     * You can additionally pass [scope] to be [rpc.backgroundScope] to make
     * your job cancelled automatically. Otherwise, it will hold [JsonRpc]
     * alive until returned [Job] is cancelled.
     *
     * It's safe to write such code and no elements will be skipped under any
     * circumstances:
     *
     * ```kotlin
     * rpc.incoming.onhResponse { response ->
     *     println(response)
     * }
     *
     * rpc.execute(...) // Send jsonrpc request to subscribe to something
     * ```
     */
    public suspend fun onResponse(
        scope: CoroutineScope = rpc.scope,
        block: suspend (JsonRpcResponse) -> Unit,
    ): Job {
        return scope.launch(start = UNDISPATCHED) {
            responsesRaw.collect(block)
        }
    }
}
