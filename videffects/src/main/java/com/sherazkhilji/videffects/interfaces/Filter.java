package com.sherazkhilji.videffects.interfaces;

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
