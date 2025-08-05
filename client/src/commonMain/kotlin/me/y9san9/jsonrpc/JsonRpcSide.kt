package me.y9san9.jsonrpc

/**
 * Whether [JsonRpc] acts as a client or server. These terms are
 * used in the JSON-RPC 2.0 Specification and the library will act
 * differently an a specific set of cases depending on that information.
 *
 * Check inheritors for more information.
 */
public sealed interface JsonRpcSide {
    /**
     * Server Side implementation will not throw exceptions if client provides
     * data that appears to be invalid. Instead it will respond with the error
     * object describing what went wrong.
     */
    public data object Server : JsonRpcSide

    /**
     * Client Side implementation will throw exceptions if server provides data
     * that client cannot understand properly.
     */
    public data object Client : JsonRpcSide
}
