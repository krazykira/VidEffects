package com.videffects.sample.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sherazkhilji.videffects.*
import com.videffects.sample.interfaces.OnSelectShaderListener

class EffectsDialog : DialogFragment() {

    companion object {

        private const val SUFFIX = "Effect"

        private val SHADERS = arrayOf(
                GrainEffect(0.1F),
                AutoFixEffect(1F),
                BlackAndWhiteEffect(),
                ContrastEffect(0.5F),
                GreyScaleEffect(),
                SaturationEffect(0F),
                VignetteEffect(0.5F),
                GammaEffect(0.5F),
                SepiaEffect()
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose effect")
                .setItems(Array(SHADERS.size) { i ->
                    SHADERS[i]::class.java.simpleName.removeSuffix()
                }) { _, which ->
                    (activity as? OnSelectShaderListener)?.onSelectShader(SHADERS[which])
                }
        return builder.create()
    }

    private fun String.removeSuffix() = removeSuffix(SUFFIX)
}