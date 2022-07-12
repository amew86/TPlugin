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
class SamplePlugin: ITPlugin {

    @TFunc
    fun syncTest1(params: Map<String, Any?>?): Map<String, Any?> {
        val resultMap = HashMap<String, Any?>()
        params?.entries?.forEach {
            resultMap[it.key] = it.value
        }
        resultMap["sync"] = "testSuccess"
        return resultMap
    }

    @TFunc(funName = "myFunName")
    fun asyncTest1(lifecycleOwner: LifecycleOwner?, params: Map<String, Any?>?) {
        Log.i("test", "asyncTest")
    }
}