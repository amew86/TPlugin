package cn.amew.sample.module

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import cn.amew.tplugin.annotation.TFunc
import cn.amew.tplugin.annotation.TPlugin
import cn.amew.tplugin.protocol.ITPlugin
import com.google.auto.service.AutoService

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 9:20
 * Update Date:
 * Modified By:
 * Description:
 */
@TPlugin(pluginName = "sample")
@AutoService(ITPlugin::class)
class SamplePlugin : ITPlugin {

    @TFunc
    fun syncTest1(params: Map<String, Any?>?): Map<String, Any?> {
        val resultMap = HashMap<String, Any?>()
        params?.entries?.forEach {
            resultMap[it.key] = it.value
        }
        resultMap["result"] = "success result for syncTest1"
        return resultMap
    }

    @TFunc(funName = "myFunName")
    fun asyncTest1(
        lifecycleOwner: LifecycleOwner?,
        params: Map<String, Any?>?,
        successCallback: ((Map<String, Any?>) -> Unit)?,
        failureCallback: ((Exception?) -> Unit)?
    ) {
        Log.i("test", "asyncTest")
        if (params?.get("input") == "test") {
            successCallback?.invoke(
                hashMapOf(
                    "result" to "success result for asyncTest1"
                )
            )
        } else {
            failureCallback?.invoke(IllegalArgumentException("unknown params"))
        }
    }

    @TFunc
    fun suspendTest1(
        params: Map<String, Any?>?,
    ): Map<String, Any?> {

        Thread.sleep(1000L)
        if (params?.get("input") == "test") {
            return hashMapOf(
                "result" to "overtime"
            )
        } else {
            throw IllegalArgumentException("unknown params")
        }
    }

    @TFunc
    fun suspendTest2(): Map<String, Any?> {
        Thread.sleep(2000L)
        return hashMapOf(
            "result" to "overtime"
        )
    }

    @TFunc(funName = "overrideTestOverride")
    fun overrideTestOrigin(
        params: Map<String, Any?>?,
    ): Map<String, Any?> {
        return ((params as HashMap<String, Any?>?) ?: HashMap()).apply {
            put("result", "override")
        }
    }

    @TFunc(timeout = 1200L)
    fun timeoutTest1(
        params: Map<String, Any?>?,
        successCallback: ((Map<String, Any?>) -> Unit)?,
        failureCallback: ((Exception?) -> Unit)?
    ) {
        Thread.sleep(1000L)
        successCallback?.invoke(
            hashMapOf(
                "success" to "timeout"
            )
        )
    }

    @TFunc(timeout = 1200L)
    fun timeoutTest2(
        params: Map<String, Any?>?,
        successCallback: ((Map<String, Any?>) -> Unit)?,
        failureCallback: ((Exception?) -> Unit)?
    ) {
        Thread {
            Thread.sleep(2000L)
            successCallback?.invoke(
                hashMapOf(
                    "success" to "timeout"
                )
            )
        }
    }
}