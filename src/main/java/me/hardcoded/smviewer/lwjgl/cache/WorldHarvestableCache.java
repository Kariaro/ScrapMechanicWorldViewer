package me.hardcoded.smviewer.lwjgl.cache;

import java.util.ArrayList;
import java.util.List;

import me.hardcoded.smviewer.db.types.Renderable;
import me.hardcoded.smviewer.db.types.Renderable.Lod;
import me.hardcoded.smviewer.db.types.SMHarvestable;
import me.hardcoded.smviewer.lwjgl.mesh.HarvestableMesh;
import me.hardcoded.smviewer.lwjgl.shader.AssetShader;

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
			for (Lod lod : rend.lodList) {
				meshes.add(new HarvestableMesh(lod, shader, harvestable));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
