package cn.amew.tplugin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.amew.tplugin.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 9:33
 * Update Date:
 * Modified By:
 * Description:
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(LayoutInflater.from(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.tvAsync1.setOnClickListener { view ->
            PluginRouter.asyncInvoke(null,"sample", "myFunName",
                hashMapOf(
                    "input" to "test"
                ),
                {
                    it?.entries?.forEach { entry ->
                        Log.i("test", "key: ${entry.key}, value: ${entry.value}")
                    }
                    view.setBackgroundColor(Color.GREEN)
                },
                {
                    it?.printStackTrace()
                    view.setBackgroundColor(Color.RED)
                }
            )
        }

        binding.tvAsync2.setOnClickListener { view ->
            PluginRouter.asyncInvoke(
                lifecycleOwner = null,
                pluginName = "sample",
                funName = "syncTest1",
                params = hashMapOf(
                    "input" to "test"
                ),
                successCallback = {
                    it?.entries?.forEach { entry ->
                        Log.i("test", "key: ${entry.key}, value: ${entry.value}")
                    }
                    view.setBackgroundColor(Color.GREEN)
                },
                failureCallback = {
                    it?.printStackTrace()
                    view.setBackgroundColor(Color.RED)
                }
            )
        }

        binding.tvSync1.setOnClickListener { view ->
            runCatching {
                val result = PluginRouter.syncInvoke(
                    lifecycleOwner = null,
                    "sample",
                    "myFunName",
                    hashMapOf(
                        "input" to "test"
                    )
                )
                result?.entries?.forEach { entry ->
                    Log.i("test", "key: ${entry.key}, value: ${entry.value}")
                }
                view.setBackgroundColor(Color.GREEN)
            }.onFailure {
                it.printStackTrace()
                view.setBackgroundColor(Color.RED)
            }
        }

        binding.tvSync2.setOnClickListener { view ->
            runCatching {
                val result = PluginRouter.syncInvoke(
                    lifecycleOwner = null,
                    "sample",
                    "syncTest1",
                    hashMapOf(
                        "input" to "test"
                    )
                )
                result?.entries?.forEach { entry ->
                    Log.i("test", "key: ${entry.key}, value: ${entry.value}")
                }
                view.setBackgroundColor(Color.GREEN)
            }.onFailure {
                it.printStackTrace()
                view.setBackgroundColor(Color.RED)
            }
        }

        binding.tvSuspend1.setOnClickListener { view ->
            lifecycleScope.launch {
                flow {
                    val result = PluginRouter.syncInvoke(
                        lifecycleOwner = null,
                        "sample",
                        "suspendTest1",
                        hashMapOf(
                            "input" to "test"
                        )
                    )
                    emit(result)
                }
                    .flowOn(Dispatchers.IO)
                    .catch {
                        it.printStackTrace()
                        view.setBackgroundColor(Color.RED)
                    }
                    .collect {
                        it?.entries?.forEach { entry ->
                            Log.i("test", "key: ${entry.key}, value: ${entry.value}")
                        }
                        view.setBackgroundColor(Color.GREEN)
                    }

            }
        }

        binding.tvSuspend2.setOnClickListener { view ->
            lifecycleScope.launch {
                flow {
                    val result = PluginRouter.syncInvoke(
                        lifecycleOwner = null,
                        "sample",
                        "suspendTest2",
                        null
                    )
                    emit(result)
                }
                    .flowOn(Dispatchers.IO)
                    .catch {
                        it.printStackTrace()
                        view.setBackgroundColor(Color.RED)
                    }
                    .collect {
                        it?.entries?.forEach { entry ->
                            Log.i("test", "key: ${entry.key}, value: ${entry.value}")
                        }
                        view.setBackgroundColor(Color.GREEN)
                    }

            }
        }
    }
}