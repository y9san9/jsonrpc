package me.y9san9.jsonrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// This optimizes handling of responses by not doing
// `filter` for every response
internal class JsonRpcRequestEngine(
    private val backgroundScope: CoroutineScope,
    private val incomingEngine: JsonRpcIncomingEngine,
) {
    private val handlers =
        mutableMapOf<
            JsonRpcMethodName,
            MutableSet<SendChannel<JsonRpcRequest>>,
            >()
    private val mutex = Mutex()

    fun start() {
        backgroundScope.launch(start = UNDISPATCHED) {
            incomingEngine.requests.collect { request ->
                val channels = mutex.withLock { handlers[request.method] }
                channels ?: return@collect
                for (channel in channels) {
                    channel.trySend(request)
                }
            }
        }
    }

    fun flow(method: JsonRpcMethodName): Flow<JsonRpcRequest> = channelFlow {
        try {
            mutex.withLock {
                handlers.getOrPut(method) { mutableSetOf() } += channel
            }
            awaitCancellation()
        } finally {
            mutex.withLock { handlers.getValue(method) -= channel }
        }
    }.buffer(UNLIMITED)
}
