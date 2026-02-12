package me.y9san9.jsonrpc

import kotlinx.serialization.json.JsonElement
import me.y9san9.jsonrpc.serializable.JsonRpcErrorSerializable

/**
 * When a rpc call encounters an error, [JsonRpcResponse] MUST contain the
 * [error][JsonRpcResponse.error] member.
 */
public data class JsonRpcError(
    val code: JsonRpcErrorCode,
    val message: JsonRpcErrorMessage,
    val data: JsonElement?,
) {
    /**
     * Convert this type to a serializable [JsonRpcErrorSerializable] variant.
     */
    public fun serializable(): JsonRpcErrorSerializable =
        JsonRpcErrorSerializable(
            code = code.serializable(),
            message = message.serializable(),
            data = data,
        )
}
