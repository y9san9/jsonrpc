package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.y9san9.jsonrpc.JsonRpcError

/** Serializable variant of [JsonRpcError]. Read the documentation there. */
@Serializable
public data class JsonRpcErrorSerializable(
    val code: JsonRpcErrorCodeSerializable,
    val message: JsonRpcErrorMessageSerializable,
    val data: JsonElement? = null,
) {

    /** Converts this to type-safe version [JsonRpcError]. */
    public fun typed(): JsonRpcError = JsonRpcError(
        code = code.typed(),
        message = message.typed(),
        data = data,
    )
}
