package me.y9san9.jsonrpc

import me.y9san9.jsonrpc.serializable.JsonRpcErrorCodeSerializable

/**
 * When a rpc call encounters an error, response contain the error member. This
 * is a Number that indicates the error type that occurred.
 */
@JvmInline
public value class JsonRpcErrorCode(public val int: Int) {

    /**
     * Convert this type to a serializable [JsonRpcErrorCodeSerializable]
     * variant.
     */
    public fun serializable(): JsonRpcErrorCodeSerializable {
        return JsonRpcErrorCodeSerializable(int)
    }

    public companion object {
        /**
         * Invalid JSON was received by the server. An error occurred on the
         * server while parsing the JSON text.
         */
        public val ParseError: JsonRpcErrorCode = JsonRpcErrorCode(int = -32700)

        /**
         * Invalid JSON was received by the server. An error occurred on the
         * server while parsing the JSON text.
         */
        public val InvalidRequest: JsonRpcErrorCode =
            JsonRpcErrorCode(int = -32600)

        /** The method does not exist / is not available. */
        public val MethodNotFound: JsonRpcErrorCode =
            JsonRpcErrorCode(int = -32601)

        /** Invalid method parameter(s). */
        public val InvalidParams: JsonRpcErrorCode =
            JsonRpcErrorCode(int = -32602)

        /** Internal JSON-RPC error. */
        public val InternalError: JsonRpcErrorCode =
            JsonRpcErrorCode(int = -32603)

        /** Reserved for implementation-defined server-errors. */
        public fun serverErrorOrThrow(int: Int): JsonRpcErrorCode {
            require(int in -32000..-32099) {
                "Server error is -32000 to -32099"
            }
            return JsonRpcErrorCode(int = -32000)
        }
    }
}
