package com.hardcoded.lwjgl.mesh;

import java.util.List;

import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.PartShader;

/**
 * A part mesh.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class PartMesh extends RenderableMeshImpl {
	private final PartShader shader;
	
	public PartMesh(Lod lod, PartShader shader, SMPart part) throws Exception {
		super(lod);
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
