package cn.amew.tplugin.processor

import cn.amew.tplugin.annotation.TFunc
import cn.amew.tplugin.annotation.TPlugin
import cn.amew.tplugin.protocol.ITPlugin
import cn.amew.tplugin.protocol.ITPluginWrapper
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.jvm.Throws

/**
 * Author:      A-mew
 * Create Date: Created in 2022/7/12 8:43
 * Update Date:
 * Modified By:
 * Description:
 */
@AutoService(Processor::class)
class TPluginProcessor : AbstractProcessor() {

    private var filer: Filer? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        filer = processingEnv?.filer
    }

    // TODO: uncompleted
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        roundEnv?.getElementsAnnotatedWith(TPlugin::class.java)?.forEach { element ->
            val packageName = "cn.amew.plugin.wrapper"
            val sourceClassName = element.simpleName
            val wrapperClassName = "${sourceClassName}Wrapper"

            val pluginAnnotation = element.getAnnotation(TPlugin::class.java)
            val pluginName = pluginAnnotation.pluginName

            // private lateinit var plugin: SamplePlugin
            val pluginPropertySpec = PropertySpec.builder("plugin", ClassName.bestGuess(element.asType().toString()))
                .addModifiers(KModifier.PRIVATE)
                .addModifiers(KModifier.LATEINIT)
                .mutable(true)
                .build()

            // override fun providePluginName() = "sample"
            val providePluginNameFunc = FunSpec.builder("providePluginName")
                .addModifiers(KModifier.OVERRIDE)
                .addCode("return \"$pluginName\"")
                .build()

            // override fun injectPlugin(plugin: ITPlugin) {
            //     this.plugin = plugin as SamplePlugin
            // }
            val injectPluginFunc = FunSpec.builder("injectPlugin")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("plugin", ITPlugin::class.java)
                .addCode("this.plugin = plugin as ${element.asType()}")
                .build()

            val asyncStringBuilder = StringBuilder().append(
                """
                when(funName) {
                    
            """.trimIndent()
            )

            val syncStringBuilder = StringBuilder().append(
                """
                return when(funName) {
                
            """.trimIndent()
            )


            element.enclosedElements?.forEach functionForEach@{ functionElement ->
                val functionAnnotation = functionElement.getAnnotation(TFunc::class.java) ?: return@functionForEach
                val (funName, realFun, isAsync) = convertFunctionWithParameter(functionElement, functionAnnotation)
                val timeout = functionAnnotation.timeout
                if (isAsync) {
                    asyncStringBuilder.append("\"$funName\" -> plugin.$realFun\r")

                    syncStringBuilder.append(
                        """
                        "$funName" -> {
                            var result: Map<String, Any?>?  = null
                            val latch = java.util.concurrent.CountDownLatch(1)
                            plugin.${realFun.substringBeforeLast(",successCallback")}, {
                                result = it
                                latch.countDown()
                            }, {
                                latch.countDown()
                                throw it ?: Exception("unknown exception")
                            })
                            ${if (timeout <= 0) "latch.await()" else "latch.await(${timeout}L, java.util.concurrent.TimeUnit.MILLISECONDS)"}
                            result
                        }
                        
                    """.trimIndent()
                    )

                } else {
                    asyncStringBuilder.append(
                        """
                        "$funName" -> {
                            try {
                                successCallback?.invoke(plugin.$realFun)
                            } catch(e: Exception) {
                                failureCallback?.invoke(e)
                            }
                        }
                        
                    """.trimIndent()
                    )

                    syncStringBuilder.append("\"$funName\" -> plugin.$realFun\r")
                }
            }
            asyncStringBuilder.append(
                """
                else -> failureCallback?.invoke(IllegalArgumentException("unknown funName"))
                    
                }
            """.trimIndent()
            )
            syncStringBuilder.append(
                """
                else -> throw IllegalArgumentException("unknown funName")
                }
            """.trimIndent()
            )

            val syncInvokeFunc = FunSpec.builder("syncInvoke")
                .addAnnotation(AnnotationSpec.builder(Throws::class.asTypeName()).addMember("Exception::class").build())
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    "lifecycleOwner",
                    Class.forName("androidx.lifecycle.LifecycleOwner").asTypeName().copy(true)
                )
                .addParameter("funName", String::class)
                .addParameter(
                    "params",
                    Map::class.asTypeName()
                        .parameterizedBy(String::class.asTypeName(), Any::class.asTypeName().copy(true))
                        .copy(true)
                )
                .returns(
                    Map::class.asTypeName()
                        .parameterizedBy(String::class.asTypeName(), Any::class.asTypeName().copy(true))
                        .copy(true)
                )
                .addCode(syncStringBuilder.toString())
                .build()

            val asyncInvokeFunc = FunSpec.builder("asyncInvoke")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    "lifecycleOwner",
                    Class.forName("androidx.lifecycle.LifecycleOwner").asTypeName().copy(true)
                )
                .addParameter("funName", String::class)
                .addParameter(
                    "params",
                    Map::class.asTypeName()
                        .parameterizedBy(String::class.asTypeName(), Any::class.asTypeName().copy(true))
                        .copy(true)
                )
                .addParameter(
                    ParameterSpec.builder(
                        "successCallback",
                        Function1::class.asTypeName().parameterizedBy(
                            Map::class.asTypeName()
                                .parameterizedBy(String::class.asTypeName(), Any::class.asTypeName().copy(true))
                                .copy(true),
                            Unit::class.asTypeName()
                        ).copy(true)
                    ).build()
                )
                .addParameter(
                    ParameterSpec.builder(
                        "failureCallback",
                        Function1::class.asTypeName().parameterizedBy(
                            Exception::class.asTypeName().copy(true),
                            Unit::class.asTypeName()
                        ).copy(true)
                    ).build()
                )
                .addCode(asyncStringBuilder.toString())
                .build()

            val typeSpec = TypeSpec.classBuilder(wrapperClassName)
                .addKdoc("AUTO GENERATED, DO NOT MODIFY")
                .addAnnotation(Class.forName("androidx.annotation.Keep"))
                .addSuperinterface(
                    ITPluginWrapper::class.asTypeName().parameterizedBy(
                        Class.forName("androidx.lifecycle.LifecycleOwner").asTypeName()
                    )
                )
                .addProperty(pluginPropertySpec)
                .addFunction(providePluginNameFunc)
                .addFunction(injectPluginFunc)
                .addFunction(syncInvokeFunc)
                .addFunction(asyncInvokeFunc)
                .build()

            val fileSpec = FileSpec.builder(packageName, wrapperClassName)
                .addType(typeSpec)
                .build()

            if (null != filer) {
                fileSpec.writeTo(filer!!)
            }
        }
        return false
    }

    private fun convertFunctionWithParameter(
        functionElement: Element,
        functionAnnotation: TFunc
    ): Triple<String, String, Boolean> {
        val pureFunName = functionElement.simpleName
        val realFunName = functionAnnotation.funName.ifEmpty { pureFunName.toString() }

        // return
        val isAsync = functionElement.asType().toString().substringAfter(")").contains("void")

        val functionAppender = StringBuilder().append("$pureFunName(")
        // lifecycle: LifecycleOwner?
        if (functionElement.asType().toString().contains("androidx.lifecycle.LifecycleOwner")) {
            functionAppender.append("lifecycleOwner,")
        }
        // params
        if (functionElement.asType().toString().contains("Map")) {
            functionAppender.append("params,")
        }
        // successCallback
        if (isAsync && functionElement.asType().toString()
                .contains("kotlin.jvm.functions.Function1<? super java.util.Map<java.lang.String,? extends java.lang.Object>,kotlin.Unit>")
        ) {
            functionAppender.append("successCallback,")
        }
        // failureCallback
        if (isAsync && functionElement.asType().toString()
                .contains("kotlin.jvm.functions.Function1<? super java.lang.Exception,kotlin.Unit>")
        ) {
            functionAppender.append("failureCallback,")
        }

        val finalFun = if (functionAppender.endsWith(","))
            "${functionAppender.substring(0, functionAppender.length - 1)})"
        else "$functionAppender)"
        return Triple(realFunName, finalFun, isAsync)
    }

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_8

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        TPlugin::class.java.name
    )
}