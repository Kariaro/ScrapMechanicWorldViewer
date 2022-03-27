package me.hardcoded.smviewer.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import me.hardcoded.smviewer.lwjgl.cache.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import me.hardcoded.smviewer.lwjgl.Camera;
import me.hardcoded.smviewer.lwjgl.cache.WorldBlueprintCache.BlueprintCache;
import me.hardcoded.smviewer.lwjgl.cache.WorldTileCache.TileCache;
import me.hardcoded.smviewer.lwjgl.cache.WorldTileCache.TileObject;
import me.hardcoded.smviewer.lwjgl.mesh.AssetMesh;
import me.hardcoded.smviewer.lwjgl.mesh.HarvestableMesh;
import me.hardcoded.smviewer.lwjgl.mesh.Mesh;
import me.hardcoded.smviewer.lwjgl.render.RenderPipeline.RenderObject;
import me.hardcoded.smreader.tile.object.Asset;
import me.hardcoded.smreader.tile.object.Blueprint;
import me.hardcoded.smreader.tile.object.Harvestable;

/**
 * This is a tile pipeline class.
 * 
 * @author HardCoded
 * @since v0.3
 */
public class TilePipeline extends RenderPipe {
	private static final int REGION = 4;
	
	public TilePipeline(RenderPipeline pipeline) {
		super(pipeline);
	}
	
	@Override
	public void render() {
		Camera camera = pipeline.getCamera();
		
		int ss = REGION;
		Vector3f cam_pos = camera.getPosition();
		int xx = (int)(cam_pos.x / 64);
		int yy = (int)(cam_pos.y / 64);
		
		List<TileCache> list = new ArrayList<>();
		for (int y = yy - ss - 1; y < yy + ss; y++) {
			for (int x = xx - ss - 1; x < xx + ss; x++) {
				WorldTileCache render = handler.getTileCache(x, y);
				if (render != null) {
					list.add(render.getTileCache(x, y));
				}
			}
		}
		
		for (TileCache tile : list) {
			push(RenderObject.Tile.get()
				.setVao(tile.mesh.getVaoId())
				.setVertexCount(tile.mesh.getVertexCount())
				.setModelMatrix(tile.mesh_modelMatrix)
			);
			
			for (WorldHarvestableCache cache : tile.harvestables.keySet()) {
				List<TileObject<Harvestable>> cache_list = tile.harvestables.get(cache);
				for (TileObject<Harvestable> object : cache_list) {
					float dist = object.modelMatrix.transformPosition(new Vector3f()).distance(camera.getPosition());
					
					HarvestableMesh harvestableMesh = cache.meshes.get(0);
					for (int j = 1; j < cache.meshes.size(); j++) {
						harvestableMesh = cache.meshes.get(j);
						if (harvestableMesh.maxViewDistance > dist) {
							break;
						}
					}
					
					if (harvestableMesh.isLoaded()) {
						for (int i = 0; i < harvestableMesh.meshes.length; i++) {
							Mesh mesh = harvestableMesh.meshes[i];
							
							double meshSize = mesh.getMeshSize();
							if (meshSize / dist > harvestableMesh.minViewSize) {
								continue;
							}
							
							push(RenderObject.Asset.get()
								.setVao(mesh.getVaoId())
								.setColor(object.object.getColor())
								.setFlags(harvestableMesh.mats[i].getPipeFlags())
								.setTextures(harvestableMesh.textures[i])
								.setVertexCount(mesh.getVertexCount())
								.setModelMatrix(object.modelMatrix)
							);
						}
					}
				}
			}
			
			for (WorldAssetCache cache : tile.assets.keySet()) {
				List<TileObject<Asset>> cache_list = tile.assets.get(cache);
				for (TileObject<Asset> object : cache_list) {
					float dist = object.modelMatrix.transformPosition(new Vector3f()).distance(camera.getPosition());
					
					AssetMesh assetMesh = cache.meshes.get(0);
					for (int j = 1; j < cache.meshes.size(); j++) {
						assetMesh = cache.meshes.get(j);
						if (assetMesh.maxViewDistance > dist) {
							break;
						}
					}
					
					if (assetMesh.isLoaded()) {
						for (int i = 0; i < assetMesh.meshes.length; i++) {
							Mesh mesh = assetMesh.meshes[i];
							
							push(RenderObject.Asset.get()
								.setVao(mesh.getVaoId())
								.setColor(assetMesh.getColor(object.object, i))
								.setTextures(assetMesh.textures[i])
								.setFlags(assetMesh.mats[i].getPipeFlags())
								.setVertexCount(mesh.getVertexCount())
								.setModelMatrix(object.modelMatrix)
							);
						}
					}
				}
			}
			
			for (WorldBlueprintCache bp_cache : tile.blueprints.keySet()) {
				if (!bp_cache.isLoaded()) continue;
				
				List<TileObject<Blueprint>> bp_cache_list = tile.blueprints.get(bp_cache);
				for (TileObject<Blueprint> bp_object : bp_cache_list) {
					BlueprintCache bp = bp_cache.getCache();
					
					// Add blocks
					for (WorldBlockCache cache : bp.blocks.keySet()) {
						List<RenderObject.Block> cache_list = bp.blocks.get(cache);
						
						final int len = cache_list.size();
						for (int i = 0; i < len; i++) {
							RenderObject.Block ro = cache_list.get(i);
							
							push(RenderObject.Block.get()
								.setVao(WorldBlockCache.mesh.getVaoId())
								.setColor(ro.color)
								.setTextures(ro.textures)
								.setVertexCount(WorldBlockCache.mesh.getVertexCount())
								.setModelMatrix(bp_object.modelMatrix.mul(ro.modelMatrix, new Matrix4f()))
								
								// Block specific
								.setTiling(ro.tiling)
								.setLocalTransform(ro.localTransform)
								.setScale(ro.scale)
							);
						}
					}
					
					// Add parts
					for (WorldPartCache cache : bp.parts.keySet()) {
						List<RenderObject.Part> cache_list = bp.parts.get(cache);
						
						final int len = cache_list.size();
						for (int i = 0; i < len; i++) {
							RenderObject.Part ro = cache_list.get(i);
							
							push(RenderObject.Part.get()
								.setVao(ro.vao)
								.setColor(ro.color)
								.setFlags(ro.flags)
								.setTextures(ro.textures)
								.setVertexCount(ro.vertexCount)
								.setModelMatrix(bp_object.modelMatrix.mul(ro.modelMatrix, new Matrix4f()))
							);
						}
					}
				}
			}
			
			// Nodes
			// Decals
		}
	}
}
