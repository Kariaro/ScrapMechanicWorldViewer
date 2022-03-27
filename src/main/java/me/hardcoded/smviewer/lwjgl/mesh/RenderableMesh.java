package me.hardcoded.smviewer.lwjgl.mesh;

import java.util.List;

import me.hardcoded.smviewer.db.types.Renderable;
import me.hardcoded.smviewer.lwjgl.data.MeshMaterial;
import me.hardcoded.smviewer.lwjgl.data.Texture;

/**
 * A interface for objects that are loaded from {@link Renderable}.
 * 
 * @author HardCoded
 * @since v0.2
 */
public interface RenderableMesh {
	
	List<Texture> loadTextures(MeshMaterial meshMat);
	
	void render();
	void renderShadows();
}
