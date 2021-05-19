package com.hardcoded.lwjgl.meshrender;

import java.util.*;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.WorldContentHandler;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.mesh.*;
import com.hardcoded.lwjgl.render.*;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.lwjgl.shader.TileShader;
import com.hardcoded.lwjgl.shadow.ShadowShader;
import com.hardcoded.math.Quat;
import com.hardcoded.math.Vec3;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.impl.TilePart;
import com.hardcoded.tile.object.Asset;
import com.hardcoded.tile.object.Harvestable;
import com.hardcoded.tile.object.Prefab;

/**
 * This class is a tile mesh render
 * 
 * @author HardCoded
 * @since v0.2
 */
public class TileTestRender {
	private final WorldContentHandler handler;
	private final Camera camera;
	
	private TileShader tileShader;
	private AssetShader assetShader;
	private ShadowShader shadowShader;
	public TileTestRender(WorldContentHandler handler, Camera camera) {
		this.handler = handler;
		this.camera = camera;
	}
	
	public void init() {
		this.tileShader = handler.tileShader;
		this.assetShader = handler.assetShader;
		this.shadowShader = handler.shadowShader;
	}
	
	private Matrix4f toShadowSpace = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f projectionView = new Matrix4f();
	private Matrix4f lightDirection = new Matrix4f();
	public void set(Matrix4f toShadowSpace, Matrix4f viewMatrix, Matrix4f projectionView, Matrix4f lightDirection) {
		this.toShadowSpace = toShadowSpace;
		this.viewMatrix = viewMatrix;
		this.projectionView = projectionView;
		this.lightDirection = lightDirection;
	}
	
	private List<TileCached> genList(Vector3f position, int radius) {
		int xx = (int)(position.x / 64);
		int yy = (int)(position.y / 64);
		
		final int ys = yy - radius - 1;
		final int xs = xx - radius - 1;
		final int ye = yy + radius;
		final int xe = xx + radius;
		
		List<TileCached> list = new ArrayList<>();
		for(int y = ys; y < ye; y++) {
			for(int x = xs; x < xe; x++) {
				TileCached test = cacheTile(x, y);
				if(test != null) {
					list.add(test);
				}
			}
		}
		
		return list;
	}
	
	public void render(Vector3f position, int radius) {
		renderList(genList(position, radius));
	}
	
	private void renderList(List<TileCached> list) {
		renderListTileGround(list);
		
		{
			assetShader.bind();
			assetShader.setProjectionView(projectionView);
			assetShader.setViewMatrix(viewMatrix);
			assetShader.setShadowMapSpace(toShadowSpace);
			assetShader.setLightDirection(lightDirection);
			
			for(TileCached tile : list) {
				for(WorldAssetRender render : tile.assets.keySet()) {
					renderListPairAssets(render, tile.assets.get(render));
				}
				
				for(WorldHarvestableRender render : tile.harvestables.keySet()) {
					renderListPairHarvestables(render, tile.harvestables.get(render));
				}
			}
			
			assetShader.unbind();
		}
		
		{
			for(TileCached tile : list) {
				for(WorldPrefabRender render : tile.prefabs.keySet()) {
					for(TileObject<Prefab> object : tile.prefabs.get(render)) {
						render.render(object.modelMatrix, projectionView, viewMatrix, toShadowSpace);
					}
				}
			}
		}
	}

	private void renderListTileGround(List<TileCached> list) {
		tileShader.bind();
		tileShader.setProjectionView(projectionView);
		tileShader.setViewMatrix(viewMatrix);
		tileShader.setShadowMapSpace(toShadowSpace);
		tileShader.setLightDirection(lightDirection);
		
		final int textures = TileShader.textures.length;
		for(int i = 0; i < textures; i++) TileShader.textures[i].bind();
		
		for(TileCached test : list) {
			tileShader.setModelMatrix(test.mesh_modelMatrix);
			test.mesh.renderDirect();
		}
		
		for(int i = 0; i < textures; i++) TileShader.textures[i].unbind();
		GL30.glBindVertexArray(0);
		
		tileShader.unbind();
	}
	
	private void renderListPairAssets(WorldAssetRender render, List<TileObject<Asset>> pairs) {
		AssetMesh mesh = render.meshes.get(0);
		if(!mesh.isLoaded()) return;
		
		for(int i = 0; i < mesh.meshes.length; i++) {
			List<Texture> texs = mesh.textures[i];
			MeshMaterial mat = mesh.mats[i];
			
			for(Texture t : texs) t.bind();
			mat.bind(assetShader);
			
			Mesh m = mesh.meshes[i];
			
			m.startRender();
			for(TileObject<Asset> pair : pairs) {
				mesh.bindColor(pair.object, mat);
				assetShader.setModelMatrix(pair.modelMatrix);
				m.renderDirect();
			}
			m.endRender();
			
			mat.unbind(assetShader);
			for(Texture t : texs) t.unbind();
		}
	}
	
