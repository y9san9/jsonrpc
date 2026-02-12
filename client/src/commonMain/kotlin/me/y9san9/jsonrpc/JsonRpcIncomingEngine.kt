package me.y9san9.jsonrpc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import me.y9san9.jsonrpc.serializable.JsonRpcRequestSerializable
import me.y9san9.jsonrpc.serializable.JsonRpcResponseSerializable

internal class JsonRpcIncomingEngine(
    private val backgroundScope: CoroutineScope,
    private val config: JsonRpcConfig,
    private val transport: JsonRpcTransport,
) {
    private val _requests = MutableSharedFlow<JsonRpcRequest>()
    val requests: SharedFlow<JsonRpcRequest> = _requests.asSharedFlow()

    private val _responses = MutableSharedFlow<JsonRpcResponse>()
    val responses: SharedFlow<JsonRpcResponse> = _responses.asSharedFlow()

    fun start() {
        launchNow {
            while (true) {
                val string = transport.receive()
                handleIncoming(string)
            }
        }
    }

    private suspend fun handleIncoming(string: String) {
        val element = config.json.parseToJsonElement(string)

        val json =
            when (element) {
                is JsonArray -> element
                is JsonObject -> listOf(element)
                else ->
                    throw JsonRpcTransportException(
                        "Unknown structure received: $element",
                    )
            }

        val responsesCause = handleResponses(json)
        if (responsesCause == null) return

        val requestsCause = handleRequests(json)
        if (requestsCause == null) return

        throw JsonRpcTransportException(
            """
            Coudln't handle $element

            Responses cause: $responsesCause

            Requests cause: $requestsCause
            """
                .trimIndent(),
        )
    }

    private fun handleResponses(json: List<JsonElement>): Throwable? {
        val serializable: List<JsonRpcResponseSerializable> =
            try {
                json.map { element ->
                    config.json.decodeFromJsonElement(element)
                }
            } catch (e: SerializationException) {
                return e
            }
        val responses = serializable.map { response -> response.typed() }
        for (response in responses) {
            launchNow { _responses.emit(response) }
        }
        return null
    }

    private fun handleRequests(json: List<JsonElement>): Throwable? {
        val serializable: List<JsonRpcRequestSerializable> =
            try {
                json.map { element ->
                    config.json.decodeFromJsonElement(element)
                }
            } catch (e: SerializationException) {
                return e
            }
        val requests = serializable.map { request -> request.typed() }
        for (request in requests) {
            launchNow { _requests.emit(request) }
        }
        return null
    }

    private inline fun launchNow(
        crossinline block: suspend CoroutineScope.() -> Unit,
    ): Job = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
        block()
    }
}
