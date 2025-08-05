package me.y9san9.jsonrpc.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import me.y9san9.jsonrpc.JsonRpcVersion

/**
 * Serializable variant of [JsonRpcVesrion].
 * Read the documentation there.
 */
@Serializable
public enum class JsonRpcVersionSerializable {
    @SerialName("2.0")
    VERSION_2_0;

    /**
     * Converts this to type-safe [JsonRpcVersion].
     */
    public fun typed(): JsonRpcVersion {
        return JsonRpcVersion.Version_2_0
    }
}
