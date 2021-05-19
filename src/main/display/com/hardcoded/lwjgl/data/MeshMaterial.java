package com.hardcoded.lwjgl.data;

import org.lwjgl.opengl.GL11;

import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.db.types.SMMaterial;
import com.hardcoded.db.types.SMMaterial.Types;
import com.hardcoded.lwjgl.meshrender.RenderPipeline;
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
	
	private int cache_flags = -1;
	public int getPipeFlags() {
		if(sm == null) return 0;
		if(cache_flags == -1) {
			int flags = 0;
			
			if(!sm.hasDefined(Types.FLIP_BACKFACE_NORMALS)) {
				flags |= RenderPipeline.PIPE_CULL_FACE;
			}
			
			if(sm.hasDefined(Types.AO_TEX)) flags  |= RenderPipeline.PIPE_AO_TEX;
			if(sm.hasDefined(Types.ASG_TEX)) flags |= RenderPipeline.PIPE_ASG_TEX;
			if(sm.hasDefined(Types.NOR_TEX)) flags |= RenderPipeline.PIPE_NOR_TEX;
			if(sm.hasDefined(Types.ALPHA)) flags |= RenderPipeline.PIPE_ALPHA;
			
			cache_flags = flags;
		}
		
		return cache_flags;
	}
}
