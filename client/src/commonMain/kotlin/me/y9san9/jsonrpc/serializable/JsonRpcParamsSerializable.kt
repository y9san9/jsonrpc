package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.y9san9.jsonrpc.JsonRpcParams

/** Serializable variant of [JsonRpcRequest]. Read the documentation there. */
@Serializable
@JvmInline
public value class JsonRpcParamsSerializable(public val json: JsonElement) {
    init {
        check()
    }

    private fun check() {
        if (json is JsonObject) return
        if (json is JsonArray) return
        throw SerializationException("JsonRpcParams must a structure")
    }

    /** Converts this to type-safe [JsonRpcParams]. */
    public fun typed(): JsonRpcParams {
        if (json is JsonObject) return JsonRpcParams.Object(json)
        if (json is JsonArray) return JsonRpcParams.Array(json)
        error("Unreachable code")
    }
}
