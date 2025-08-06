package me.y9san9.jsonrpc.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.serialization.json.Json
import me.y9san9.jsonrpc.JsonRpc
import me.y9san9.jsonrpc.JsonRpcConfig
import me.y9san9.jsonrpc.JsonRpcSide

/** Creates a jsonrpc connector using ktor-client. */
public fun JsonRpc.Companion.websocket(
    url: String,
    httpClient: HttpClient = HttpClient(CIO),
    json: Json = Json,
    request: HttpRequestBuilder.() -> Unit = {},
): JsonRpc.Connector {
    val transport =
        KtorClientJsonRpcTransport.Connector(
            url = url,
            httpClient = httpClient,
            request = request,
        )
    val config = JsonRpcConfig(json = json, side = JsonRpcSide.Client)
    return JsonRpc.Connector(transport, config)
}
