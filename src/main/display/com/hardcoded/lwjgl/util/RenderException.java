package com.hardcoded.lwjgl.util;

/**
 * A render exception.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class RenderException extends RuntimeException {
	private static final long serialVersionUID = 2589463690812829410L;
	
	public RenderException() {
		
	}
	
	public RenderException(String message) {
		super(message);
	}
}
