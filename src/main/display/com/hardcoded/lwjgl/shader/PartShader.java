package com.hardcoded.lwjgl.shader;

import org.joml.Matrix4f;

/**
 * A container class for the part shader.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class PartShader extends Shader {
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
	}
	
	@Override
	protected void loadUniforms() {
		createUniform("projectionView");
		createUniform("transformationMatrix");
		createUniform("color");
		
		setUniform("dif_tex", 0);
		setUniform("asg_tex", 1);
		setUniform("nor_tex", 2);
		setUniform("ao_tex", 3);
	}
	
	public void setTransformationMatrix(Matrix4f transformationMatrix) {
		setUniform("transformationMatrix", transformationMatrix);
	}
}
