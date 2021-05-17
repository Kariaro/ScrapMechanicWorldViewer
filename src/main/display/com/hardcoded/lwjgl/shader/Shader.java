package com.hardcoded.lwjgl.shader;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import com.hardcoded.util.FileUtils;

/**
 * A simple shader implementation.
 * 
 * @author HardCoded
 * @since v0.1
 */
public abstract class Shader {
	private Map<String, Integer> uniforms = new HashMap<>();
	
	protected final int programId;
	protected int vertexShaderId;
	protected int fragmentShaderId;
	
	protected Shader(String vertexPath, String fragmentPath) {
		programId = GL20.glCreateProgram();
		if(programId == 0) {
			throw new ShaderException("Failed to create shader: GL20.glCreateProgram() returned 0");
		}
		
		createShader(FileUtils.readStream(Shader.class.getResourceAsStream(vertexPath)), GL20.GL_VERTEX_SHADER);
		createShader(FileUtils.readStream(Shader.class.getResourceAsStream(fragmentPath)), GL20.GL_FRAGMENT_SHADER);
		
		
		loadBinds();
		link();
		
		bind();
		loadUniforms();
		unbind();
	}
	
	protected abstract void loadBinds();
	protected abstract void loadUniforms();
	
	private int createShader(String shaderCode, int shaderType) throws ShaderException {
		int shaderId = glCreateShader(shaderType);
		if(shaderId == 0) {
			throw new ShaderException("Error creating shader. Type: " + shaderType);
		}
		
		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);
		
		if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new ShaderException("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}
		
		glAttachShader(programId, shaderId);
		return shaderId;
	}
	
	protected final void bindAttrib(int index, String name) {
		GL20.glBindAttribLocation(programId, index, name);
	}
	
	protected final int getUniformLocation(String uniformName) {
		return GL20.glGetUniformLocation(programId, uniformName);
	}
	
	protected void setMatrix4f(int uniformId, Matrix4f value) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(16);
			value.get(fb);
			GL20.glUniformMatrix4fv(uniformId, false, fb);
		}
	}
	
	protected final void setVector3f(int uniformId, Vector3f v) {
		GL20.glUniform3f(uniformId, v.x, v.y, v.z);
	}
	
	// OLD FUNCTIONS
	protected int createUniform(String uniformName) {
		int uniformLocation = glGetUniformLocation(programId, uniformName);
		uniforms.put(uniformName, uniformLocation);
		return uniformLocation;
	}
	
	protected void setUniform(String uniformName, Matrix4f value) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(16);
			value.get(fb);
			glUniformMatrix4fv(_uniform(uniformName), false, fb);
		}
	}
	
	protected void setUniform(String uniformName, Matrix3f value) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(12);
			value.get(fb);
			glUniformMatrix3fv(_uniform(uniformName), false, fb);
		}
	}
	
	protected void setUniform(String uniformName, Vector4f v) {
		glUniform4f(_uniform(uniformName), v.x, v.y, v.z, v.w);
	}
	
	protected void setUniform(String uniformName, Vector3f v) {
		glUniform3f(_uniform(uniformName), v.x, v.y, v.z);
	}
	
	protected void setUniform(String uniformName, Vector2f v) {
		glUniform2f(_uniform(uniformName), v.x, v.y);
	}
	
	protected void setUniform(String uniformName, int value) {
		glUniform1i(_uniform(uniformName), value);
	}
	
	protected void setUniform4i(String uniformName, int a, int b, int c, int d) {
		glUniform4i(_uniform(uniformName), a, b, c, d);
	}
	
	protected void setUniform(String uniformName, float x, float y, float z, float w) {
		glUniform4f(_uniform(uniformName), x, y, z, w);
	}
	
	protected void setUniform(String uniformName, float x, float y, float z) {
		glUniform3f(_uniform(uniformName), x, y, z);
	}
	
	protected void setUniform(String uniformName, float x, float y) {
		glUniform2f(_uniform(uniformName), x, y);
	}
	
	protected void setUniform(String uniformName, float x) {
		glUniform1f(_uniform(uniformName), x);
	}
	
	@Deprecated
	public void setUniform(String uniformName, boolean value) {
		glUniform1i(_uniform(uniformName), value ? 1 : 0);
	}
	
	private int _uniform(String uniformName) {
		if(!uniforms.containsKey(uniformName)) {
			createUniform(uniformName);
			return uniforms.get(uniformName);
		}
		
		return uniforms.get(uniformName);
	}
	
	private void link() throws ShaderException {
		glLinkProgram(programId);
		if(glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
		}
		
		if(vertexShaderId != 0) {
			glDetachShader(programId, vertexShaderId);
		}
		
		if(fragmentShaderId != 0) {
			glDetachShader(programId, fragmentShaderId);
		}
		
		glValidateProgram(programId);
		if(glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
			System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
		}
	}
	
	public final void bind() {
		glUseProgram(programId);
	}
	
	public final void unbind() {
		glUseProgram(0);
	}
	
	public final void cleanup() {
		unbind();
		if(programId != 0) {
			glDeleteProgram(programId);
		}
	}
}
