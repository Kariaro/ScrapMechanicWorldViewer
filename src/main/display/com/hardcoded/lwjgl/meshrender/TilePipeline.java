package com.hardcoded.lwjgl.meshrender;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.mesh.AssetMesh;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.mesh.Mesh;
import com.hardcoded.lwjgl.meshrender.RenderPipeline.RenderObject;
import com.hardcoded.lwjgl.render.WorldAssetRender;
import com.hardcoded.lwjgl.render.WorldHarvestableRender;
import com.hardcoded.lwjgl.render.WorldTileRender;
import com.hardcoded.lwjgl.render.WorldTileRender.TileCache;
import com.hardcoded.lwjgl.render.WorldTileRender.TileObject;
import com.hardcoded.tile.object.Asset;
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
				WorldTileRender render = handler.getTileRender(x, y);
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
			
			for(WorldHarvestableRender rend : tile.harvestables.keySet()) {
				HarvestableMesh harvestable_mesh = rend.meshes.get(0);
				if(!harvestable_mesh.isLoaded()) continue;
				
				List<TileObject<Harvestable>> rend_list = tile.harvestables.get(rend);
				for(TileObject<Harvestable> object : rend_list) {
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
			
			for(WorldAssetRender rend : tile.assets.keySet()) {
				final AssetMesh asset_mesh = rend.meshes.get(0);
				if(!asset_mesh.isLoaded()) continue;
				
				List<TileObject<Asset>> rend_list = tile.assets.get(rend);
				for(TileObject<Asset> object : rend_list) {
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
			
			// Blueprints
			// Nodes
			// Decals
		}
	}
}
