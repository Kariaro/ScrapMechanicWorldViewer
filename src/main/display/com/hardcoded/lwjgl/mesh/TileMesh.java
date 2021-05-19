package com.hardcoded.lwjgl.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import com.hardcoded.lwjgl.shader.TileShader;
import com.hardcoded.tile.Tile;
import com.hardcoded.tile.impl.TilePart;

public class TileMesh {
	private int vaoId;
	private int vertexCount;
	
	private int vboVertex;
	private int vboColors;
	private int vboTextures;
	private int vboMaterial;
	
	public TileMesh(Tile tile) {
		int width = tile.getWidth();
		int height = tile.getHeight();
		
		float[] verts = new float[width * height * 32 * 32 * 3 * 6];
		float[] colors = new float[width * height * 32 * 32 * 4 * 6];
		float[] uvs = new float[width * height * 32 * 32 * 2 * 6];
		int[] materials = new int[width * height * 32 * 32 * 16 * 6];
		
		float[] tile_heights = tile.getVertexHeight();
		long[] tile_mats = tile.getGround();
		int[] tile_colors = tile.getVertexColor();
		
		int tchw = tile.getWidth() * 0x20 + 1;
		int thcw = tile.getWidth() * 0x20 + 1;
		int tgrw = tile.getWidth() * 0x40 + 1;
		
		float mul = 2;
		for(int y = 0; y < height * 32; y++) {
			for(int x = 0; x < width * 32; x++) {
				int vidx = (x + y * width * 32) * 3 * 6;
				int cidx = (x + y * width * 32) * 4 * 6;
				int midx = (x + y * width * 32) * 16 * 6;
				int uidx = (x + y * width * 32) * 2 * 6;
				
				int c00 = tile_colors[(x    ) + (y    ) * tchw];
				int c01 = tile_colors[(x    ) + (y + 1) * tchw];
				int c10 = tile_colors[(x + 1) + (y    ) * tchw];
				int c11 = tile_colors[(x + 1) + (y + 1) * tchw];
				
				float h00 = tile_heights[(x    ) + (y    ) * thcw];
				float h01 = tile_heights[(x    ) + (y + 1) * thcw];
				float h10 = tile_heights[(x + 1) + (y    ) * thcw];
				float h11 = tile_heights[(x + 1) + (y + 1) * thcw];
				
				long m00 = tile_mats[(x*2    ) + (y*2    ) * tgrw];
				long m01 = tile_mats[(x*2    ) + (y*2 + 1) * tgrw];
				long m10 = tile_mats[(x*2 + 1) + (y*2    ) * tgrw];
				long m11 = tile_mats[(x*2 + 1) + (y*2 + 1) * tgrw];
				
				float x0 = x;
				float x1 = x + 1;
				float y0 = y;
				float y1 = y + 1;
				
				//   00 --+-- 10
				//   |    |  .'|
				//   |____|.'  |
				//   |   .'    |
				//   | .'      |
				//   01 ------11
				
				for(int j = 0; j < 6; j++) {
					writeMaterial(materials, midx +  0, m00);
					writeMaterial(materials, midx +  4, m01);
					writeMaterial(materials, midx +  8, m10);
					writeMaterial(materials, midx + 12, m11);
					midx += 16;
				}
				
				verts[vidx    ] = x0 * mul;
				verts[vidx + 1] = y0 * mul;
				verts[vidx + 2] = h00;
				writeColor(colors, cidx, c00);
				writeUv(uvs, uidx, 0, 0);
				
				verts[vidx + 3] = x1 * mul;
				verts[vidx + 4] = y0 * mul;
				verts[vidx + 5] = h10;
				writeColor(colors, cidx + 4, c10);
				writeUv(uvs, uidx + 2, 1, 0);
				
				verts[vidx + 6] = x0 * mul;
				verts[vidx + 7] = y1 * mul;
				verts[vidx + 8] = h01;
				writeColor(colors, cidx + 8, c01);
				writeUv(uvs, uidx + 4, 0, 1);
				
				uidx += 6;
				vidx += 9;
				cidx += 12;
				
				verts[vidx    ] = x0 * mul;
				verts[vidx + 1] = y1 * mul;
				verts[vidx + 2] = h01;
				writeColor(colors, cidx, c01);
				writeUv(uvs, uidx, 0, 1);
				
				verts[vidx + 3] = x1 * mul;
				verts[vidx + 4] = y0 * mul;
				verts[vidx + 5] = h10;
				writeColor(colors, cidx + 4, c10);
				writeUv(uvs, uidx + 2, 1, 0);
				
				verts[vidx + 6] = x1 * mul;
				verts[vidx + 7] = y1 * mul;
				verts[vidx + 8] = h11;
				writeColor(colors, cidx + 8, c11);
				writeUv(uvs, uidx + 4, 1, 1);
			}
		}
		
		// 0xa98942
		// 0x51d6a3
		// 0x2081be
		
		/*
		int a = 1;
		for(int i = 0; i < 1000; i += 4) {
			System.out.printf("[%04x%04x%04x%04x] ",
				materials[i + 3],
				materials[i + 2],
				materials[i + 1],
				materials[i + 0]
			);
			
			if((a++) > 20) {
				a = 0;
				System.out.println();
			}
		}
		System.out.println();
		*/
		
		buildObject(verts, colors, uvs, materials);
	}
	
