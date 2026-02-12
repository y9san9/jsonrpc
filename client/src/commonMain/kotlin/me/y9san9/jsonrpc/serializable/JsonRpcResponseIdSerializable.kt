package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import me.y9san9.jsonrpc.JsonRpcResponseId

/**
 * Serializable variant of [JsonRpcResponseId]. Read the documentation there.
 */
@Serializable
@JvmInline
public value class JsonRpcResponseIdSerializable(
    public val jsonPrimitive: JsonPrimitive,
) {
    init {
        check()
    }

    private fun check() {
        if (jsonPrimitive.isString) return
        if (jsonPrimitive.longOrNull != null) return
        if (jsonPrimitive is JsonNull) return
        throw SerializationException("Must be either String, Long or null")
    }

    /** Converts this to type-safe version [JsonRpcResponseId]. */
    public fun typed(): JsonRpcResponseId {
        if (jsonPrimitive.isString) {
            return JsonRpcResponseId.String(jsonPrimitive.content)
        }
        if (jsonPrimitive.longOrNull != null) {
            return JsonRpcResponseId.Long(jsonPrimitive.long)
        }
        if (jsonPrimitive is JsonNull) {
            return JsonRpcResponseId.Null
        }
        error("Unreachable code")
    }
}
