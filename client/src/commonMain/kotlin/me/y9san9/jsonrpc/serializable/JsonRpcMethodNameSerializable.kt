package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import me.y9san9.jsonrpc.JsonRpcMethodName

/**
 * Serializable variant of [JsonRpcMethodName]. Read the documentation there.
 */
@Serializable
@JvmInline
public value class JsonRpcMethodNameSerializable(public val string: String) {
    /** Converts this to type-safe version [JsonRpcMethodName]. */
    public fun typed(): JsonRpcMethodName {
        return JsonRpcMethodName(string)
    }
}