	public TileMesh(TilePart part) {
		int width = 1;
		int height = 1;
		
		
		float[] verts = new float[32 * 32 * 3 * 6];
		float[] colors = new float[32 * 32 * 4 * 6];
		float[] uvs = new float[32 * 32 * 2 * 6];
		int[] materials = new int[32 * 32 * 16 * 6];
		
		float[] tile_heights = part.vertexHeight; // tile.getVertexHeight();
		long[] tile_mats = part.ground; // tile.getGround();
		int[] tile_colors = part.vertexColor; // tile.getVertexColor();
		
		int tchw = 0x20 + 1;
		int thcw = 0x20 + 1;
		int tgrw = 0x40 + 1;
		
		float mul = 2;
		for(int y = 0; y < height * 32; y++) {
			for(int x = 0; x < width * 32; x++) {
				int vidx = (x + y * width * 32) * 3 * 6;
				int cidx = (x + y * width * 32) * 4 * 6;
				int midx = (x + y * width * 32) * 16 * 6;
				int uidx = (x + y * width * 32) * 2 * 6;
				
				int c00 = tile_colors[(x    ) + (y    ) * tchw]; //tile.getVertexColor(x, y);
				int c01 = tile_colors[(x    ) + (y + 1) * tchw]; //tile.getVertexColor(x, y + 1);
				int c10 = tile_colors[(x + 1) + (y    ) * tchw]; //tile.getVertexColor(x + 1, y);
				int c11 = tile_colors[(x + 1) + (y + 1) * tchw]; //tile.getVertexColor(x + 1, y + 1);
				
				float h00 = tile_heights[(x    ) + (y    ) * thcw]; //tile.getVertexHeight(x, y);
				float h01 = tile_heights[(x    ) + (y + 1) * thcw]; //tile.getVertexHeight(x, y + 1);
				float h10 = tile_heights[(x + 1) + (y    ) * thcw]; //tile.getVertexHeight(x + 1, y);
				float h11 = tile_heights[(x + 1) + (y + 1) * thcw]; //tile.getVertexHeight(x + 1, y + 1);
				
				long m00 = tile_mats[(x*2    ) + (y*2    ) * tgrw]; //tile.getGroundMaterial(x * 2    , y * 2    );
				long m01 = tile_mats[(x*2    ) + (y*2 + 1) * tgrw]; //tile.getGroundMaterial(x * 2    , y * 2 + 1);
				long m10 = tile_mats[(x*2 + 1) + (y*2    ) * tgrw]; //tile.getGroundMaterial(x * 2 + 1, y * 2    );
				long m11 = tile_mats[(x*2 + 1) + (y*2 + 1) * tgrw]; //tile.getGroundMaterial(x * 2 + 1, y * 2 + 1);
				
				float x0 = x;
				float x1 = x + 1;
				float y0 = y;
				float y1 = y + 1;
				
				//   00 --+-- 10
				//   |    |  .'|
				//   |____|.'  |
				//   |   .'    |
				//   | .'      |
				//   01 ------11
				
				for(int j = 0; j < 6; j++) {
					writeMaterial(materials, midx +  0, m00);
					writeMaterial(materials, midx +  4, m01);
					writeMaterial(materials, midx +  8, m10);
					writeMaterial(materials, midx + 12, m11);
					midx += 16;
				}
				
				verts[vidx    ] = x0 * mul;
				verts[vidx + 1] = y0 * mul;
				verts[vidx + 2] = h00;
				writeColor(colors, cidx, c00);
				writeUv(uvs, uidx, 0, 0);
				
				verts[vidx + 3] = x1 * mul;
				verts[vidx + 4] = y0 * mul;
				verts[vidx + 5] = h10;
				writeColor(colors, cidx + 4, c10);
				writeUv(uvs, uidx + 2, 1, 0);
				
				verts[vidx + 6] = x0 * mul;
				verts[vidx + 7] = y1 * mul;
				verts[vidx + 8] = h01;
				writeColor(colors, cidx + 8, c01);
				writeUv(uvs, uidx + 4, 0, 1);
				
				uidx += 6;
				vidx += 9;
				cidx += 12;
				
				verts[vidx    ] = x0 * mul;
				verts[vidx + 1] = y1 * mul;
				verts[vidx + 2] = h01;
				writeColor(colors, cidx, c01);
				writeUv(uvs, uidx, 0, 1);
				
				verts[vidx + 3] = x1 * mul;
				verts[vidx + 4] = y0 * mul;
				verts[vidx + 5] = h10;
				writeColor(colors, cidx + 4, c10);
				writeUv(uvs, uidx + 2, 1, 0);
				
				verts[vidx + 6] = x1 * mul;
				verts[vidx + 7] = y1 * mul;
				verts[vidx + 8] = h11;
				writeColor(colors, cidx + 8, c11);
				writeUv(uvs, uidx + 4, 1, 1);
			}
		}
		
		buildObject(verts, colors, uvs, materials);
	}
	
