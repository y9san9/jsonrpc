package me.y9san9.jsonrpc

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.y9san9.jsonrpc.serializable.JsonRpcParamsSerializable

/**
 * A Structured value that holds the parameter values to be used during the
 * invocation of the method.
 */
public sealed interface JsonRpcParams {
    public val json: JsonElement

    /**
     * Convert this type to a serializable [JsonRpcParamsSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcParamsSerializable {
        return when (this) {
            is Object -> JsonRpcParamsSerializable(json)
            is Array -> JsonRpcParamsSerializable(json)
        }
    }

    /**
     * A Structured value that holds the parameter values to be used during the
     * invocation of the method.
     */
    @JvmInline
    public value class Object(
        override val json: JsonObject,
    ) : JsonRpcParams

    /**
     * A Structured value that holds the parameter values to be used during the
     * invocation of the method.
     */
    @JvmInline
    public value class Array(
        override val json: JsonArray,
    ) : JsonRpcParams
}
