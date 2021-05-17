package com.hardcoded.lwjgl.shader;

import org.lwjgl.opengl.GL20;

/**
 * A container class for the part shader.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class PartShader extends ShaderObjectImpl {
	protected int load_color;
	
	public PartShader() {
		super(
			"/shaders/part/part_vertex.vs",
			"/shaders/part/part_fragment.fs"
		);
	}
	
	@Override
	protected void loadBinds() {
		bindAttrib(0, "in_Position");
		bindAttrib(1, "in_Uv");
		bindAttrib(2, "in_Normal");
		bindAttrib(3, "in_Tangent");
	}
	
	@Override
	protected void loadUniforms() {
		super.loadUniforms();
		
		load_color = createUniform("color");
		
		setUniform("dif_tex", 0);
		setUniform("asg_tex", 1);
		setUniform("nor_tex", 2);
		setUniform("ao_tex", 3);
	}
	
	public void setColor(float r, float g, float b, float a) {
		GL20.glUniform4f(load_color, r, g, b, a);
	}
}
