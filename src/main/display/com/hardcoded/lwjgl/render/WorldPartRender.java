package com.hardcoded.lwjgl.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.lwjgl.LwjglOptions;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.shader.PartShader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;
import com.hardcoded.world.utils.BoxBounds;
import com.hardcoded.world.utils.PartRotation;

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
	
	private void applyRotation(ChildShape shape, Matrix4f matrix) {
		BoxBounds bounds = part.getBounds();
		Matrix4f mat1 = PartRotation.getRotationMultiplier(shape.partRotation).get(new Matrix4f());
		mat1.translate(
			(bounds.getWidth() - 1) / 2.0f,
			(bounds.getHeight() - 1) / 2.0f,
			(bounds.getDepth() - 1) / 2.0f
		);
		matrix.mul(mat1);
	}
	
	public Matrix4f calculateMatrix(ChildShape shape) {
		float x = shape.xPos - 0.5f;
		float y = shape.yPos - 0.5f;
		float z = shape.zPos - 0.5f;
		RigidBody body = shape.body;
		
		Matrix4f matrix = new Matrix4f();
		if(body.isGridLocked_0_2 == 2) {
			matrix.rotate(body.quat);
		} else {
			if(body.staticFlags < -1) {
				matrix.rotate(body.quat);
			}
		}
		
		matrix.translateLocal(body.xWorld, body.yWorld, body.zWorld);
		matrix.scale(1 / 4.0f);
		matrix.translate(x, y, z);
		applyRotation(shape, matrix);
//		{
//			float angle = (float)Math.toRadians((System.currentTimeMillis() % 7200L) / 20.0f);
//			matrix.rotateY(angle);
//		}
		
		return matrix;
	}
	
	public void render(ChildShape shape) {
		Matrix4f mat = calculateMatrix(shape);
		shader.setModelMatrix(mat);
		{
			int rgba = shape.colorRGBA;
			float r = ((rgba >> 24) & 0xff) / 255.0f;
			float g = ((rgba >> 16) & 0xff) / 255.0f;
			float b = ((rgba >>  8) & 0xff) / 255.0f;
			float a = ((rgba      ) & 0xff) / 255.0f;
			shader.setColor(r, g, b, a);
		}
		
		if(LwjglOptions.LOD_OBJECTS && !meshes.isEmpty()) {
			PartMesh mesh = meshes.get(meshes.size() - 1);
			mesh.render();
			return;
		}
		
		for(PartMesh mesh : meshes) {
			mesh.render();
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
