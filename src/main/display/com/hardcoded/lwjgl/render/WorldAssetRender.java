package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.mesh.AssetMesh;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.world.types.Renderable;
import com.hardcoded.world.types.Renderable.Lod;
import com.hardcoded.world.types.SMAsset;

/**
 * An asset renderer.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldAssetRender {
	private final List<AssetMesh> meshes;
	
	public WorldAssetRender(SMAsset part, AssetShader shader) {
		meshes = new ArrayList<>();
		
		try {
			Renderable rend = part.renderable;
			for(Lod lod : rend.lodList) {
				meshes.add(new AssetMesh(lod, shader, part));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(Vector3f pos, Quaternionf quat, Vector3f scale, Camera camera) {
//		for(int i = 0; i < meshes.size(); i++) {
//			AssetMesh mesh = meshes.get(i);
//			mesh.render(pos, quat, scale);
//			break;
//		}
		for(AssetMesh mesh : meshes) {
			mesh.render(pos, quat, scale);
			break;
		}
	}
}
