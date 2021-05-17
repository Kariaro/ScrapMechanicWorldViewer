package com.hardcoded.lwjgl.shader;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.hardcoded.lwjgl.LwjglOptions;
import com.hardcoded.lwjgl.shadow.Light;

/**
 * This abstract class adds light and transformation access to shaders.
 * 
 * @author HardCoded
 * @since v0.2
 */
public abstract class ShaderObjectImpl extends Shader {
	protected static final Vector3f DEFAULT_EMPTY = new Vector3f();
	protected static final int MAX_LIGHTS = LwjglOptions.MAX_LIGHTS;
	
	protected int[] load_lightPositionEyeSpace;
	protected int[] load_lightColor;
	protected int load_transformationMatrix;
	protected int load_projectionView;
	protected int load_modelMatrix;
	protected int load_viewMatrix;
	
	protected ShaderObjectImpl(String vertex, String fragment) {
		super(vertex, fragment);
	}
	
	@Override
	protected void loadUniforms() {
		load_projectionView = getUniformLocation("projectionView");
		load_modelMatrix = getUniformLocation("modelMatrix");
		load_viewMatrix = getUniformLocation("viewMatrix");
		
		load_lightPositionEyeSpace = new int[MAX_LIGHTS];
		//load_lightColor = new int[MAX_LIGHTS];
		for(int i = 0; i < MAX_LIGHTS; i++) {
			load_lightPositionEyeSpace[i] = getUniformLocation("load_lightPositionEyeSpace[" + i + "]");
			//load_lightColor[i] = getUniformLocation("load_lightColor[" + i + "]");
		}
	}
	
	public void setProjectionView(Matrix4f projectionView) {
		setMatrix4f(load_projectionView, projectionView);
	}
	
	public void setModelMatrix(Matrix4f modelMatrix) {
		setMatrix4f(load_modelMatrix, modelMatrix);
	}
	
	public void setViewMatrix(Matrix4f viewMatrix) {
		setMatrix4f(load_viewMatrix, viewMatrix);
	}
	
	public void loadLights(List<Light> lights, Matrix4f viewMatrix) {
		final int lightCount = lights.size();
		for(int i = 0; i < MAX_LIGHTS; i++) {
			if(i < lightCount) {
				Light light = lights.get(i);
				setVector3f(load_lightPositionEyeSpace[i], calculateLightEyePosition(viewMatrix, light.getPosition()));
				//setVector3f(load_lightColor[i], light.getColor());
			} else {
				setVector3f(load_lightPositionEyeSpace[i], DEFAULT_EMPTY);
				//setVector3f(load_lightColor[i], DEFAULT_EMPTY);
			}
		}
	}
	
	private Vector3f calculateLightEyePosition(Matrix4f viewMatrix, Vector3f pos) {
		Vector4f result = viewMatrix.transform(pos.x, pos.y, pos.z, 0.0f, new Vector4f());
		return new  Vector3f(result.x, result.y, result.z);
	}
}
