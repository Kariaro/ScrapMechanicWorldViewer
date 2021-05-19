package com.hardcoded.lwjgl.shader;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

/**
 * A container class for the block shader.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class BlockShader extends ShaderObjectImpl {
	protected int load_localTransform;
	protected int load_color;
	protected int load_tiling;
	protected int load_scale;
	
	public BlockShader() {
		super(
			"/shaders/block/block_vertex.vs",
			"/shaders/block/block_fragment.fs"
		);
	}
	
	@Override
	protected void loadBinds() {
		bindAttrib(0, "in_Position");
		bindAttrib(1, "in_Normal");
		bindAttrib(2, "in_Tangent");
	}
	
	@Override
	protected void loadUniforms() {
		super.loadUniforms();
		
		load_localTransform = createUniform("localTransform");
		load_color = createUniform("color");
		load_tiling = createUniform("tiling");
		load_scale = createUniform("scale");
		
		setUniform("dif_tex", 0);
		setUniform("asg_tex", 1);
		setUniform("nor_tex", 2);
		
		setUniform("shadowMap", 9);
	}
	
	public void setLocalTransform(float x, float y, float z) {
		GL20.glUniform3f(load_localTransform, x, y, z);
	}
	
	public void setLocalTransform(Vector3f localTransform) {
		GL20.glUniform3f(load_localTransform, localTransform.x, localTransform.y, localTransform.z);
	}
	
	public void setTiling(int value) {
		GL20.glUniform1i(load_tiling, value);
	}
	
	public void setScale(float x, float y, float z) {
		GL20.glUniform3f(load_scale, x, y, z);
	}
	
	public void setScale(Vector3f scale) {
		GL20.glUniform3f(load_scale, scale.x, scale.y, scale.z);
	}
	
	public void setColor(float r, float g, float b, float a) {
		GL20.glUniform4f(load_color, r, g, b, a);
	}
	
	public void setColor(Vector4f color) {
		GL20.glUniform4f(load_color, color.x, color.y, color.z, color.w);
	}
}
