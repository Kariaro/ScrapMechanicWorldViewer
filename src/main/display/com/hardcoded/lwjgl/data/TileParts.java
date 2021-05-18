package com.hardcoded.lwjgl.data;

import java.util.HashMap;
import java.util.Map;

import com.hardcoded.lwjgl.mesh.TileMesh;
import com.hardcoded.tile.Tile;
import com.hardcoded.tile.impl.TilePart;

/**
 * A tile container.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class TileParts {
	private Tile tile;
	private Map<Long, TilePart> parts;
	private Map<Long, TileMesh> meshes;
	
	public TileParts(Tile tile) {
		this.tile = tile;
		this.parts = new HashMap<>();
		this.meshes = new HashMap<>();
		
		for(int y = 0; y < tile.getHeight(); y++) {
			for(int x = 0; x < tile.getWidth(); x++) {
				long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
				
				parts.put(index, tile.getPart(x, y));
			}
		}
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public TilePart getPart(int x, int y) {
		long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
		return parts.get(index);
	}
	
	public TileMesh getMesh(int x, int y) {
		long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
		TileMesh mesh = meshes.get(index);
		
		if(mesh == null) {
			TilePart part = getPart(x, y);
			mesh = new TileMesh(part);
			meshes.put(index, mesh);
		}
		
		return mesh;
	}
}
