package me.y9san9.jsonrpc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.longOrNull
import me.y9san9.jsonrpc.serializable.JsonRpcRequestIdSerializable

/**
 * An identifier established by the Client that MUST contain a String, Number,
 * or NULL value if included. If it is not included it is assumed to be a
 * notification. The value SHOULD normally not be Null.
 *
 * The Server MUST reply with the same value in the Response object if included.
 * This member is used to correlate the context between the two objects.
 */
public sealed interface JsonRpcRequestId {
    /**
     * Convert this type to a serializable [JsonRpcRequestIdSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcRequestIdSerializable {
        val primitive =  when (this) {
            is String -> JsonPrimitive(string)
            is Long -> JsonPrimitive(long)
        }
        return JsonRpcRequestIdSerializable(primitive)
    }

    /**
     * A unique String that identifies requests and responses. Might be useful
     * with UUIDs.
     */
    public data class String(val string: kotlin.String) : JsonRpcRequestId

    /**
     * A unique Long that identifies requests and responses. There is a simple
     * counter builtin [JsonRpc] that just increments them one by one.
     */
    public data class Long(val long: kotlin.Long) : JsonRpcRequestId
}

/**
 * Converts request id to response id when identifier is no longer used for
 * request.
 */
public fun JsonRpcRequestId.toResponseId(): JsonRpcResponseId {
    return when (this) {
        is JsonRpcRequestId.String -> JsonRpcResponseId.String(string)
        is JsonRpcRequestId.Long -> JsonRpcResponseId.Long(long)
    }
}
