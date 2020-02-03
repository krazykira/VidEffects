package com.videffects.sample.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.videffects.sample.interfaces.OnSelectShaderListener
import com.videffects.sample.model.Shaders

class ShaderChooserDialog : DialogFragment() {

    companion object {

        private const val WIDTH = "width"
        private const val HEIGHT = "height"

        fun newInstance(videoViewWidth: Int, videoViewHeight: Int): ShaderChooserDialog {
            return ShaderChooserDialog().apply {
                arguments = Bundle().apply {
                    putInt(WIDTH, videoViewWidth)
                    putInt(HEIGHT, videoViewHeight)
                }
            }
        }
    }

    private var listener: OnSelectShaderListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val width = arguments?.getInt(WIDTH) ?: 0
        val height = arguments?.getInt(HEIGHT) ?: 0
        val shaders = Shaders(width, height)    // Can take some time and may block ui

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Choose effect")
                .setItems(Array(shaders.count) { i ->
                    shaders.getShaderName(i)
                }) { _, which ->
                    this.listener?.onSelectShader(shaders.getShader(which))
                }

        builder.setOnDismissListener {
            this.listener = null
        }
        return builder.create()
    }

    fun setListener(listener: OnSelectShaderListener) {
        this.listener = listener
    }
}