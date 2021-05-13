package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.SMAsset;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.mesh.AssetMesh;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.tile.object.Asset;

/**
 * An asset renderer.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldAssetRender {
	private final List<AssetMesh> meshes;
	
	public WorldAssetRender(SMAsset asset, AssetShader shader) {
		meshes = new ArrayList<>();
		
		try {
			Renderable rend = asset.renderable;
			for(Lod lod : rend.lodList) {
				meshes.add(new AssetMesh(lod, shader, asset));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(Vector3f pos, Asset asset, Quaternionf quat, Vector3f scale, Camera camera) {
		//float dist = camera.getPosition().distance(pos);
		
		if(!meshes.isEmpty()) {
			AssetMesh mesh = meshes.get(meshes.size() - 1);
			mesh.render(asset, pos, quat, scale);
			return;
		}
		
		for(AssetMesh mesh : meshes) {
			mesh.render(asset, pos, quat, scale);
			break;
		}
	}
}
