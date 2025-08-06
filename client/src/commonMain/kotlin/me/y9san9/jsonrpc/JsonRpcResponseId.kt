package me.y9san9.jsonrpc

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import me.y9san9.jsonrpc.serializable.JsonRpcResponseIdSerializable

/**
 * An identifier established by the Client that MUST contain a String, Number,
 * or NULL value if included. If it is not included it is assumed to be a
 * notification. The value SHOULD normally not be Null.
 *
 * The Server MUST reply with the same value in the Response object if included.
 * This member is used to correlate the context between the two objects.
 */
public sealed interface JsonRpcResponseId {
    /**
     * Convert this type to a serializable [JsonRpcResponseIdSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcResponseIdSerializable {
        val jsonPrimitive =
            when (this) {
                is String -> JsonPrimitive(string)
                is Long -> JsonPrimitive(long)
                is Null -> JsonNull
            }
        return JsonRpcResponseIdSerializable(jsonPrimitive)
    }

    /**
     * A unique String that identifies requests and responses. Might be useful
     * with UUIDs.
     */
    public data class String(val string: kotlin.String) : JsonRpcResponseId

    /**
     * A unique Long that identifies requests and responses. There is a simple
     * counter builtin [JsonRpc] that just increments them one by one.
     */
    public data class Long(val long: kotlin.Long) : JsonRpcResponseId

    /**
     * Null is ONLY used for responses to requests without any valid ID. Do not
     * use it for notifications.
     */
    public data object Null : JsonRpcResponseId
}
