package com.sherazkhilji.videffects.interfaces;

import android.annotation.TargetApi;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Implementation of this interface should provide access to vertex and fragment shader.
 * @author ivan.murashov
 *
 */
public interface Filter {

	public String getVertexShader();

	public String getFragmentShader();

	public void setIntensity(float intensity);
}
