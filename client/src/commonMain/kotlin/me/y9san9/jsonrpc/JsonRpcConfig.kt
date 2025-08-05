package me.y9san9.jsonrpc

import kotlinx.serialization.json.Json

/**
 * [JsonRpcConfig] has a number of settings which mostly have
 * sensible defaults, but are up to you to decide their actual values.
 */
// TODO: move it into a bag of tags
public class JsonRpcConfig(
    public val side: JsonRpcSide,
    public val json: Json = Json,
    public val batchRequests: Boolean = true,
)
