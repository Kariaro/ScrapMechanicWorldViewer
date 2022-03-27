package me.hardcoded.smviewer.lwjgl.mesh;

import java.util.List;

import me.hardcoded.smviewer.db.types.Renderable.Lod;
import me.hardcoded.smviewer.db.types.SMHarvestable;
import me.hardcoded.smviewer.lwjgl.data.MeshMaterial;
import me.hardcoded.smviewer.lwjgl.data.Texture;
import me.hardcoded.smviewer.lwjgl.shader.Shader;

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
		if(!isLoaded) return;
		
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
