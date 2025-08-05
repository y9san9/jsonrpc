# Json RPC

Idiomatic Json RPC Implementation in Kotlin and kotlinx.serialization.
It has ktor implementation, but more engines may be added if needed. It is
really simple to implement, but annoying to do every time for every project,
so I made this into library.

## Example

```kotlin
import me.y9san9.jsonrpc.JsonRpc
import me.y9san9.jsonrpc.ktor.ktor

suspend fun main() {
    val rpc = JsonRpc.websocket("wss://example.org")

    rpc.connect {
        val response = execute(...)

        incoming.onRequest { request ->
            println("Incoming Request: $request")
            respond(response)
        }

        incoming.onRequest(JsonRpcMethodName("test")) { /* ... */ }
    }
}
```

## Install

Library is available on Maven Central and may be installed in the following
ways:

```kotlin
dependencies {
    implementation("me.y9san9.jsonrpc:ktor-client:$version")
}
```

```toml
[versions]
jsonrpc = "$version"

[libraries]
jsonrpc = { module = "me.y9san9.jsonrpc", version.ref = "jsonrpc" }
```

`$version` should be the same as the last version in releases section.

## Server-Client

TODO: at the moment only client behaviour is supported. Since I myself
don't have any jsonrpc server to build with this. What I want to add
for server is different errors that Server will tell to Client if Client
did mistaked.

And, of course, an intergration with ktor-server is still missing, but it's
not hard to master. I also would want to introduce dsl for ktor-server that
looks like this:

```kotlin
embeddedServer {
    jsonrpc { this: JsonRpcServer
        // launch a coroutine to respond to methods
        method("test") { call: JsonRpcCall ->
            call.respond(/* ... */)
            call.notification(/* ... */)
        }
        // this.jsonrpc works fine
    }
}
```
