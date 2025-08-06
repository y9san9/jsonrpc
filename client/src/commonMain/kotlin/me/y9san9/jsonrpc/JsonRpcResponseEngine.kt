package me.y9san9.jsonrpc

import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// This optimizes handling of responses by not doing
// `filter` for every response
internal class JsonRpcResponseEngine(
    private val backgroundScope: CoroutineScope,
    private val incomingEngine: JsonRpcIncomingEngine,
) {
    private val pending =
        mutableMapOf<
            JsonRpcResponseId,
            CancellableContinuation<JsonRpcResponse>,
        >()
    private val mutex = Mutex()

    fun start() {
        backgroundScope.launch(start = UNDISPATCHED) {
            incomingEngine.responses.collect { response ->
                val continuation =
                    mutex.withLock { pending.remove(response.id) }
                if (continuation != null) {
                    continuation.resume(response)
                }
            }
        }
    }

    suspend fun await(id: JsonRpcRequestId): JsonRpcResponse {
        val responseId = id.toResponseId()
        return suspendCancellableCoroutine { continuation ->
            backgroundScope.launch(start = UNDISPATCHED) {
                mutex.withLock { pending[responseId] = continuation }
            }
            continuation.invokeOnCancellation {
                backgroundScope.launch(start = UNDISPATCHED) {
                    mutex.withLock { pending.remove(responseId) }
                }
            }
        }
    }
}
