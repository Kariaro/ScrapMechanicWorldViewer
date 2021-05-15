package com.hardcoded.lwjgl.mesh;

import java.util.List;

import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMHarvestable;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.Shader;

/**
 * A harvestable mesh.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class HarvestableMesh extends RenderableMeshImpl {
	public final Shader shader;
	public final SMHarvestable harvestable;
	
	public HarvestableMesh(Lod lod, Shader shader, SMHarvestable harvestable) throws Exception {
		super(lod);
		this.harvestable = harvestable;
		this.shader = shader;
	}
	
	public void render() {
		for(int i = 0; i < meshes.length; i++) {
			List<Texture> texs = textures[i];
			MeshMaterial mat = mats[i];
			
			for(Texture t : texs) t.bind();
			mat.bind(shader);
			meshes[i].render();
			mat.unbind(shader);
			for(Texture t : texs) t.unbind();
		}
	}
}
