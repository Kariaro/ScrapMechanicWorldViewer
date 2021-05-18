package com.hardcoded.lwjgl.mesh;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

/**
 * @author HardCoded
 * @since v0.1
 */
public class BlockMesh {
	private int vaoId;
	private int vertexCount;
	private int vboVertex;
	private int vboNormal;
	private int vboTangent;
	
	public BlockMesh() {
		float[] verts = {
			0, 1, 1,   1, 0, 1,   1, 1, 1,
			1, 0, 1,   0, 0, 0,   1, 0, 0,
			0, 0, 1,   0, 1, 0,   0, 0, 0,
			1, 1, 0,   0, 0, 0,   0, 1, 0,
			
			1, 1, 1,   1, 0, 0,   1, 1, 0,
			0, 1, 1,   1, 1, 0,   0, 1, 0,
			0, 1, 1,   0, 0, 1,   1, 0, 1,
			1, 0, 1,   0, 0, 1,   0, 0, 0,
			
			0, 0, 1,   0, 1, 1,   0, 1, 0,
			1, 1, 0,   1, 0, 0,   0, 0, 0,
			1, 1, 1,   1, 0, 1,   1, 0, 0,
			0, 1, 1,   1, 1, 1,   1, 1, 0
		};
		
		float[] normal = {
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			0,-1, 0,   0,-1, 0,   0,-1, 0,
		   -1, 0, 0,  -1, 0, 0,  -1, 0, 0,
			0, 0,-1,   0, 0,-1,   0, 0,-1,
			
			1, 0, 0,   1, 0, 0,   1, 0, 0,
			0, 1, 0,   0, 1, 0,   0, 1, 0,
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			0,-1, 0,   0,-1, 0,   0,-1, 0,
			
		   -1, 0, 0,  -1, 0, 0,  -1, 0, 0,
			0, 0,-1,   0, 0,-1,   0, 0,-1,
			1, 0, 0,   1, 0, 0,   1, 0, 0,
			0, 1, 0,   0, 1, 0,   0, 1, 0
		};
		
		float[] tangent = {
		   -1, 0, 0,  -1, 0, 0,  -1, 0, 0,
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			1, 0, 0,   1, 0, 0,   1, 0, 0,
			
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			0, 0, 1,   0, 0, 1,   0, 0, 1,
		   -1, 0, 0,  -1, 0, 0,  -1, 0, 0,
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			1, 0, 0,   1, 0, 0,   1, 0, 0,
			0, 0, 1,   0, 0, 1,   0, 0, 1,
			0, 0, 1,   0, 0, 1,   0, 0, 1
		};

		buildObject(verts, normal, tangent);
	}
	
	private void buildObject(float[] verts, float[] normal, float[] tangent) {
		FloatBuffer verticesBuffer = null;
		FloatBuffer normalsBuffer = null;
		FloatBuffer tangentsBuffer = null;
		
		vertexCount = verts.length / 3;
		try {
			verticesBuffer = MemoryUtil.memAllocFloat(verts.length);
			verticesBuffer.put(verts).flip();
			normalsBuffer = MemoryUtil.memAllocFloat(normal.length);
			normalsBuffer.put(normal).flip();
			tangentsBuffer = MemoryUtil.memAllocFloat(tangent.length);
			tangentsBuffer.put(tangent).flip();
			
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			vboVertex = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
			
			vboNormal = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNormal);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0L);
			
			vboTangent = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTangent);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tangentsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0L);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
		} finally {
			if(verticesBuffer != null) {
				MemoryUtil.memFree(verticesBuffer);
			}
			
			if(normalsBuffer != null) {
				MemoryUtil.memFree(normalsBuffer);
			}
			
			if(tangentsBuffer != null) {
				MemoryUtil.memFree(tangentsBuffer);
			}
		}
	}
	
	public void render() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}
	
	public void cleanup() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(vboVertex);
		GL15.glDeleteBuffers(vboNormal);
		GL15.glDeleteBuffers(vboTangent);
	}
}
