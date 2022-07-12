package cn.amew.tplugin.annotation

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 8:28
 * Update Date:
 * Modified By:
 * Description:
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class TFunc(
    val funName: String = "",
    val timeout: Long = -1L
)