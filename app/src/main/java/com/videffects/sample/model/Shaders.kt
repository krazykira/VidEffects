package com.videffects.sample.model

import android.graphics.Color
import com.sherazkhilji.videffects.*
import com.sherazkhilji.videffects.filter.AutoFixFilter
import com.sherazkhilji.videffects.filter.GrainFilter
import com.sherazkhilji.videffects.filter.HueFilter

object Shaders {

    private const val SUFFIX = "Effect"

    private val shaders = arrayOf(
            // Filters
            AutoFixFilter(),
            GrainFilter(1920, 1080),
            HueFilter(),

            // Effects
            BlackAndWhiteEffect(),
            BrightnessEffect(0.5F),
            ContrastEffect(0.5F),
            CrossProcessEffect(),
            DocumentaryEffect(),
            DuotoneEffect(),
            FillLightEffect(0.5F),
            GammaEffect(1.0F),
            GreyScaleEffect(),
            InvertColorsEffect(),
            LamoishEffect(),
            PosterizeEffect(),
            SaturationEffect(0.5F),
            SepiaEffect(),
            SharpnessEffect(0.5F),
            TemperatureEffect(0.5F),
            TintEffect(Color.GREEN),
            VignetteEffect(0F)
    )

    val count = shaders.size

    fun getShader(index: Int) = shaders[index]

    fun getShaderName(index: Int) = shaders[index]::class.java.simpleName.removeSuffix(SUFFIX)
}