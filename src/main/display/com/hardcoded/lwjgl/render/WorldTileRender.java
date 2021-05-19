package com.hardcoded.lwjgl.render;

import java.util.*;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.data.TileParts;
import com.hardcoded.lwjgl.mesh.AssetMesh;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.mesh.TileMesh;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.lwjgl.shader.TileShader;
import com.hardcoded.lwjgl.shadow.ShadowShader;
import com.hardcoded.math.Quat;
import com.hardcoded.math.Vec3;
import com.hardcoded.prefab.readers.PrefabFileReader;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.impl.TilePart;
import com.hardcoded.tile.object.Asset;
import com.hardcoded.tile.object.Harvestable;
import com.hardcoded.tile.object.Prefab;

/**
 * A tile render.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldTileRender {
	private WorldContentHandler handler;
	
	private AssetShader assetShader;
	private TileShader tileShader;
	private Map<Long, TileCache> cache = new HashMap<>();
	
	// DEBUG
	public TileParts parts;
	
	public WorldTileRender(WorldContentHandler handler, int x, int y, TileParts parts) {
		this.handler = handler;
		this.parts = parts;
		this.tileShader = handler.tileShader;
		this.assetShader = handler.assetShader;
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
		public Map<WorldHarvestableRender, List<TileObject<Harvestable>>> harvestables = new HashMap<>(); 
		public Map<WorldAssetRender, List<TileObject<Asset>>> assets = new HashMap<>();
		
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
				WorldAssetRender rend = handler.getAssetRender(asset.getUuid());
				if(rend != null) {
					Vec3 apos = asset.getPosition();
					Quat arot = asset.getRotation();
					Vec3 size = asset.getSize();
					
					Matrix4f modelMatrix = new Matrix4f(transform)
						.translate(new Vector3f(apos.toArray()))
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
						.scale(size.getX(), size.getY(), size.getZ());
					
					List<TileObject<Asset>> list = tile_cache.assets.get(rend);
					if(list == null) {
						list = new ArrayList<>();
						tile_cache.assets.put(rend, list);
					}
					
					list.add(new TileObject<>(asset, modelMatrix));
				}
			}
			
			List<Harvestable> harvestables = part.harvestables[i];
			for(Harvestable harvestable : harvestables) {
				WorldHarvestableRender rend = handler.getHarvestableRender(harvestable.getUuid());
				if(rend != null) {
					Vec3 apos = harvestable.getPosition();
					Quat arot = harvestable.getRotation();
					Vec3 size = harvestable.getSize();
					
					Matrix4f modelMatrix = new Matrix4f(transform)
						.translate(new Vector3f(apos.toArray()))
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
						.scale(size.getX(), size.getY(), size.getZ());
					
					List<TileObject<Harvestable>> list = tile_cache.harvestables.get(rend);
					if(list == null) {
						list = new ArrayList<>();
						tile_cache.harvestables.put(rend, list);
					}
					
					list.add(new TileObject<>(harvestable, modelMatrix));
				}
			}
		}
		
		try {
			for(Prefab pref : part.prefabs) {
				Vec3 apos = pref.getPosition();
				Quat arot = pref.getRotation();
				Vec3 size = pref.getSize();
				
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(size.getX(), size.getY(), size.getZ());
				
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
			WorldAssetRender rend = handler.getAssetRender(asset.getUuid());
			if(rend != null) {
				Vec3 apos = asset.getPosition();
				Quat arot = asset.getRotation();
				Vec3 size = asset.getSize();
				
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(size.getX(), size.getY(), size.getZ());
				
				List<TileObject<Asset>> list = tile_cache.assets.get(rend);
				if(list == null) {
					list = new ArrayList<>();
					tile_cache.assets.put(rend, list);
				}
				
				list.add(new TileObject<>(asset, modelMatrix));
			}
		}
		
		for(Harvestable harvestable : prefab.getHarvestables()) {
			WorldHarvestableRender rend = handler.getHarvestableRender(harvestable.getUuid());
			if(rend != null) {
				Vec3 apos = harvestable.getPosition();
				Quat arot = harvestable.getRotation();
				Vec3 size = harvestable.getSize();
				
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(size.getX(), size.getY(), size.getZ());
				
				List<TileObject<Harvestable>> list = tile_cache.harvestables.get(rend);
				if(list == null) {
					list = new ArrayList<>();
					tile_cache.harvestables.put(rend, list);
				}
				
				list.add(new TileObject<>(harvestable, modelMatrix));
			}
		}
		
		// TODO: Blueprints / Nodes / Decals
		
		for(Prefab pref : prefab.getPrefabs()) {
			Vec3 apos = pref.getPosition();
			Quat arot = pref.getRotation();
			Vec3 size = pref.getSize();
			
			Matrix4f modelMatrix = new Matrix4f(transform)
				.translate(new Vector3f(apos.toArray()))
				.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
				.scale(size.getX(), size.getY(), size.getZ());
			
			cacheWritePrefab(tile_cache, modelMatrix, pref);
		}
	}
	
	
	
	
	public void render(int x, int y, Matrix4f toShadowSpace, Matrix4f viewMatrix, Matrix4f projectionView, Camera camera) {
		TileCache tile = getTileCache(x, y);
		
		tileShader.bind();
		tileShader.setProjectionView(projectionView);
		tileShader.setModelMatrix(tile.mesh_modelMatrix);
		tileShader.setShadowMapSpace(toShadowSpace);
		tile.mesh.render();
		tileShader.unbind();
		
		assetShader.bind();
		assetShader.setProjectionView(projectionView);
		assetShader.setViewMatrix(viewMatrix);
		assetShader.setShadowMapSpace(toShadowSpace);
		
		for(WorldAssetRender rend : tile.assets.keySet()) {
			List<TileObject<Asset>> list = tile.assets.get(rend);
			
			AssetMesh mesh = rend.meshes.get(rend.meshes.size() - 1);
			for(TileObject<Asset> object : list) {
				assetShader.setModelMatrix(object.modelMatrix);
				mesh.render(object.object);
			}
		}
		
		for(WorldHarvestableRender rend : tile.harvestables.keySet()) {
			List<TileObject<Harvestable>> list = tile.harvestables.get(rend);
			
			HarvestableMesh mesh = rend.meshes.get(rend.meshes.size() - 1);
			for(TileObject<Harvestable> object : list) {
				assetShader.setModelMatrix(object.modelMatrix);
				{
					int color = object.object.getColor();
					float r = ((color >> 24) & 0xff) / 255.0f;
					float g = ((color >> 16) & 0xff) / 255.0f;
					float b = ((color >>  8) & 0xff) / 255.0f;
					float a = ((color >>  0) & 0xff) / 255.0f;
					assetShader.setColor(r, g, b, a);
				}
				
				mesh.render();
			}
		}
		
		assetShader.unbind();
	}
	
	public void renderShadows(ShadowShader shader, int x, int y, Matrix4f mvpMatrix) {
		TileCache tile = getTileCache(x, y);
		
		shader.setMvpMatrix(mvpMatrix.mul(tile.mesh_modelMatrix, new Matrix4f()));
		tile.mesh.render();
		
		for(WorldAssetRender rend : tile.assets.keySet()) {
			List<TileObject<Asset>> list = tile.assets.get(rend);
			
			AssetMesh mesh = rend.meshes.get(rend.meshes.size() - 1);
			for(TileObject<Asset> object : list) {
				shader.setMvpMatrix(mvpMatrix.mul(object.modelMatrix, new Matrix4f()));
				mesh.renderShadows();
			}
		}
		
		for(WorldHarvestableRender rend : tile.harvestables.keySet()) {
			List<TileObject<Harvestable>> list = tile.harvestables.get(rend);
			
			HarvestableMesh mesh = rend.meshes.get(rend.meshes.size() - 1);
			for(TileObject<Harvestable> object : list) {
				shader.setMvpMatrix(mvpMatrix.mul(object.modelMatrix, new Matrix4f()));
				mesh.renderShadows();
			}
		}
	}
}
