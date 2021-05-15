package com.hardcoded.lwjgl.shader;

import org.joml.Matrix4f;

/**
 * A container class for the asset shader.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class AssetShader extends Shader {
	public AssetShader() {
		super(
			"/shaders/asset/asset_vertex.vs",
			"/shaders/asset/asset_fragment.fs"
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
		createUniform("modelMatrix");
		createUniform("toShadowMapSpace");
		createUniform("color");
		createUniform("shadowMap");
		
		setUniform("dif_tex", 0);
		setUniform("asg_tex", 1);
		setUniform("nor_tex", 2);
		setUniform("ao_tex", 3);
		setUniform("shadowMap", 9);
	}
	
	public void setModelMatrix(Matrix4f modelMatrix) {
		setUniform("modelMatrix", modelMatrix);
	}
	
	public void setShadowMapSpace(Matrix4f matrix) {
		setUniform("toShadowMapSpace", matrix);
	}
}