	private void writeColor(float[] array, int offset, int rgba) {
		float a = ((rgba >> 24) & 0xff) / 255.0f;
		float r = ((rgba >> 16) & 0xff) / 255.0f;
		float g = ((rgba >>  8) & 0xff) / 255.0f;
		float b = ((rgba      ) & 0xff) / 255.0f;
		
		array[offset    ] = r;
		array[offset + 1] = g;
		array[offset + 2] = b;
		array[offset + 3] = a;
	}
	
	private void writeUv(float[] array, int offset, float x, float y) {
		array[offset    ] = x;
		array[offset + 1] = y;
	}
	
	private void writeMaterial(int[] array, int offset, long mat) {
		array[offset    ] = (int)((mat        ) & 0xffff);
		array[offset + 1] = (int)((mat >>> 16L) & 0xffff);
		array[offset + 2] = (int)((mat >>> 32L) & 0xffff);
		array[offset + 3] = (int)((mat >>> 48L) & 0xffff);
	}
	
	private void buildObject(float[] verts, float[] colors, float[] uvs, int[] materials) {
		FloatBuffer verticesBuffer = null;
		FloatBuffer colorsBuffer = null;
		FloatBuffer texturesBuffer = null;
		IntBuffer materialsBuffer = null;
		
		vertexCount = verts.length / 3;
		try {
			verticesBuffer = MemoryUtil.memAllocFloat(verts.length);
			verticesBuffer.put(verts).flip();
			
			colorsBuffer = MemoryUtil.memAllocFloat(colors.length);
			colorsBuffer.put(colors).flip();
			
			texturesBuffer = MemoryUtil.memAllocFloat(uvs.length);
			texturesBuffer.put(uvs).flip();
			
			materialsBuffer = MemoryUtil.memAllocInt(materials.length);
			materialsBuffer.put(materials).flip();
			
			vaoId = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vaoId);
			
			vboVertex = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertex);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
			
			vboColors = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboColors);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0L);
			
			vboTextures = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTextures);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texturesBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 0, 0L);
			
			vboMaterial = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboMaterial);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, materialsBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(3, 4, GL11.GL_INT, false, 64,  0L);
			GL20.glVertexAttribPointer(4, 4, GL11.GL_INT, false, 64, 16L);
			GL20.glVertexAttribPointer(5, 4, GL11.GL_INT, false, 64, 32L);
			GL20.glVertexAttribPointer(6, 4, GL11.GL_INT, false, 64, 48L);
			
			
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL30.glBindVertexArray(0);
		} finally {
			if(verticesBuffer != null) {
				MemoryUtil.memFree(verticesBuffer);
			}
			
			if(colorsBuffer != null) {
				MemoryUtil.memFree(colorsBuffer);
			}

			if(texturesBuffer != null) {
				MemoryUtil.memFree(texturesBuffer);
			}
			
			if(materialsBuffer != null) {
				MemoryUtil.memFree(materialsBuffer);
			}
		}
	}
	
	public int getVaoId() {
		return vaoId;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public void render() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		GL20.glEnableVertexAttribArray(5);
		GL20.glEnableVertexAttribArray(6);
		
		final int textures = TileShader.textures.length;
		for(int i = 0; i < textures; i++) TileShader.textures[i].bind();
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		for(int i = 0; i < textures; i++) TileShader.textures[i].unbind();
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL20.glDisableVertexAttribArray(5);
		GL20.glDisableVertexAttribArray(6);
		GL30.glBindVertexArray(0);
	}
	
	public void renderDirect() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		GL20.glEnableVertexAttribArray(5);
		GL20.glEnableVertexAttribArray(6);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL20.glDisableVertexAttribArray(5);
		GL20.glDisableVertexAttribArray(6);
//		GL30.glBindVertexArray(0);
	}
	
	public void renderShadows() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
	
	public void renderShadowsDirect() {
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		GL20.glDisableVertexAttribArray(0);
	}
	
	public void cleanup() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(vboVertex);
		GL15.glDeleteBuffers(vboColors);
		GL15.glDeleteBuffers(vboMaterial);
	}
}
