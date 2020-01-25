package com.sherazkhilji.videffects.interfaces;

import android.opengl.GLSurfaceView;

/**
 * An abstract class that every effect must extend so that there is a common
 * getShader method that every effect class is force to override
 * 
 * @author sheraz.khilji
 *
 */
public interface ShaderInterface {
	/**
	 * Returns Shader code
	 * 
	 * @param mGlSurfaceView
	 *            send this for every shader but this will only be used when the
	 *            shader needs it.
	 * @return complete shader code in C
	 */
	public String getShader(GLSurfaceView mGlSurfaceView);

	public boolean isAdjustable();

}
