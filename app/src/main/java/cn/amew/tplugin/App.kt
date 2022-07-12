package cn.amew.tplugin

import android.app.Application

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 9:28 上午
 * Update Date:
 * Modified By:
 * Description:
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()

        PluginRouter.init()
    }
}