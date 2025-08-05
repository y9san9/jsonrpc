package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import me.y9san9.jsonrpc.JsonRpcErrorCode


/**
 * Serializable variant of [JsonRpcErrorCode].
 * Read the documentation there.
 */
@Serializable
@JvmInline
public value class JsonRpcErrorCodeSerializable(public val int: Int) {

    /**
     * Converts this to type-safe version [JsonRpcErrorCode].
     */
    public fun typed(): JsonRpcErrorCode {
        return JsonRpcErrorCode(int)
    }

}
