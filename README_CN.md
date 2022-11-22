# TPlugin

一个帮助Android原生开发者快速有效地实现组件间路由通信的类库

- 轻量级.仅通过两个注解类、一个接口类即可完成单一封闭组件的声明
- 在一次声明下同时支持同步调用与异步调用


### 如何使用

- 组件声明示例

```kotlin
@TPlugin(pluginName = "sample")
class SamplePlugin: ITPlugin {

    /**
     * 同步方法声明
     */
    @TFunc
    fun syncFun(params: Map<String, Any?>?): Map<String, Any?> {
        // 同步返回预期结果
    }

    /**
     * 异步方法声明
     */
    @TFunc
    fun asyncFun(
        params: Map<String, Any?>?,
        successCallback: ((Map<String, Any?>) -> Unit)?,
        failureCallback: ((Exception?) -> Unit)?
    ) {
        // 通过异步回调返回预期结果
    }
}
```

- 组件调用者示例

```kotlin
// 初始化
PluginRouter.init()
```

```kotlin
// 第一种调用方法: 直接同步调用
// 注意: 在同步调用时, 如果声明者使用了suspend挂起函数可能会引起线程的阻塞, 此时使用时应特别小心
// 或者也可以在外部使用coroutine/flow等进行包装

// 同步方法的同步调用
kotlin.runCatching { 
    val params = hashMapOf(
        "paramA" to "valueA",
        "paramB" to 123123
    )
    val result = PluginRouter.syncRoute("sample", "syncFunc", params)
    // 后续result的处理逻辑
}.onFailure {
    // 错误处理逻辑
}

// 异步方法的同步调用
kotlin.runCatching {
    val params = hashMapOf(
        "paramA" to "valueA",
        "paramB" to 123123
    )
    val result = PluginRouter.syncRoute("sample", "asyncFunc", params)
    // 后续result的处理逻辑
}.onFailure {
    // 错误处理逻辑
}
```

```kotlin
// 第二种使用方法,采用异步调用方法

// 同步方法的异步调用
val params = HashMap<String, Any?>()
PluginRouter.asyncInvoke("sample", "syncFunc", params, {
    // success callback                                                   
}, {
    // failure callback
})

// 异步方法的异步调用
val params = HashMap<String, Any?>()
PluginRouter.asyncInvoke("sample", "asyncFunc", params, {
    // success callback                                                   
}, {
    // failure callback
})
```