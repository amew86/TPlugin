package cn.amew.tplugin

import androidx.lifecycle.LifecycleOwner
import cn.amew.tplugin.protocol.ITPlugin
import cn.amew.tplugin.protocol.ITPluginWrapper
import java.util.*

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 8:53
 * Update Date:
 * Modified By:
 * Description: main entrance
 */
object PluginRouter {

    private val plugins = HashMap<String, ITPluginWrapper<LifecycleOwner>>()

    /**
     * initialize plugins by ServiceLoader
     */
    @Suppress("UNCHECKED_CAST")
    fun init() {
        ServiceLoader.load(ITPlugin::class.java).forEach { tPlugin ->
            try {
                val wrapper = Class.forName("cn.amew.plugin.wrapper.${tPlugin.javaClass.simpleName}Wrapper")
                    .getConstructor()
                    .newInstance() as ITPluginWrapper<LifecycleOwner>
                wrapper.injectPlugin(tPlugin)
                val pluginName = wrapper.providePluginName()
                if (pluginName.isNotEmpty()) {
                    plugins[pluginName] = wrapper
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * trunk to invoke plugins functions by synchronous way
     */
    fun syncInvoke(
        lifecycleOwner: LifecycleOwner?,
        pluginName: String,
        funName: String,
        params: Map<String, Any?>?
    ) = plugins[pluginName]?.syncInvoke(lifecycleOwner, funName, params)

    /**
     * trunk to invoke plugins functions by asynchronous way
     */
    fun asyncInvoke(
        lifecycleOwner: LifecycleOwner?,
        pluginName: String,
        funName: String,
        params: Map<String, Any?>?,
        successCallback: ((Map<String, Any?>?) -> Unit)?,
        failureCallback: ((Exception) -> Unit)?
    ) {
        plugins[pluginName]?.asyncInvoke(lifecycleOwner, funName, params, successCallback, failureCallback)
    }
}