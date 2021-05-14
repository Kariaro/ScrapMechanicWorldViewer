package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMHarvestable;
import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.shader.Shader;
import com.hardcoded.tile.object.Harvestable;

/**
 * A harvestable renderer.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class WorldHarvestableRender {
	private final List<HarvestableMesh> meshes;
	
	public WorldHarvestableRender(SMHarvestable harvestable, Shader shader) {
		meshes = new ArrayList<>();
		
		try {
			Renderable rend = harvestable.renderable;
			for(Lod lod : rend.lodList) {
				meshes.add(new HarvestableMesh(lod, shader, harvestable));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(Vector3f pos, Harvestable harvestable, Quaternionf quat, Vector3f scale, Camera camera) {
		//float dist = camera.getPosition().distance(pos);
		
		if(!meshes.isEmpty()) {
			HarvestableMesh mesh = meshes.get(meshes.size() - 1);
			mesh.render(harvestable, pos, quat, scale);
			return;
		}
		
		for(HarvestableMesh mesh : meshes) {
			mesh.render(harvestable, pos, quat, scale);
			break;
		}
	}
}
