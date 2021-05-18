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
import com.hardcoded.lwjgl.mesh.AssetMesh;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.mesh.TileMesh;
import com.hardcoded.lwjgl.render.WorldAssetRender;
import com.hardcoded.lwjgl.render.WorldHarvestableRender;
import com.hardcoded.lwjgl.render.WorldTileRender;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.lwjgl.shader.TileShader;
import com.hardcoded.math.Quat;
import com.hardcoded.math.Vec3;
import com.hardcoded.sm.objects.TileData;
import com.hardcoded.tile.impl.TilePart;
import com.hardcoded.tile.object.Asset;
import com.hardcoded.tile.object.Harvestable;

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
	public TileTestRender(WorldContentHandler handler, Camera camera) {
		this.handler = handler;
		this.camera = camera;
	}
	
	public void init() {
		this.tileShader = handler.tileShader;
		this.assetShader = handler.assetShader;
	}
	
	private Matrix4f toShadowSpace = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f projectionView = new Matrix4f();
	public void set(Matrix4f toShadowSpace, Matrix4f viewMatrix, Matrix4f projectionView) {
		this.toShadowSpace = toShadowSpace;
		this.viewMatrix = viewMatrix;
		this.projectionView = projectionView;
	}

	public void render(Vector3f position, int radius) {
		int xx = (int)(position.x / 64);
		int yy = (int)(position.y / 64);
		
		final int ys = yy - radius - 1;
		final int xs = xx - radius - 1;
		final int ye = yy + radius;
		final int xe = xx + radius;
		
		List<TileTest> list = new ArrayList<>();
		for(int y = ys; y < ye; y++) {
			for(int x = xs; x < xe; x++) {
				TileTest test = cacheTile(x, y);
				if(test != null) {
					list.add(test);
				}
			}
		}
		
		renderList(list);
	}
	
	private void renderList(List<TileTest> list) {
		renderListTileGround(list);
		renderListTileEntities(list);
	}

	private void renderListTileGround(List<TileTest> list) {
		tileShader.bind();
		tileShader.setProjectionView(projectionView);
		tileShader.setViewMatrix(viewMatrix);
		tileShader.setShadowMapSpace(toShadowSpace);
		
		final int textures = TileShader.textures.length;
		for(int i = 0; i < textures; i++) TileShader.textures[i].bind();
		
		for(TileTest test : list) {
			tileShader.setModelMatrix(test.mesh_modelMatrix);
			test.mesh.renderDirect();
		}
		
		for(int i = 0; i < textures; i++) TileShader.textures[i].unbind();
		GL30.glBindVertexArray(0);
		
		tileShader.unbind();
	}
	
	private void renderListTileEntities(List<TileTest> list) {
		assetShader.bind();
		assetShader.setProjectionView(projectionView);
		assetShader.setViewMatrix(viewMatrix);
		assetShader.setShadowMapSpace(toShadowSpace);
		
		for(TileTest tile : list) {
			for(WorldAssetRender render : tile.assets.keySet()) {
				renderListPairAssets(render, tile.assets.get(render));
			}
			
			for(WorldHarvestableRender render : tile.harvestables.keySet()) {
				renderListPairHarvestables(render, tile.harvestables.get(render));
			}
		}
		
		assetShader.unbind();
	}
	
	private void renderListPairAssets(WorldAssetRender render, List<Pair<Asset, Matrix4f>> pairs) {
		AssetMesh mesh = render.meshes.get(0);
		if(!mesh.isLoaded()) return;
		
		for(int i = 0; i < mesh.meshes.length; i++) {
			List<Texture> texs = mesh.textures[i];
			MeshMaterial mat = mesh.mats[i];
			
			for(Texture t : texs) t.bind();
			mat.bind(assetShader);
			
			for(Pair<Asset, Matrix4f> pair : pairs) {
				mesh.bindColor(pair.a, mat);
				assetShader.setModelMatrix(pair.b);
				mesh.meshes[i].render();
			}
			
			mat.unbind(assetShader);
			for(Texture t : texs) t.unbind();
		}
	}
	
	private void renderListPairHarvestables(WorldHarvestableRender render, List<Pair<Harvestable, Matrix4f>> pairs) {
		HarvestableMesh mesh = render.meshes.get(0);
		if(!mesh.isLoaded()) return;
		
		for(int i = 0; i < mesh.meshes.length; i++) {
			List<Texture> texs = mesh.textures[i];
			MeshMaterial mat = mesh.mats[i];
			
			for(Texture t : texs) t.bind();
			mat.bind(assetShader);
			
			for(Pair<Harvestable, Matrix4f> pair : pairs) {
				{
					int color = pair.a.getColor();
					float r = ((color >> 24) & 0xff) / 255.0f;
					float g = ((color >> 16) & 0xff) / 255.0f;
					float b = ((color >>  8) & 0xff) / 255.0f;
					float a = ((color >>  0) & 0xff) / 255.0f;
					assetShader.setColor(r, g, b, a);
				}
				
				assetShader.setModelMatrix(pair.b);
				mesh.meshes[i].render();
			}
			
			mat.unbind(assetShader);
			for(Texture t : texs) t.unbind();
		}
	}
	
	private Map<Long, TileTest> cache = new HashMap<>();
	public static class Pair<T, E> {
		public T a;
		public E b;
		
		public Pair(T a, E b) {
			this.a = a;
			this.b = b;
		}
	}
	
	public static class TileTest {
		public Matrix4f mesh_modelMatrix;
		public TileMesh mesh;
		public Map<WorldAssetRender, List<Pair<Asset, Matrix4f>>> assets = new HashMap<>();
		public Map<WorldHarvestableRender, List<Pair<Harvestable, Matrix4f>>> harvestables = new HashMap<>();
		// TODO: Sort harvestable uuid and asset uuids!
	}
	
	public TileTest cacheTile(int x, int y) {
		long index = ((long)(x) & 0xffffffffL) | (((long)y) << 32L);
		{
			TileTest test = cache.get(index);
			if(test != null) return test;
		}
		
		WorldTileRender render = handler.getTileRender(x, y);
		if(render == null) return null;
		
		TileTest cache_tile = new TileTest();
		
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
				
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				WorldAssetRender rend = handler.getAssetRender(uuid);
				if(rend != null) {
					List<Pair<Asset, Matrix4f>> list = cache_tile.assets.get(rend);
					if(list == null) {
						list = new ArrayList<>();
						cache_tile.assets.put(rend, list);
					}
					
					list.add(new Pair<>(asset, modelMatrix));
				}
			}
			
			for(int j = 0; j < harvestables.size(); j++) {
				Harvestable harvestable = harvestables.get(j);
				
				UUID uuid = harvestable.getUuid();
				Vec3 apos = harvestable.getPosition();
				Quat arot = harvestable.getRotation();
				Vec3 _size = harvestable.getSize();
				
				Matrix4f modelMatrix = new Matrix4f(transform)
					.translate(new Vector3f(apos.toArray()))
					.rotate(new Quaternionf(arot.getX(), arot.getY(), arot.getZ(), arot.getW()))
					.scale(_size.getX(), _size.getY(), _size.getZ());
				
				WorldHarvestableRender rend = handler.getHarvestableRender(uuid);
				if(rend != null) {
					List<Pair<Harvestable, Matrix4f>> list = cache_tile.harvestables.get(rend);
					if(list == null) {
						list = new ArrayList<>();
						cache_tile.harvestables.put(rend, list);
					}
					
					list.add(new Pair<>(harvestable, modelMatrix));
				}
			}
		}
		
		return cache_tile;
	}
}
