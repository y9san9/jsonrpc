package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import me.y9san9.jsonrpc.JsonRpcErrorMessage

/**
 * Serializable variant of [JsonRpcErrorMessage]. Read the documentation there.
 */
@Serializable
@JvmInline
public value class JsonRpcErrorMessageSerializable(public val string: String) {

    /** Converts this to type-safe version [JsonRpcErrorMessage]. */
    public fun typed(): JsonRpcErrorMessage {
        return JsonRpcErrorMessage(string)
    }
}
