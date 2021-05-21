package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.hardcoded.game.World;
import com.hardcoded.lwjgl.cache.WorldPartCache;
import com.hardcoded.lwjgl.mesh.Mesh;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.render.RenderPipeline.RenderObject;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;

/**
 * This is a part pipeline class.
 * 
 * @author HardCoded
 * @since v0.3
 */
public class PartPipeline extends RenderPipe {
	public PartPipeline(RenderPipeline pipeline) {
		super(pipeline);
	}
	
	private List<RenderObject.Part> parts = new ArrayList<>();
	private boolean reload_cache = false;
	
	@Override
	public void onWorldReload() {
		reload_cache = true;
		parts.clear();
	}
	
	@Override
	public void render() {
		if(reload_cache) {
			World world = pipeline.getWorld();
			List<RigidBody> list = world.getRigidBodies();
			
			boolean loaded = true;
			for(RigidBody body : list) {
				for(ChildShape shape : body.shapes) {
					WorldPartCache cache = handler.getPartCache(shape.uuid);
					if(cache != null) {
						PartMesh part_mesh = cache.meshes.get(0);
						if(!part_mesh.isLoaded()) {
							loaded = false;
						}
					}
				}
			}
			
			if(!loaded) {
				return;
			}
			
			this.parts = new ArrayList<>();
			
			for(RigidBody body : list) {
				for(ChildShape shape : body.shapes) {
					WorldPartCache cache = handler.getPartCache(shape.uuid);
					if(cache != null) {
						PartMesh part_mesh = cache.meshes.get(0);
						
						for(int i = 0; i < part_mesh.meshes.length; i++) {
							Mesh mesh = part_mesh.meshes[i];
							
							parts.add(RenderObject.Part.get()
								.setVao(mesh.getVaoId())
								.setColor(shape.colorRGBA)
								.setFlags(part_mesh.mats[i].getPipeFlags())
								.setTextures(part_mesh.textures[i])
								.setVertexCount(mesh.getVertexCount())
								.setModelMatrix(cache.calculateMatrix(shape))
							);
						}
					}
				}
			}
			
			reload_cache = false;
			
			System.out.println("Loaded parts: " + parts.size());
		}
		
		Vector3f camera = pipeline.getCamera().getPosition();
		for(RenderObject.Part object : parts) {
			float dist = object.modelMatrix.transformPosition(new Vector3f()).distance(camera);
			if(dist < 1000) {
				push(object);
			}
		}
	}
}
