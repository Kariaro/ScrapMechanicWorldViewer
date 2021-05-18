package com.hardcoded.lwjgl.mesh;

import static org.lwjgl.opengl.GL15.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import com.hardcoded.lwjgl.async.LwjglAsyncThread;
import com.hardcoded.lwjgl.util.LoadedMaterial;
import com.hardcoded.lwjgl.util.StaticMeshLoaderAsync.AsyncMesh;

/**
 * A simple mesh
 * 
 * @author HardCoded
 * @since v0.1
 */
public class Mesh {
	public static final int MAX_WEIGHTS = 4;
	protected final int vaoId;
	protected final List<Integer> vboIdList;
	private final int vertexCount;
	private LoadedMaterial material;
	private String name;
	
	public Mesh(AsyncMesh mesh) {
		this(
			mesh.vertexs,
			mesh.uvs,
			mesh.normals,
			mesh.tangents,
			mesh.indices
		);
		setMaterial(mesh.material);
	}
	
	protected Mesh(float[] positions, float[] textCoords, float[] normals, float[] tangents, int[] indices) { // int[] jointIndices, float[] weights) {
		if(LwjglAsyncThread.isCurrentThread()) {
			throw new RuntimeException("Meshes can only be loaded on the main thread");
		}
		
		FloatBuffer posBuffer = null;
		FloatBuffer textCoordsBuffer = null;
		FloatBuffer vecNormalsBuffer = null;
		FloatBuffer vecTangentsBuffer = null;
		FloatBuffer weightsBuffer = null;
		IntBuffer jointIndicesBuffer = null;
		IntBuffer indicesBuffer = null;
		
		try {
			vertexCount = indices.length;
			vboIdList = new ArrayList<>();
			
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			// Position VBO
			int vboId = GL15.glGenBuffers();
			vboIdList.add(vboId);
			posBuffer = MemoryUtil.memAllocFloat(positions.length);
			posBuffer.put(positions).flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			
			// Texture coordinates VBO
			vboId = GL15.glGenBuffers();
			vboIdList.add(vboId);
			textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
			textCoordsBuffer.put(textCoords).flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textCoordsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
			
			// Vertex normals VBO
			vboId = GL15.glGenBuffers();
			vboIdList.add(vboId);
			vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
			vecNormalsBuffer.put(normals).flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vecNormalsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
			
			// Vertex tangents VBO
			vboId = GL15.glGenBuffers();
			vboIdList.add(vboId);
			vecTangentsBuffer = MemoryUtil.memAllocFloat(tangents.length);
			vecTangentsBuffer.put(tangents).flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vecNormalsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 0, 0);
			
//			// Weights VBO
//			vboId = GL15.glGenBuffers();
//			vboIdList.add(vboId);
//			weightsBuffer = MemoryUtil.memAllocFloat(weights.length);
//			weightsBuffer.put(weights).flip();
//			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
//			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, weightsBuffer, GL15.GL_STATIC_DRAW);
//			GL20.glVertexAttribPointer(4, 4, GL11.GL_FLOAT, false, 0, 0);
//			
//			// Joint indices VBO
//			vboId = GL15.glGenBuffers();
//			vboIdList.add(vboId);
//			jointIndicesBuffer = MemoryUtil.memAllocInt(jointIndices.length);
//			jointIndicesBuffer.put(jointIndices).flip();
//			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
//			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, jointIndicesBuffer, GL15.GL_STATIC_DRAW);
//			GL20.glVertexAttribPointer(5, 4, GL11.GL_FLOAT, false, 0, 0);
			
			// Indices VBO
			vboId = GL15.glGenBuffers();
			vboIdList.add(vboId);
			indicesBuffer = MemoryUtil.memAllocInt(indices.length);
			indicesBuffer.put(indices).flip();
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
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
			
			if(vecTangentsBuffer != null) {
				MemoryUtil.memFree(vecTangentsBuffer);
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
	
	public int getVaoId() {
		return vaoId;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public void render() {
		// Draw the mesh
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		
		GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
		
		// Restore state
//		GL20.glDisableVertexAttribArray(3);
//		GL20.glDisableVertexAttribArray(2);
//		GL20.glDisableVertexAttribArray(1);
//		GL20.glDisableVertexAttribArray(0);
//		GL30.glBindVertexArray(0);
	}
	
	public void cleanUp() {
		GL20.glDisableVertexAttribArray(0);
		
		// Delete the VBOs
		GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);
		for(int vboId : vboIdList) {
			GL15.glDeleteBuffers(vboId);
		}
		
		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
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