package me.y9san9.jsonrpc

/**
 * Method is something that requires response from the server. For that
 * purpose you must specify [id], which server will specify to respond
 * to you.
 *
 * Note that it's not guaranteed that responses will be returned in the
 * same order that you make requests as they can be parallelled.
 */
public data class JsonRpcMethod(
    override val id: JsonRpcRequestId,
    override val method: JsonRpcMethodName,
    override val params: JsonRpcParams? = null,
    override val jsonrpc: JsonRpcVersion = JsonRpcVersion.Version_2_0,
) : JsonRpcRequest
