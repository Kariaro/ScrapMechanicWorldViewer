package com.hardcoded.lwjgl.mesh;

import java.util.List;

import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;

/**
 * A interface for objects that are loaded from {@link com.hardcoded.db.types.Renderable}.
 * 
 * @author HardCoded
 * @since v0.2
 */
public interface RenderableMesh {
	
	/**
	 * Load a texture
	 * @param map
	 * @param list
	 * @return
	 * @throws Exception
	 */
	MeshMaterial loadTextures(MeshMap map, List<Texture> list) throws Exception;
	
	int getMeshIndex(String name);
	
	Mesh getMesh(String name);
	
	void renderShadows();
}
