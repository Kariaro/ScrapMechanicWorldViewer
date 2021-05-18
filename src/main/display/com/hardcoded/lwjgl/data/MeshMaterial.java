package com.hardcoded.lwjgl.data;

import org.lwjgl.opengl.GL11;

import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.db.types.SMMaterial;
import com.hardcoded.db.types.SMMaterial.Types;
import com.hardcoded.lwjgl.shader.Shader;

/**
 * A mesh material class.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class MeshMaterial {
	public final String key;
	public final MeshMap map;
	public SMMaterial sm;
	
	public MeshMaterial(String key, MeshMap map) {
		this.key = key;
		this.map = map;
	}
	
	public void bind(Shader shader) {
		if(sm == null) return;
		
		if(sm.hasDefined(Types.FLIP_BACKFACE_NORMALS)) {
			//GL11.glDisable(GL11.GL_CULL_FACE);
		}
		
		if(sm.hasDefined(Types.ALPHA)) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			shader.setUniform("hasAlpha", true);
		}
	}
	
	public void unbind(Shader shader) {
		if(sm == null) return;
		
		if(sm.hasDefined(Types.FLIP_BACKFACE_NORMALS)) {
			//GL11.glEnable(GL11.GL_CULL_FACE);
		}
		
		if(sm.hasDefined(Types.ALPHA)) {
			GL11.glDisable(GL11.GL_BLEND);
			shader.setUniform("hasAlpha", false);
		}
	}
}
