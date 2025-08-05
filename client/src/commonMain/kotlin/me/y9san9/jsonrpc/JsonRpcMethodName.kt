package me.y9san9.jsonrpc

import me.y9san9.jsonrpc.serializable.JsonRpcMethodNameSerializable

/**
 * A String containing the name of the method to be invoked. Method names
 * that begin with the word rpc followed by a period character (U+002E or
 * ASCII 46) are reserved for rpc-internal methods and extensions and MUST NOT
 * be used for anything else.
 */
@JvmInline
public value class JsonRpcMethodName(public val string: String) {
    /**
     * Convert this type to a serializable [JsonRpcMethodNameSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcMethodNameSerializable {
        return JsonRpcMethodNameSerializable(string)
    }
}
