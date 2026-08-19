package me.y9san9.jsonrpc

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonPrimitive
import java.io.Closeable
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

internal class JsonRpcResponseRegistrationTest {
    @Test
    fun `registers response before sending request`() {
        LifoDispatcher().use { dispatcher ->
            val transport = TestTransport()

            val result = runBlocking(dispatcher) {
                withTimeout(1_000) {
                    connector(transport).connect {
                        execute(request())
                    }
                }
            }

            val response = assertIs<JsonRpc.Result.Success<JsonRpcResponse>>(
                result,
            ).value
            assertEquals(JsonRpcResponseId.Long(1), response.id)
            assertEquals(JsonPrimitive("ok"), response.result)
        }
    }

    @Test
    fun `registers every batch response before sending request`() {
        LifoDispatcher().use { dispatcher ->
            val transport = TestTransport()

            val result = runBlocking(dispatcher) {
                withTimeout(1_000) {
                    connector(transport).connect {
                        execute(listOf(request(1), request(2)))
                    }
                }
            }

            val responses =
                assertIs<JsonRpc.Result.Success<List<JsonRpcResponse>>>(
                    result,
                ).value
            assertEquals(
                listOf(
                    JsonRpcResponseId.Long(1),
                    JsonRpcResponseId.Long(2),
                ),
                responses.map { response -> response.id },
            )
        }
    }

    @Test
    fun `removes registration when send fails`(): Unit = runBlocking {
        val transport = TestTransport(mode = SendMode.Fail)

        val result = connector(transport).connect {
            val failure = try {
                execute(request())
                null
            } catch (exception: Exception) {
                exception
            }
            assertIs<TestSendException>(failure)

            transport.mode = SendMode.Respond
            withTimeout(1_000) {
                execute(request())
            }
        }

        assertIs<JsonRpc.Result.Success<JsonRpcResponse>>(result)
    }

    @Test
    fun `removes registration when caller is cancelled`(): Unit = runBlocking {
        val transport = TestTransport(mode = SendMode.Ignore)

        val result = connector(transport).connect {
            val job = launch {
                execute(request())
            }
            transport.awaitSend()
            job.cancelAndJoin()

            transport.mode = SendMode.Respond
            withTimeout(1_000) {
                execute(request())
            }
        }

        assertIs<JsonRpc.Result.Success<JsonRpcResponse>>(result)
    }

    private fun request(id: Long = 1): JsonRpcMethod = JsonRpcMethod(
        id = JsonRpcRequestId.Long(id),
        method = JsonRpcMethodName("test"),
    )

    private fun connector(transport: TestTransport): JsonRpc.Connector =
        JsonRpc.Connector(
            transport = TestTransportConnector(transport),
            config = JsonRpcConfig(side = JsonRpcSide.Client),
        )
}

private enum class SendMode {
    Respond,
    Ignore,
    Fail,
}

private class TestTransportConnector(private val transport: TestTransport) :
    JsonRpcTransport.Connector {
    override suspend fun <T> connect(
        block: suspend JsonRpcTransport.() -> T,
    ): JsonRpcTransport.Result<T> =
        JsonRpcTransport.Result.Success(transport.block())
}

private class TestTransport(@Volatile var mode: SendMode = SendMode.Respond) :
    JsonRpcTransport {
    private val incoming = Channel<IncomingMessage>(Channel.UNLIMITED)
    private val sends = Channel<Unit>(Channel.UNLIMITED)
    override val isActive: StateFlow<Boolean> = MutableStateFlow(true)

    override suspend fun send(data: String) {
        sends.send(Unit)
        when (mode) {
            SendMode.Respond -> {
                val ids = Regex("\\\"id\\\":(\\d+)")
                    .findAll(data)
                    .map { match -> match.groupValues[1] }
                    .toList()
                assertNotNull(ids.firstOrNull())
                val responses = ids.reversed().map { id ->
                    """{"jsonrpc":"2.0","id":$id,"result":"ok"}"""
                }
                val message = IncomingMessage(
                    data = if (responses.size == 1) {
                        responses.single()
                    } else {
                        responses.joinToString(prefix = "[", postfix = "]")
                    },
                )
                incoming.send(message)
                message.read.await()
            }
            SendMode.Ignore -> Unit
            SendMode.Fail -> throw TestSendException()
        }
    }

    override suspend fun receive(): String {
        val message = incoming.receive()
        message.read.complete(Unit)
        return message.data
    }

    suspend fun awaitSend() {
        sends.receive()
    }
}

private data class IncomingMessage(
    val data: String,
    val read: CompletableDeferred<Unit> = CompletableDeferred(),
)

private class TestSendException : Exception()

private class LifoDispatcher :
    CoroutineDispatcher(),
    Closeable {
    private val tasks = LinkedBlockingDeque<Runnable>()
    private val running = AtomicBoolean(true)
    private val thread = Thread {
        while (running.get()) {
            try {
                tasks.takeLast().run()
            } catch (exception: InterruptedException) {
                if (running.get()) throw exception
            }
        }
    }.apply {
        name = "jsonrpc-test-lifo-dispatcher"
        isDaemon = true
        start()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        tasks.putLast(block)
    }

    override fun close() {
        running.set(false)
        thread.interrupt()
    }
}
