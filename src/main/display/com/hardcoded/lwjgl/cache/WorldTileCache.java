package com.hardcoded.lwjgl.cache;

import java.util.*;

import org.joml.Matrix4f;

import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.data.TileParts;
import com.hardcoded.lwjgl.mesh.TileMesh;
import com.hardcoded.lwjgl.util.MathUtils;
import com.hardcoded.prefab.readers.PrefabFileReader;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.impl.TilePart;
import com.hardcoded.tile.object.*;

/**
 * A tile cache.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldTileCache {
	private WorldContentHandler handler;
	
	private Map<Long, TileCache> cache = new HashMap<>();
	
	// DEBUG
	public TileParts parts;
	
	public WorldTileCache(WorldContentHandler handler, int x, int y, TileParts parts) {
		this.handler = handler;
		this.parts = parts;
	}
	
	public static class TileObject<T> {
		public T object;
		public Matrix4f modelMatrix;
		
		public TileObject(T a, Matrix4f modelMatrix) {
			this.object = a;
			this.modelMatrix = modelMatrix;
		}
	}
	
	public static class TileCache {
		public Map<WorldHarvestableCache, List<TileObject<Harvestable>>> harvestables = new HashMap<>(); 
		public Map<WorldBlueprintCache, List<TileObject<Blueprint>>> blueprints = new HashMap<>();
		public Map<WorldAssetCache, List<TileObject<Asset>>> assets = new HashMap<>();
		
		public Matrix4f mesh_modelMatrix;
		public TileMesh mesh;
	}
	
	public TileCache getTileCache(int x, int y) {
		long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
		{
			TileCache tile_cache = cache.get(index);
			if(tile_cache != null) return tile_cache;
		}
		
		int ox = TileData.getTileOffsetX(x, y);
		int oy = TileData.getTileOffsetY(x, y);
		
		TilePart part = parts.getPart(ox, oy);
		
		float c_sw = TileData.getTileCliffLevel(x    , y    );
		float c_se = TileData.getTileCliffLevel(x + 1, y    );
		float c_nw = TileData.getTileCliffLevel(x    , y + 1);
		float c_ne = TileData.getTileCliffLevel(x + 1, y + 1);
		float cliff_level = Math.min(Math.min(c_sw, c_se), Math.min(c_nw, c_ne)) * 8;
		
		float tile_x = x * 64;
		float tile_y = y * 64;
		float tile_z = cliff_level;
		
		int rot = TileData.getTileRotation(x, y);
		float rot_offset = rot * (float)(Math.PI / 2.0);
		Matrix4f transform = new Matrix4f()
			.translateLocal(-32, -32, 0).rotateLocalZ(rot_offset).translateLocal(32, 32, 0)
			.translateLocal(tile_x, tile_y, tile_z);
		
		TileCache tile_cache = new TileCache();
		tile_cache.mesh = parts.getMesh(ox, oy);
		tile_cache.mesh_modelMatrix = transform;
		
		for(int i = 0; i < 4; i++) {
			List<Asset> assets = part.assets[i];
			for(Asset asset : assets) {
				WorldAssetCache cache = handler.getAssetCache(asset.getUuid());
				if(cache != null) {
					Matrix4f modelMatrix = new Matrix4f(transform)
						.mul(MathUtils.getModelMatrix(asset));
					
					List<TileObject<Asset>> list = tile_cache.assets.get(cache);
					if(list == null) {
						list = new ArrayList<>();
						tile_cache.assets.put(cache, list);
					}
					
					list.add(new TileObject<>(asset, modelMatrix));
				}
			}
			
			List<Harvestable> harvestables = part.harvestables[i];
			for(Harvestable harvestable : harvestables) {
				WorldHarvestableCache cache = handler.getHarvestableCache(harvestable.getUuid());
				if(cache != null) {
					Matrix4f modelMatrix = new Matrix4f(transform)
						.mul(MathUtils.getModelMatrix(harvestable));
					
					List<TileObject<Harvestable>> list = tile_cache.harvestables.get(cache);
					if(list == null) {
						list = new ArrayList<>();
						tile_cache.harvestables.put(cache, list);
					}
					
					list.add(new TileObject<>(harvestable, modelMatrix));
				}
			}
		}
		
		for(Blueprint blueprint : part.blueprints) {
			WorldBlueprintCache cache = handler.getBlueprintCache(blueprint);
			if(cache != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.mul(MathUtils.getModelMatrix(blueprint));
				
				List<TileObject<Blueprint>> list = tile_cache.blueprints.get(cache);
				if(list == null) {
					list = new ArrayList<>();
					tile_cache.blueprints.put(cache, list);
				}
				
				list.add(new TileObject<>(blueprint, modelMatrix));
			}
		}
		
		try {
			for(Prefab pref : part.prefabs) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.mul(MathUtils.getModelMatrix(pref));
				
				cacheWritePrefab(tile_cache, modelMatrix, pref);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return tile_cache;
	}
	
	private void cacheWritePrefab(TileCache tile_cache, Matrix4f transform, Prefab unloaded) throws Exception {
		Prefab prefab = PrefabFileReader.readPrefab(handler.getContext().resolve(unloaded.getPath()));
		
		for(Asset asset : prefab.getAssets()) {
			WorldAssetCache cache = handler.getAssetCache(asset.getUuid());
			if(cache != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.mul(MathUtils.getModelMatrix(asset));
				
				List<TileObject<Asset>> list = tile_cache.assets.get(cache);
				if(list == null) {
					list = new ArrayList<>();
					tile_cache.assets.put(cache, list);
				}
				
				list.add(new TileObject<>(asset, modelMatrix));
			}
		}
		
		for(Harvestable harvestable : prefab.getHarvestables()) {
			WorldHarvestableCache cache = handler.getHarvestableCache(harvestable.getUuid());
			if(cache != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.mul(MathUtils.getModelMatrix(harvestable));
				
				List<TileObject<Harvestable>> list = tile_cache.harvestables.get(cache);
				if(list == null) {
					list = new ArrayList<>();
					tile_cache.harvestables.put(cache, list);
				}
				
				list.add(new TileObject<>(harvestable, modelMatrix));
			}
		}
		
		for(Blueprint blueprint : prefab.getBlueprints()) {
			WorldBlueprintCache cache = handler.getBlueprintCache(blueprint);
			if(cache != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.mul(MathUtils.getModelMatrix(blueprint));
				
				List<TileObject<Blueprint>> list = tile_cache.blueprints.get(cache);
				if(list == null) {
					list = new ArrayList<>();
					tile_cache.blueprints.put(cache, list);
				}
				
				list.add(new TileObject<>(blueprint, modelMatrix));
			}
		}
		
		// TODO: Nodes / Decals
		
		for(Prefab pref : prefab.getPrefabs()) {
//			Vec3 apos = pref.getPosition();
//			Quat arot = pref.getRotation();
//			Vec3 size = pref.getSize();
			
			Matrix4f modelMatrix = new Matrix4f(transform)
				.mul(MathUtils.getModelMatrix(pref));
//				.translate(new Vector3f(apos.toArray()))
//				.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
//				.scale(size.getX(), size.getY(), size.getZ());
			
			cacheWritePrefab(tile_cache, modelMatrix, pref);
		}
	}
}
