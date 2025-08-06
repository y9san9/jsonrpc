package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import me.y9san9.jsonrpc.JsonRpcResponse

/** Serializable variant of [JsonRpcResponse]. Read the documentation there. */
@Serializable
public data class JsonRpcResponseSerializable(
    val jsonrpc: JsonRpcVersionSerializable,
    val id: JsonRpcResponseIdSerializable,
    val result: JsonElement? = null,
    val error: JsonRpcErrorSerializable? = null,
) {
    init {
        check()
    }

    private fun check() {
        if (result != null && error != null) {
            throw SerializationException("Can't parse both result and error")
        }
        if (result == null && error == null) {
            throw SerializationException(
                "At least result or error should be present"
            )
        }
    }

    /**
     * Serializable variant of [JsonRpcResponse]. Read the documentation there.
     */
    public fun typed(): JsonRpcResponse {
        return if (result != null) {
            JsonRpcResponse.Success(
                jsonrpc = jsonrpc.typed(),
                id = id.typed(),
                result = result,
            )
        } else if (error != null) {
            JsonRpcResponse.Error(
                jsonrpc = jsonrpc.typed(),
                id = id.typed(),
                error = error.typed(),
            )
        } else {
            error("Unreachable code")
        }
    }
}
