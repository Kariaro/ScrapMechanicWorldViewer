package com.hardcoded.lwjgl.cache;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMHarvestable;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.shader.AssetShader;

/**
 * A harvestable renderer.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class WorldHarvestableCache implements WorldObjectCache {
	public final List<HarvestableMesh> meshes;
	public final AssetShader shader;
	
	public WorldHarvestableCache(SMHarvestable harvestable, AssetShader shader) {
		this.meshes = new ArrayList<>();
		this.shader = shader;
		
		try {
			Renderable rend = harvestable.renderable;
			for(Lod lod : rend.lodList) {
				meshes.add(new HarvestableMesh(lod, shader, harvestable));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
