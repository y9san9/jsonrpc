package me.y9san9.jsonrpc

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import me.y9san9.jsonrpc.serializable.JsonRpcVersionSerializable

/**
 * Header that must be always present and this helps to
 * distinguish between jsonrpc 1.0 (unsupported in this library)
 * and jsonrpc 2.0.
 */
public sealed interface JsonRpcVersion {

    /**
     * Convert this type to a serializable [JsonRpcVersionSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcVersionSerializable {
        return when (this) {
            Version_2_0 -> JsonRpcVersionSerializable.VERSION_2_0
        }
    }

    /**
     * Header that must be always present and this helps to
     * distinguish between jsonrpc 1.0 (unsupported in this library)
     * and jsonrpc 2.0.
     */
    public data object Version_2_0 : JsonRpcVersion
}
