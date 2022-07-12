package cn.amew.tplugin.protocol

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 8:32
 * Update Date:
 * Modified By:
 * Description:
 */
interface ITPluginWrapper<L> {

    fun providePluginName(): String

    /**
     * inject plugins when init to avoid twice reflect
     */
    fun injectPlugin(plugin: ITPlugin)

    /**
     * invoke functions by synchronous way
     */
    @Throws(Exception::class)
    fun syncInvoke(lifecycle: L?, funName: String, params: Map<String, Any?>?): Map<String, Any?>?

    /**
     * trunk to invoke plugins functions by asynchronous way
     */
    fun asyncInvoke(
        lifecycle: L?, funName: String, params: Map<String, Any?>?,
        successCallback: ((Map<String, Any?>?) -> Unit)?,
        failureCallback: ((Exception) -> Unit)?
    )
}