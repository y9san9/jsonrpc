package me.y9san9.jsonrpc

import me.y9san9.jsonrpc.serializable.JsonRpcRequestSerializable

/**
 * A rpc call is represented by sending a [JsonRpcRequest] object to a Server.
 */
public sealed interface JsonRpcRequest : JsonRpcMessage {
    public val jsonrpc: JsonRpcVersion
    public val method: JsonRpcMethodName
    public val params: JsonRpcParams?
    public val id: JsonRpcRequestId?

    /**
     * Convert this type to a serializable [JsonRpcRequestSerializable] variant.
     */
    public fun serializable(): JsonRpcRequestSerializable {
        return JsonRpcRequestSerializable(
            jsonrpc = jsonrpc.serializable(),
            method = method.serializable(),
            params = params?.serializable(),
            id = id?.serializable(),
        )
    }
}
