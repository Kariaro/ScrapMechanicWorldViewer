package com.hardcoded.lwjgl.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.system.MemoryUtil;

import com.hardcoded.lwjgl.util.LoadedMaterial;

/**
 * @author HardCoded
 * @since v0.1
 */
public class Mesh {
	public static final int MAX_WEIGHTS = 4;
	protected final int vaoId;
	protected final List<Integer> vboIdList;
	private final int vertexCount;
	private LoadedMaterial material;
	private float boundingRadius;
	private String name;
	
	public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices) {
		this(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
	}
	
	public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights) {
		FloatBuffer posBuffer = null;
		FloatBuffer textCoordsBuffer = null;
		FloatBuffer vecNormalsBuffer = null;
		FloatBuffer weightsBuffer = null;
		IntBuffer jointIndicesBuffer = null;
		IntBuffer indicesBuffer = null;
		try {
			//calculateBoundingRadius(positions);
			
			vertexCount = indices.length;
			vboIdList = new ArrayList<>();
			
			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);
			
			// Position VBO
			int vboId = glGenBuffers();
			vboIdList.add(vboId);
			posBuffer = MemoryUtil.memAllocFloat(positions.length);
			posBuffer.put(positions).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			
			// Texture coordinates VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
			textCoordsBuffer.put(textCoords).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			
			// Vertex normals VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
			vecNormalsBuffer.put(normals).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
			
			// Weights
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			weightsBuffer = MemoryUtil.memAllocFloat(weights.length);
			weightsBuffer.put(weights).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);
			
			// Joint indices
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			jointIndicesBuffer = MemoryUtil.memAllocInt(jointIndices.length);
			jointIndicesBuffer.put(jointIndices).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, jointIndicesBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);
			
			// Index VBO
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			indicesBuffer = MemoryUtil.memAllocInt(indices.length);
			indicesBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		} finally {
			if(posBuffer != null) {
				MemoryUtil.memFree(posBuffer);
			}
			
			if(textCoordsBuffer != null) {
				MemoryUtil.memFree(textCoordsBuffer);
			}
			
			if(vecNormalsBuffer != null) {
				MemoryUtil.memFree(vecNormalsBuffer);
			}
			
			if(weightsBuffer != null) {
				MemoryUtil.memFree(weightsBuffer);
			}
			
			if(jointIndicesBuffer != null) {
				MemoryUtil.memFree(jointIndicesBuffer);
			}
			
			if(indicesBuffer != null) {
				MemoryUtil.memFree(indicesBuffer);
			}
		}
	}
	
	/*
	private void calculateBoundingRadius(float[] positions) {
		int length = positions.length;
		boundingRadius = 0;
		for(int i = 0; i < length; i++) {
			float pos = positions[i];
			boundingRadius = Math.max(Math.abs(pos), boundingRadius);
		}
	}*/
	
	public LoadedMaterial getMaterial() {
		return material;
	}
	
	public void setMaterial(LoadedMaterial material) {
		this.name = material.name;
		this.material = material;
	}
	
	public String getName() {
		return name;
	}
	
	public final int getVaoId() {
		return vaoId;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public float getBoundingRadius() {
		return boundingRadius;
	}
	
	public void setBoundingRadius(float boundingRadius) {
		this.boundingRadius = boundingRadius;
	}
	
	public void render() {
		// Draw the mesh
		glBindVertexArray(getVaoId());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
		glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
		
		// Restore state
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindVertexArray(0);
	}
	
	public void cleanUp() {
		glDisableVertexAttribArray(0);
		
		// Delete the VBOs
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for(int vboId : vboIdList) {
			glDeleteBuffers(vboId);
		}
		
		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}
	
	public void deleteBuffers() {
		glDisableVertexAttribArray(0);
		
		// Delete the VBOs
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for(int vboId : vboIdList) {
			glDeleteBuffers(vboId);
		}
		
		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}
	
	protected static float[] createEmptyFloatArray(int length, float defaultValue) {
		float[] result = new float[length];
		Arrays.fill(result, defaultValue);
		return result;
	}
	
	protected static int[] createEmptyIntArray(int length, int defaultValue) {
		int[] result = new int[length];
		Arrays.fill(result, defaultValue);
		return result;
	}
}