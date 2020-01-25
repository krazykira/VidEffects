package com.videffects.sample.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.videffects.sample.interfaces.OnSelectShaderListener
import com.videffects.sample.model.Shaders

class ShaderChooserDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose effect")
                .setItems(Array(Shaders.count) { i ->
                    Shaders.getShaderName(i)
                }) { _, which ->
                    val shader = Shaders.getShader(which)
                    val listener = (activity as? OnSelectShaderListener)
                    listener?.onSelectShader(shader.first, shader.second)
                }
        return builder.create()
    }
}