package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMHarvestable;
import com.hardcoded.lwjgl.WorldRender;
import com.hardcoded.lwjgl.mesh.HarvestableMesh;
import com.hardcoded.lwjgl.shader.Shader;
import com.hardcoded.tile.object.Harvestable;

/**
 * A harvestable renderer.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class WorldHarvestableRender implements WorldObjectRender {
	private final List<HarvestableMesh> meshes;
	private final SMHarvestable harvestable;
	private final Shader shader;
	
	public WorldHarvestableRender(SMHarvestable harvestable, Shader shader) {
		this.meshes = new ArrayList<>();
		this.harvestable = harvestable;
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
			int color = this.harvestable.color;
			//color = harvestable.getColor();
			float r = ((color >> 24) & 0xff) / 255.0f;
			float g = ((color >> 16) & 0xff) / 255.0f;
			float b = ((color >>  8) & 0xff) / 255.0f;
			float a = ((color >>  0) & 0xff) / 255.0f;
			shader.setUniform("color", r, g, b, a);
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
		if(WorldRender.LOD_OBJECTS && !meshes.isEmpty()) {
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
