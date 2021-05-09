package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.shader.PartShader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.world.types.SMPart;
import com.hardcoded.world.types.Renderable;
import com.hardcoded.world.types.Renderable.Lod;
import com.hardcoded.world.types.ShapeUtils.Bounds3D;

/**
 * A part renderer.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldPartRender {
	private final List<PartMesh> meshes;
	
	public WorldPartRender(SMPart part, PartShader shader) {
		meshes = new ArrayList<>();
		
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
		//Vector3f pos = camera.getPosition();
		
		for(PartMesh mesh : meshes) {
			//float dist = pos.distance(shape.xPos, shape.yPos, 0) * 2;
			
			mesh.render(shape, bounds);
			break;
		}
	}
	
	public void render(Vector3f pos, Quaternionf quat, Vector3f scale, Camera camera) {
		//Vector3f pos = camera.getPosition();
		
		for(PartMesh mesh : meshes) {
			//float dist = pos.distance(shape.xPos, shape.yPos, 0) * 2;
			
			mesh.render(pos, quat, scale);
			break;
		}
	}
}
