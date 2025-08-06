package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import me.y9san9.jsonrpc.JsonRpcRequestId

/** Serializable variant of [JsonRpcRequestId]. Read the documentation there. */
@Serializable
@JvmInline
public value class JsonRpcRequestIdSerializable(
    public val jsonPrimitive: JsonPrimitive
) {
    init {
        check()
    }

    private fun check() {
        if (jsonPrimitive.isString) return
        if (jsonPrimitive.longOrNull != null) return
        throw SerializationException("Must be either String or Long")
    }

    /** Converts this to type-safe version [JsonRpcRequestId]. */
    public fun typed(): JsonRpcRequestId {
        if (jsonPrimitive.isString) {
            return JsonRpcRequestId.String(jsonPrimitive.content)
        }
        if (jsonPrimitive.longOrNull != null) {
            return JsonRpcRequestId.Long(jsonPrimitive.long)
        }
        error("Unreachable code")
    }
}
