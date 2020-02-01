package com.sherazkhilji.videffects.interfaces;

import android.os.Parcelable;

/**
 * Implementation of this interface should provide access to vertex and fragment shader.
 * @author ivan.murashov
 *
 */
public interface Filter extends Parcelable {

	public String getVertexShader();

	public String getFragmentShader();
}
