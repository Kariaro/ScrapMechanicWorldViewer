package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.shader.PartShader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.world.utils.ShapeUtils.Bounds3D;

/**
 * A part renderer.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldPartRender implements WorldObjectRender {
	public final List<PartMesh> meshes;
	public final SMPart part;
	
	public WorldPartRender(SMPart part, PartShader shader) {
		meshes = new ArrayList<>();
		this.part = part;
		
		try {
			Renderable rend = part.renderable;
			for(Lod lod : rend.lodList) {
				meshes.add(new PartMesh(lod, shader, part));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(ChildShape shape, Bounds3D bounds, Camera camera) {
		//float dist = camera.getPosition().distance(pos);
		
		if(!meshes.isEmpty()) {
			PartMesh mesh = meshes.get(meshes.size() - 1);
			mesh.render(shape, bounds);
			return;
		}
		
		for(PartMesh mesh : meshes) {
			mesh.render(shape, bounds);
			break;
		}
	}
	
	public void render(Vector3f pos, Quaternionf quat, Vector3f scale, Camera camera) {
		//float dist = camera.getPosition().distance(pos);
		
		if(!meshes.isEmpty()) {
			PartMesh mesh = meshes.get(meshes.size() - 1);
			mesh.render(pos, quat, scale);
			return;
		}
		
		for(PartMesh mesh : meshes) {
			mesh.render(pos, quat, scale);
			break;
		}
	}
	
	@Override
	public void renderShadows() {
		if(!meshes.isEmpty()) {
			PartMesh mesh = meshes.get(meshes.size() - 1);
			mesh.renderShadows();
			return;
		}
		
		for(PartMesh mesh : meshes) {
			mesh.renderShadows();
			break;
		}
	}
}
