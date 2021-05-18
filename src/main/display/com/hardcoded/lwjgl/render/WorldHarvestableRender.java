package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMHarvestable;
import com.hardcoded.lwjgl.LwjglOptions;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.tile.object.Harvestable;

/**
 * A harvestable renderer.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class WorldHarvestableRender implements WorldObjectRender {
	public final List<HarvestableMesh> meshes;
	public final AssetShader shader;
	
	public WorldHarvestableRender(SMHarvestable harvestable, AssetShader shader) {
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
	
	public void render(Harvestable harvestable) {
		{
			int color = harvestable.getColor();
			float r = ((color >> 24) & 0xff) / 255.0f;
			float g = ((color >> 16) & 0xff) / 255.0f;
			float b = ((color >>  8) & 0xff) / 255.0f;
			float a = ((color >>  0) & 0xff) / 255.0f;
			shader.setColor(r, g, b, a);
		}
		
		if(!meshes.isEmpty()) {
			HarvestableMesh mesh = meshes.get(meshes.size() - 1);
			mesh.render();
			return;
		}
		
		for(HarvestableMesh mesh : meshes) {
			mesh.render();
			break;
		}
	}
	
	@Override
	public void renderShadows() {
		if(LwjglOptions.LOD_OBJECTS) {
			HarvestableMesh mesh = meshes.get(meshes.size() - 1);
			mesh.renderShadows();
			return;
		}
		
		for(HarvestableMesh mesh : meshes) {
			mesh.renderShadows();
			break;
		}
	}
}
