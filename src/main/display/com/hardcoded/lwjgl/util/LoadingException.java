package com.hardcoded.lwjgl.util;

/**
 * This exception is used when something goes wrong during object loading.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class LoadingException extends RuntimeException {
	private static final long serialVersionUID = -1638437566171228548L;

	public LoadingException() {
		
	}
	
	public LoadingException(String message) {
		super(message);
	}
}
