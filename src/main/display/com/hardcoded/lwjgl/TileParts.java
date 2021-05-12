package com.hardcoded.lwjgl;

import java.util.HashMap;
import java.util.Map;

import com.hardcoded.tile.Tile;
import com.hardcoded.tile.impl.TileImpl;

public class TileParts {
	private Map<Long, Tile> tiles;
	
	public TileParts(Tile tile) {
		this.tiles = new HashMap<>();
		
		for(int y = 0; y < tile.getHeight(); y++) {
			for(int x = 0; x < tile.getWidth(); x++) {
				long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
				
				TileImpl impl = new TileImpl(1, 1);
				impl.setPart(0, 0, tile.getPart(x, y));
				tiles.put(index, impl);
			}
		}
	}
	
	public Tile getTile(int x, int y) {
		long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
		return tiles.get(index);
	}
}
