package me.y9san9.jsonrpc

import me.y9san9.jsonrpc.serializable.JsonRpcErrorMessageSerializable

/**
 * When a rpc call encounters an error, response contain the error member. This
 * is a String providing a short description of the error. The message SHOULD be
 * limited to a concise single sentence.
 */
@JvmInline
public value class JsonRpcErrorMessage(public val string: String) {
    /**
     * Convert this type to a serializable [JsonRpcErrorMessageSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcErrorMessageSerializable {
        return JsonRpcErrorMessageSerializable(string)
    }
}
