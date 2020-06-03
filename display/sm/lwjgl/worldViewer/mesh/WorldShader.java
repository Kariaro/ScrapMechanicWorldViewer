package sm.lwjgl.worldViewer.mesh;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import sm.util.FileUtils;

public class WorldShader {
	private final int programId;
	private int vertexShaderId;
	private int fragmentShaderId;
	
	private final Map<String, Integer> uniforms;
	
	public WorldShader() throws Exception {
		programId = GL20.glCreateProgram();
		if(programId == 0) {
			throw new Exception("Could not create Shader");
		}
		
		uniforms = new HashMap<>();
	}
	
	public WorldShader(String vertexPath, String fragmentPath) throws Exception {
		programId = GL20.glCreateProgram();
		if(programId == 0) {
			throw new Exception("Could not create Shader");
		}
		
		createVertexShader(vertexPath);
		createFragmentShader(fragmentPath);
		link();
		
		uniforms = new HashMap<>();
	}
	
	public void createVertexShader(String shaderPath) throws Exception {
		vertexShaderId = createShader(shaderPath, GL20.GL_VERTEX_SHADER);
	}
	
	public void createFragmentShader(String shaderPath) throws Exception {
		fragmentShaderId = createShader(shaderPath, GL20.GL_FRAGMENT_SHADER);
	}
	
	protected int createShader(String shaderPath, int shaderType) throws Exception {
		int shaderId = glCreateShader(shaderType);
		if(shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}
		
		String shaderCode = FileUtils.readFile(shaderPath);
		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);
		
		if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}
		
		glAttachShader(programId, shaderId);
		return shaderId;
	}
	
	public int createShaderCode(String shaderCode, int shaderType) throws Exception {
		int shaderId = glCreateShader(shaderType);
		if(shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}
		
		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);
		
		if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}
		
		glAttachShader(programId, shaderId);
		return shaderId;
	}
	
	public void link() throws Exception {
		glLinkProgram(programId);
		if(glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
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
	
	public void bindAttrib(int index, String name) {
		GL20.glBindAttribLocation(programId, index, name);
	}
	
	public void createUniform(String uniformName) {
		int uniformLocation = glGetUniformLocation(programId, uniformName);
		uniforms.put(uniformName, uniformLocation);
	}
	
	public void setUniform(String uniformName, Matrix4f value) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(16);
			value.get(fb);
			glUniformMatrix4fv(_uniform(uniformName), false, fb);
		}
	}
	
	public void setUniform(String uniformName, Vector4f v) {
		glUniform4f(_uniform(uniformName), v.x, v.y, v.z, v.w);
	}
	
	public void setUniform(String uniformName, Vector3f v) {
		glUniform3f(_uniform(uniformName), v.x, v.y, v.z);
	}
	
	public void setUniform(String uniformName, Vector2f v) {
		glUniform2f(_uniform(uniformName), v.x, v.y);
	}
	
	public void setUniform(String uniformName, int value) {
		glUniform1i(_uniform(uniformName), value);
	}
	
	public void setUniform(String uniformName, float x, float y, float z, float w) {
		glUniform4f(_uniform(uniformName), x, y, z, w);
	}
	
	public void setUniform(String uniformName, float x, float y) {
		glUniform2f(_uniform(uniformName), x, y);
	}
	
	public void setUniform(String uniformName, float x) {
		glUniform1f(_uniform(uniformName), x);
	}
	
	public void setUniform(String uniformName, boolean value) {
		glUniform1i(_uniform(uniformName), value ? 1 : 0);
	}
	
	private int _uniform(String uniformName) {
		if(!uniforms.containsKey(uniformName)) {
			uniforms.put(uniformName, -1);
			return -1;
		}
		
		return uniforms.get(uniformName);
	}
	
	public void bind() {
		glUseProgram(programId);
	}
	
	public void unbind() {
		glUseProgram(0);
	}
	
	public void cleanup() {
		unbind();
		if(programId != 0) {
			glDeleteProgram(programId);
		}
	}
}
