package com.videffects.sample.model

import android.graphics.Color
import com.sherazkhilji.videffects.*

object Shaders {

    private const val SUFFIX = "Effect"

    private val shaders = arrayOf(
            Pair(AutoFixEffect(0.0F), true),
            Pair(BlackAndWhiteEffect(), false),
            Pair(BrightnessEffect(0.5F), false),
            Pair(ContrastEffect(0.5F), false),
            Pair(CrossProcessEffect(), false),
            Pair(DocumentaryEffect(), false),
            Pair(DuotoneEffect(), false),
            Pair(FillLightEffect(0.5F), false),
            Pair(GammaEffect(1.0F), false),
            Pair(GrainEffect(0.1F), true),
            Pair(GreyScaleEffect(), false),
            Pair(HueEffect(0.0F), true),
            Pair(InvertColorsEffect(), false),
            Pair(LamoishEffect(), false),
            Pair(PosterizeEffect(), false),
            Pair(SaturationEffect(0.5F), false),
            Pair(SepiaEffect(), false),
            Pair(SharpnessEffect(0.5F), false),
            Pair(TemperatureEffect(0.5F), false),
            Pair(TintEffect(Color.GREEN), false),
            Pair(VignetteEffect(0F), false)
    )

    val count = shaders.size

    fun getShader(index: Int) = shaders[index]

    fun getShaderName(index: Int) = shaders[index].first::class.java.simpleName.removeSuffix(SUFFIX)
}