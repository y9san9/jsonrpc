package me.y9san9.jsonrpc

import kotlinx.serialization.json.JsonElement
import me.y9san9.jsonrpc.serializable.JsonRpcResponseSerializable

/**
 * When a rpc call is made, the Server MUST reply with a Response, except for in
 * the case of Notifications. The Response is expressed as a single JSON Object.
 */
public sealed interface JsonRpcResponse : JsonRpcMessage {
    public val jsonrpc: JsonRpcVersion
    public val id: JsonRpcResponseId
    public val result: JsonElement?
    public val error: JsonRpcError?

    /**
     * Convert this type to a serializable [JsonRpcResponseSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcResponseSerializable =
        JsonRpcResponseSerializable(
            jsonrpc = jsonrpc.serializable(),
            id = id.serializable(),
            result = result,
            error = error?.serializable(),
        )

    public fun successOrThrow(): Success = when (this) {
        is Success -> this
        is Error -> error("Can't get success from $this")
    }

    /**
     * When a rpc call is made, the Server MUST reply with a Response, except
     * for in the case of Notifications. The Response is expressed as a single
     * JSON Object.
     */
    public data class Success(
        override val id: JsonRpcResponseId,
        override val result: JsonElement,
        override val jsonrpc: JsonRpcVersion = JsonRpcVersion.Version_2_0,
    ) : JsonRpcResponse {
        override val error: Nothing?
            get() = null
    }

    /**
     * When a rpc call is made, the Server MUST reply with a Response, except
     * for in the case of Notifications. The Response is expressed as a single
     * JSON Object.
     */
    public data class Error(
        override val id: JsonRpcResponseId,
        override val error: JsonRpcError,
        override val jsonrpc: JsonRpcVersion = JsonRpcVersion.Version_2_0,
    ) : JsonRpcResponse {
        override val result: Nothing?
            get() = null
    }
}
