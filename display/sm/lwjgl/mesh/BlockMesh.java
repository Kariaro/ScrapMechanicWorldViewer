package sm.lwjgl.mesh;

import static org.lwjgl.opengl.GL11.*;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

public class BlockMesh {
	private int vaoId;
	private int vertexCount;
	
	private int vboVertex;
	
	public BlockMesh() {
		float[] verts = {
			1, 1, 0,   0, 0, 0,   0, 1, 0,
			0, 0, 0,   1, 1, 0,   1, 0, 0,
			1, 1, 1,   0, 1, 1,   0, 0, 1,
			0, 0, 1,   1, 0, 1,   1, 1, 1,
			
			1, 1, 0,   1, 1, 1,   1, 0, 0,
			1, 0, 0,   1, 1, 1,   1, 0, 1,
			0, 1, 0,   0, 0, 0,   0, 1, 1,
			0, 0, 0,   0, 0, 1,   0, 1, 1,
			
			0, 0, 0,   1, 0, 1,   0, 0, 1,
			0, 0, 0,   1, 0, 0,   1, 0, 1,
			0, 1, 0,   0, 1, 1,   1, 1, 1,
			0, 1, 0,   1, 1, 1,   1, 1, 0,
		};
		
		buildObject(verts);
	}
	
	private void buildObject(float[] verts) {
		FloatBuffer verticesBuffer = null;
		
		vertexCount = verts.length / 3;
		try {
			verticesBuffer = MemoryUtil.memAllocFloat(verts.length);
			verticesBuffer.put(verts).flip();
			
			
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			vboVertex = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0L);
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
		} finally {
			if(verticesBuffer != null) {
				MemoryUtil.memFree(verticesBuffer);
			}
		}
	}
	
	public void render() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
	
	public void cleanup() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(vboVertex);
	}
}
