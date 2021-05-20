package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.cache.*;
import com.hardcoded.lwjgl.cache.WorldBlueprintCache.BlueprintCache;
import com.hardcoded.lwjgl.cache.WorldTileCache.TileCache;
import com.hardcoded.lwjgl.cache.WorldTileCache.TileObject;
import com.hardcoded.lwjgl.mesh.AssetMesh;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.mesh.Mesh;
import com.hardcoded.lwjgl.render.RenderPipeline.RenderObject;
import com.hardcoded.tile.object.Asset;
import com.hardcoded.tile.object.Blueprint;
import com.hardcoded.tile.object.Harvestable;

/**
 * This is a tile pipeline class.
 * 
 * @author HardCoded
 * @since v0.3
 */
public class TilePipeline extends RenderPipe {
	public TilePipeline(RenderPipeline pipeline) {
		super(pipeline);
	}
	
	@Override
	public void render() {
		Camera camera = pipeline.getCamera();
		
		int ss = 3;
		Vector3f cam_pos = camera.getPosition();
		int xx = (int)(cam_pos.x / 64);
		int yy = (int)(cam_pos.y / 64);
		
		List<TileCache> list = new ArrayList<>();
		for(int y = yy - ss - 1; y < yy + ss; y++) {
			for(int x = xx - ss - 1; x < xx + ss; x++) {
				WorldTileCache render = handler.getTileCache(x, y);
				if(render != null) {
					list.add(render.getTileCache(x, y));
				}
			}
		}
		
		for(TileCache tile : list) {
			push(RenderObject.Tile.get()
				.setVao(tile.mesh.getVaoId())
				.setVertexCount(tile.mesh.getVertexCount())
				.setModelMatrix(tile.mesh_modelMatrix)
			);
			
			for(WorldHarvestableCache cache : tile.harvestables.keySet()) {
				HarvestableMesh harvestable_mesh = cache.meshes.get(0);
				if(!harvestable_mesh.isLoaded()) continue;
				
				List<TileObject<Harvestable>> cache_list = tile.harvestables.get(cache);
				for(TileObject<Harvestable> object : cache_list) {
					for(int i = 0; i < harvestable_mesh.meshes.length; i++) {
						Mesh mesh = harvestable_mesh.meshes[i];
						
						push(RenderObject.Asset.get()
							.setVao(mesh.getVaoId())
							.setColor(object.object.getColor())
							.setFlags(harvestable_mesh.mats[i].getPipeFlags())
							.setTextures(harvestable_mesh.textures[i])
							.setVertexCount(mesh.getVertexCount())
							.setModelMatrix(object.modelMatrix)
						);
					}
				}
			}
			
			for(WorldAssetCache cache : tile.assets.keySet()) {
				final AssetMesh asset_mesh = cache.meshes.get(0);
				if(!asset_mesh.isLoaded()) continue;
				
				List<TileObject<Asset>> cache_list = tile.assets.get(cache);
				for(TileObject<Asset> object : cache_list) {
					for(int i = 0; i < asset_mesh.meshes.length; i++) {
						Mesh mesh = asset_mesh.meshes[i];
						
						push(RenderObject.Asset.get()
							.setVao(mesh.getVaoId())
							.setColor(asset_mesh.getColor(object.object, i))
							.setTextures(asset_mesh.textures[i])
							.setFlags(asset_mesh.mats[i].getPipeFlags())
							.setVertexCount(mesh.getVertexCount())
							.setModelMatrix(object.modelMatrix)
						);
					}
				}
			}
			
			for(WorldBlueprintCache bp_cache : tile.blueprints.keySet()) {
				if(!bp_cache.isLoaded()) continue;
				
				List<TileObject<Blueprint>> bp_cache_list = tile.blueprints.get(bp_cache);
				for(TileObject<Blueprint> bp_object : bp_cache_list) {
					BlueprintCache bp = bp_cache.getCache();
					
					// Add blocks
					for(WorldBlockCache cache : bp.blocks.keySet()) {
						List<RenderObject.Block> cache_list = bp.blocks.get(cache);
						
						final int len = cache_list.size();
						for(int i = 0; i < len; i++) {
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
					for(WorldPartCache cache : bp.parts.keySet()) {
						List<RenderObject.Part> cache_list = bp.parts.get(cache);
						
						final int len = cache_list.size();
						for(int i = 0; i < len; i++) {
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
