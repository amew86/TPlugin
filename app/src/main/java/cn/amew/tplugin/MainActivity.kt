package cn.amew.tplugin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import cn.amew.tplugin.databinding.ActivityMainBinding

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 9:33
 * Update Date:
 * Modified By:
 * Description:
 */
class MainActivity : AppCompatActivity() {

    private val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.tvAsync1.setOnClickListener {
            PluginRouter.asyncInvoke(
                lifecycleOwner = null,
                pluginName = "sample",
                funName = "myFunName",
                params = hashMapOf(
                    "input" to "test"
                ),
                successCallback = {
                    it?.entries?.forEach { entry ->
                        Log.i("test", "key: ${entry.key}, value: ${entry.value}")
                    }
                },
                failureCallback = {
                    it?.printStackTrace()
                }
            )
        }
    }
}