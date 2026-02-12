package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import me.y9san9.jsonrpc.JsonRpcMethod
import me.y9san9.jsonrpc.JsonRpcNotification
import me.y9san9.jsonrpc.JsonRpcRequest

/** Serializable variant of [JsonRpcRequest]. Read the documentation there. */
@Serializable
public data class JsonRpcRequestSerializable(
    val jsonrpc: JsonRpcVersionSerializable,
    val id: JsonRpcRequestIdSerializable? = null,
    val method: JsonRpcMethodNameSerializable,
    val params: JsonRpcParamsSerializable? = null,
) {
    /** Converts this to type-safe [JsonRpcRequest]. */
    public fun typed(): JsonRpcRequest = if (id == null) {
        JsonRpcNotification(
            jsonrpc = jsonrpc.typed(),
            method = method.typed(),
            params = params?.typed(),
        )
    } else {
        JsonRpcMethod(
            jsonrpc = jsonrpc.typed(),
            id = id.typed(),
            method = method.typed(),
            params = params?.typed(),
        )
    }
}