	private void renderListPairHarvestables(WorldHarvestableRender render, List<TileObject<Harvestable>> pairs) {
		HarvestableMesh mesh = render.meshes.get(0);
		if(!mesh.isLoaded()) return;
		
		for(int i = 0; i < mesh.meshes.length; i++) {
			List<Texture> texs = mesh.textures[i];
			MeshMaterial mat = mesh.mats[i];
			
			for(Texture t : texs) t.bind();
			mat.bind(assetShader);
			
			Mesh m = mesh.meshes[i];
			
			m.startRender();
			for(TileObject<Harvestable> pair : pairs) {
				{
					int color = pair.object.getColor();
					float r = ((color >> 24) & 0xff) / 255.0f;
					float g = ((color >> 16) & 0xff) / 255.0f;
					float b = ((color >>  8) & 0xff) / 255.0f;
					float a = ((color >>  0) & 0xff) / 255.0f;
					assetShader.setColor(r, g, b, a);
				}
				
				assetShader.setModelMatrix(pair.modelMatrix);
				m.renderDirect();
			}
			m.endRender();
			
			mat.unbind(assetShader);
			for(Texture t : texs) t.unbind();
		}
	}
	
	private Map<Long, TileCached> cache = new HashMap<>();
	private static class TileObject<T> {
		public T object;
		public Matrix4f modelMatrix;
		
		public TileObject(T a, Matrix4f modelMatrix) {
			this.object = a;
			this.modelMatrix = modelMatrix;
		}
	}
	
	private static class TileCached {
		public Matrix4f mesh_modelMatrix;
		public TileMesh mesh;
		public Map<WorldAssetRender, List<TileObject<Asset>>> assets = new HashMap<>();
		public Map<WorldHarvestableRender, List<TileObject<Harvestable>>> harvestables = new HashMap<>();
		public Map<WorldPrefabRender, List<TileObject<Prefab>>> prefabs = new HashMap<>();
	}
	
	private TileCached cacheTile(int x, int y) {
		long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
		{
			TileCached test = cache.get(index);
			if(test != null) return test;
		}
		
		WorldTileRender render = handler.getTileRender(x, y);
		if(render == null) return null;
		
		TileCached cache_tile = new TileCached();
		int ox = TileData.getTileOffsetX(x, y);
		int oy = TileData.getTileOffsetY(x, y);
		
		TilePart part = render.parts.getPart(ox, oy);
		TileMesh tm = render.parts.getMesh(ox, oy);
		
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
		
		cache_tile.mesh_modelMatrix = transform;
		cache_tile.mesh = tm;
		
		for(int i = 0; i < 4; i++) {
			List<Harvestable> harvestables = part.harvestables[i];
			List<Asset> assets = part.assets[i];
			
			for(int j = 0; j < assets.size(); j++) {
				Asset asset = assets.get(j);
				
				UUID uuid = asset.getUuid();
				Vec3 apos = asset.getPosition();
				Quat arot = asset.getRotation();
				Vec3 _size = asset.getSize();
				
				WorldAssetRender rend = handler.getAssetRender(uuid);
				if(rend != null) {
					Matrix4f modelMatrix = new Matrix4f(transform)
						.translate(new Vector3f(apos.toArray()))
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
						.scale(_size.getX(), _size.getY(), _size.getZ());
					
					List<TileObject<Asset>> list = cache_tile.assets.get(rend);
					if(list == null) {
						list = new ArrayList<>();
						cache_tile.assets.put(rend, list);
					}
					
					list.add(new TileObject<>(asset, modelMatrix));
				}
			}
			
			for(int j = 0; j < harvestables.size(); j++) {
				Harvestable harvestable = harvestables.get(j);
				
				UUID uuid = harvestable.getUuid();
				Vec3 apos = harvestable.getPosition();
				Quat arot = harvestable.getRotation();
				Vec3 _size = harvestable.getSize();
				
				WorldHarvestableRender rend = handler.getHarvestableRender(uuid);
				if(rend != null) {
					Matrix4f modelMatrix = new Matrix4f(transform)
						.translate(new Vector3f(apos.toArray()))
						.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
						.scale(_size.getX(), _size.getY(), _size.getZ());
					
					List<TileObject<Harvestable>> list = cache_tile.harvestables.get(rend);
					if(list == null) {
						list = new ArrayList<>();
						cache_tile.harvestables.put(rend, list);
					}
					
					list.add(new TileObject<>(harvestable, modelMatrix));
				}
			}
		}
		
		for(Prefab prefab : part.prefabs) {
			Vec3 apos = prefab.getPosition();
			Quat arot = prefab.getRotation();
			Vec3 _size = prefab.getSize();
			
			WorldPrefabRender rend = handler.getPrefabRender(prefab.getPath());
			if(rend != null) {
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				List<TileObject<Prefab>> list = cache_tile.prefabs.get(rend);
				if(list == null) {
					list = new ArrayList<>();
					cache_tile.prefabs.put(rend, list);
				}
				
				list.add(new TileObject<>(rend.prefab, modelMatrix));
			}
		}
		
		return cache_tile;
	}
}
