package com.hardcoded.lwjgl.util;

import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * A material container class.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class LoadedMaterial {
	public static final Vector4f DEFAULT_COLOR = new Vector4f(1, 1, 1, 1);
	public final Vector4fc ambient;
	public final Vector4fc diffuse;
	public final Vector4fc specular;
	public final float intensity;
	public final String name;
	
	public LoadedMaterial() {
		this(null, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, 0.0f);
	}
	
	public LoadedMaterial(String name, Vector4f ambient, Vector4f diffuse, Vector4f specular, float intensity) {
		this.name = name;
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.intensity = intensity;
	}
}
