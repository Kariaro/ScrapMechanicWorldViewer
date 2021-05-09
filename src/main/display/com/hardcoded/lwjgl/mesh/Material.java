package com.hardcoded.lwjgl.mesh;

import org.joml.Vector4f;

public class Material {
	public static final Vector4f DEFAULT_COLOUR = new Vector4f(1, 1, 1, 1);
	public final Vector4f ambient;
	public final Vector4f diffuse;
	public final Vector4f specular;
	public final float intensity;
	public final String name;
	
	public Material() {
		this(null,
			 DEFAULT_COLOUR,
			 DEFAULT_COLOUR,
			 DEFAULT_COLOUR,
			 0.0f);
	}
	
	public Material(String name, Vector4f ambient, Vector4f diffuse, Vector4f specular, float intensity) {
		this.name = name;
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
		this.intensity = intensity;
	}
}
