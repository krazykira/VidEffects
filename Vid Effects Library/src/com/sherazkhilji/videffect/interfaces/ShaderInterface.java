package com.sherazkhilji.videffect.interfaces;

/**
 * An interface that every effect must implement so that there is a common
 * getShader method that every effect class is force to override
 * 
 * @author sheraz.khilji
 *
 */
public interface ShaderInterface {
	/**
	 * Returns Shader code
	 * 
	 * @return complete shader code in C
	 */
	public String getShader();

}
