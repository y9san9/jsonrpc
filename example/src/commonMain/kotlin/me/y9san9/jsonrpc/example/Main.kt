package me.y9san9.jsonrpc.example

import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.y9san9.jsonrpc.JsonRpc
import me.y9san9.jsonrpc.JsonRpcMethod
import me.y9san9.jsonrpc.JsonRpcMethodName
import me.y9san9.jsonrpc.ktor.websocket

@Serializable
data class SetHeartbeatParams(val interval: Int)

suspend fun JsonRpc.setHeartbeat(interval: Int) {
    val id = nextId()
    val name = JsonRpcMethodName("public/set_heartbeat")
    val params = SetHeartbeatParams(interval)
    val encodedParams = encodeParams(params)
    val method = JsonRpcMethod(id, name, encodedParams)
    execute(method)
}

suspend fun JsonRpc.test() {
    val id = nextId()
    val name = JsonRpcMethodName("public/test")
    val method = JsonRpcMethod(id, name)
    execute(method)
}

@Serializable
data class Heartbeat(val type: String)

suspend fun main() {
    val json = Json {
        ignoreUnknownKeys = true
    }
    val rpc = JsonRpc.websocket(
        url = "wss://test.deribit.com/ws/api/v2",
        json = json,
    )
    // todo: work on notifications
    // Maybe something like: JsonRpcIncomingNotification<T>(val method: String, val dataType: KType)
    val result = rpc.connect {
        val name = JsonRpcMethodName("heartbeat")
        incoming.onRequest(name) { method ->
            val heartbeat: Heartbeat = decodeParams(method)
            if (heartbeat.type == "test_request") {
                test()
            }
            println(method)
        }

        print("Test: ")
        println(setHeartbeat(interval = 10))

        backgroundScope.launch {
            // while (true) {}
        }

        scope.coroutineContext.job.invokeOnCompletion {
            println(">0 Something's happening $it")
        }

        // val book: Flow<JsonRpcRequest> = incoming.notifications("book.ETH-PERPETUAL.100.1.100ms")
        // subscribe("book.ETH-PERPETUAL.100.1.100ms")
        // book.take(10).collect { }
        // unsubscribe("book.ETH-PERPETUAL.100.1.100ms")

        // incoming.requests { flow ->
        //     flow.take(0).collect {
        //
        //     }
        // }
    }
    println(result)
}
