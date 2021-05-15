package com.hardcoded.lwjgl.shader;

import org.joml.Matrix4f;

/**
 * A container class for the block shader.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class BlockShader extends Shader {
	public BlockShader() {
		super(
			"/shaders/block/block_vertex.vs",
			"/shaders/block/block_fragment.fs"
		);
	}
	
	@Override
	protected void loadBinds() {
		bindAttrib(0, "in_Position");
	}
	
	@Override
	protected void loadUniforms() {
		createUniform("projectionView");
		createUniform("transformationMatrix");
		createUniform("localTransform");
		createUniform("tiling");
		createUniform("color");
		createUniform("scale");
		
		createUniform("dif_tex");
		createUniform("asg_tex");
		createUniform("nor_tex");
		
		setUniform("dif_tex", 0);
		setUniform("asg_tex", 1);
		setUniform("nor_tex", 2);
	}
	
	public void setTransformationMatrix(Matrix4f transformationMatrix) {
		setUniform("transformationMatrix", transformationMatrix);
	}
}
