package com.hardcoded.lwjgl.shader;

/**
 * A container class for the asset shader.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class AssetShader extends Shader {
	public AssetShader() throws Exception {
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
		createUniform("transformationMatrix");
		createUniform("color");
		
		setUniform("dif_tex", 0);
		setUniform("asg_tex", 1);
		setUniform("nor_tex", 2);
		setUniform("ao_tex", 3);
	}
}
