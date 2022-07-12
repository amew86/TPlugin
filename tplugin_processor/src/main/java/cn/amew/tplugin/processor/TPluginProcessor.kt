package cn.amew.tplugin.processor

import cn.amew.tplugin.annotation.TPlugin
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

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

            val providePluginNameFunc = FunSpec.builder("providePluginName")
//                .addModifiers(KModifier.OVERRIDE)
                .addCode("return \"$pluginName\"")
                .build()

            val typeSpec = TypeSpec.classBuilder(wrapperClassName)
                .addKdoc("AUTO GENERATED, DO NOT MODIFY")
                .addAnnotation(Class.forName("androidx.annotation.Keep"))
                .addFunction(providePluginNameFunc)
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

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_8

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        TPlugin::class.java.name
    )
}