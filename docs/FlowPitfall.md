There is a big pitfall of Kotlin Flows that you might not know about:

Consider the following example:

```kotlin
rpc.incoming.messagesRaw.onEach { message ->
    println(message)
}.launchIn(scope)

rpc.execute(...) // Send jsonrpc request to subscribe to something
```

You actually have a slight chance to miss some messages. It's related
to the fact that you don't have any guarantees when launchIn will
be actually executed.
Those chances are so low that I only got those under great pressure and
high RPC. And, believe me, this is a hell to debug. You can rewrite that
code to something like this, if you want guarantees that you will catch
every element:

```kotlin
scope.launch(start = CoroutineStart.UNDISPATCHED) {
    rpc.incoming.messagesRaw.collect { message ->
        println(message)
    }
}

rpc.execute(...) // Send jsonrpc request only after launch was called
```

Undispatched makes it so the task is dispatched on dispatcher only after
first suspend point is reached (`collect` in this case).

But I highly discourage you from doing this, since it's easy to forget
and mess up, but the API is here. What you can do instead is use
other functions in this class which provide safe lifecycle and
guarantees that no elements can be dropped. It's less convenient, but
it will not bring you sleepless nights trying to find that one race.
yes, it's personal.
