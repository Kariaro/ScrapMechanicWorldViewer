package com.hardcoded.lwjgl.shadow;

import com.hardcoded.lwjgl.shader.Shader;

/**
 * A shadow shader.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class ShadowShader extends Shader {
	public ShadowShader() throws Exception {
		super(
			"/shaders/shadow/shadow_vertex.vs",
			"/shaders/shadow/shadow_fragment.fs"
		);
	}
	
	@Override
	protected void loadBinds() {
		bindAttrib(0, "in_Position");
	}
	
	@Override
	protected void loadUniforms() {
		createUniform("mvpMatrix");
	}
}