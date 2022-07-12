package cn.amew.tplugin.annotation

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 8:24
 * Update Date:
 * Modified By:
 * Description:
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class TPlugin(val pluginName: String)