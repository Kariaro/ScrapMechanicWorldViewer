package com.hardcoded.lwjgl.mesh;

import java.util.List;

import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;

/**
 * A interface for objects that are loaded from {@link com.hardcoded.db.types.Renderable}.
 * 
 * @author HardCoded
 * @since v0.2
 */
public interface RenderableMesh {
	
	List<Texture> loadTextures(MeshMaterial meshMat);
	
	void render();
	void renderShadows();
}
