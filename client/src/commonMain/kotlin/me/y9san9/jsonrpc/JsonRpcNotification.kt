package me.y9san9.jsonrpc

/**
 * Notification is basically a method that has no [id]. Since it has no
 * id, you will receive nothing in reply.
 */
public data class JsonRpcNotification(
    override val method: JsonRpcMethodName,
    override val params: JsonRpcParams? = null,
    override val jsonrpc: JsonRpcVersion = JsonRpcVersion.Version_2_0,
) : JsonRpcRequest {
    override val id: Nothing? get() = null
}
