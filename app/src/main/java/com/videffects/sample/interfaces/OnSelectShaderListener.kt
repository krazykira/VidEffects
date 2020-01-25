package com.videffects.sample.interfaces

import com.sherazkhilji.videffects.interfaces.ShaderInterface

interface OnSelectShaderListener {

    fun onSelectShader(shader: ShaderInterface, allowAdjustment: Boolean)
}