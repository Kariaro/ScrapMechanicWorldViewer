package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.lwjgl.Camera;
import com.hardcoded.lwjgl.WorldRender;
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
	public final PartShader shader;
	public final SMPart part;
	
	public WorldPartRender(SMPart part, PartShader shader) {
		this.meshes = new ArrayList<>();
		this.shader = shader;
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
		if(WorldRender.LOD_OBJECTS && !meshes.isEmpty()) {
			PartMesh mesh = meshes.get(meshes.size() - 1);
			mesh.render(shape, bounds);
			return;
		}
		
		for(PartMesh mesh : meshes) {
			mesh.render(shape, bounds);
			break;
		}
	}
	
//	public void render(Vector3f pos) {
//		//float dist = camera.getPosition().distance(pos);
////		Matrix4f transformationMatrix = new Matrix4f()
////			.translate(pos)
////			.rotate(quat)
////			.scale(scale)
////			.scale(1 / 4.0f);
////		
////		shader.setTransformationMatrix(transformationMatrix);
//		
//		if(!meshes.isEmpty()) {
//			PartMesh mesh = meshes.get(meshes.size() - 1);
//			mesh.render();
//			return;
//		}
//		
//		for(PartMesh mesh : meshes) {
//			mesh.render();
//			break;
//		}
//	}
	
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
