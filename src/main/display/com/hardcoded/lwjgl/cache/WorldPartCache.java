package com.hardcoded.lwjgl.cache;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.hardcoded.db.types.Renderable;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.lwjgl.mesh.PartMesh;
import com.hardcoded.lwjgl.shader.PartShader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;
import com.hardcoded.world.utils.BoxBounds;
import com.hardcoded.world.utils.PartRotation;

/**
 * A part cahe.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldPartCache implements WorldObjectCache {
	public final List<PartMesh> meshes;
	public final PartShader shader;
	public final SMPart part;
	
	public WorldPartCache(SMPart part, PartShader shader) {
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
	
	public Matrix4f calculateMatrix(WorldBlueprintCache.BodyChild shape) {
		float x = shape.xPos - 0.5f;
		float y = shape.yPos - 0.5f;
		float z = shape.zPos - 0.5f;
		
		Matrix4f matrix = new Matrix4f();
		matrix.scale(1 / 4.0f);
		matrix.translate(x, y, z);
		
		BoxBounds bounds = part.getBounds();
		Matrix4f mat1 = PartRotation.getAxisRotation(shape.xaxis, shape.zaxis);
		mat1.translate(
			(bounds.getWidth() - 1) / 2.0f,
			(bounds.getHeight() - 1) / 2.0f,
			(bounds.getDepth() - 1) / 2.0f
		);
		matrix.mul(mat1);
		
		return matrix;
	}
}
