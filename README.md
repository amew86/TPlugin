# TPlugin

[中文说明](./README_CN.md)

An effective way to build connections belong components by one standard.

- light weighted, decoupling components using just two simple annotation.
- either synchronous invoking and asynchronous invoking by declaring once
- kotlin only

### How to use

- Sample for component declarer
```kotlin
@TPlugin(pluginName = "sample")
class SamplePlugin: ITPlugin {
    @TFunc
    fun syncFun(params: Map<String, Any?>?): Map<String, Any?> {
        // returns result ...
    }
    
    @TFunc
    fun asyncFun(
        params: Map<String, Any?>?,
        successCallback: ((Map<String, Any?>) -> Unit)?,
        failureCallback: ((Exception?) -> Unit)?
    ) {
        // async callback returns
    }
}
```

- Sample for caller
```kotlin
// simple init
PluginRouter.init()
```

```kotlin
// way one for both sync and async declare
try {
    val params = HashMap<String, Any?>()
    val result = PluginRouter.syncRoute("sample", "syncFunc", params)
    // result solve
} catch (e: Exception) {
    e.printStackTrace()
}
// DO be careful for thread blocking, if this is a suspend function, you can use this in coroutine/flow 
try {
    val params = HashMap<String, Any?>()
    val result = PluginRouter.syncRoute("sample", "asyncFunc", params)
    // result solve
} catch (e: Exception) {
    e.printStackTrace()
}

// way two for both sync and async declare
val params = HashMap<String, Any?>()
PluginRouter.asyncInvoke("sample", "syncFunc", params, {
    // success callback                                                   
}, {
    // failure callback
})

val params = HashMap<String, Any?>()
PluginRouter.asyncInvoke("sample", "asyncFunc", params, {
    // success callback                                                   
}, {
    // failure callback
})

```
