package com.hardcoded.lwjgl.mesh;

import java.util.List;

import org.joml.Matrix4f;

import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.PartShader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;
import com.hardcoded.world.utils.BoxBounds;
import com.hardcoded.world.utils.PartRotation;
import com.hardcoded.world.utils.ShapeUtils.Bounds3D;

/**
 * A part mesh.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class PartMesh extends RenderableMeshImpl {
	private final PartShader shader;
	private final SMPart part;
	
	public PartMesh(Lod lod, PartShader shader, SMPart part) throws Exception {
		super(lod);
		this.part = part;
		this.shader = shader;
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
	
	public void render(ChildShape shape, Bounds3D bounds) {
		float x = shape.xPos - 0.5f;
		float y = shape.yPos - 0.5f;
		float z = shape.zPos - 0.5f;
		RigidBody body = shape.body;
		
		Matrix4f matrix = new Matrix4f();
		
		{
			if(body.isGridLocked_0_2 == 2) {
				matrix.rotate(body.quat);
			} else {
				if(body.staticFlags < -1) {
					matrix.rotate(body.quat);
				}
			}
			
			// https://stackoverflow.com/questions/15777757/drawing-normals-in-lwjgl-messes-with-lighting
			matrix.translateLocal(body.xWorld, body.yWorld, body.zWorld);
			matrix.scale(1 / 4.0f);
			matrix.translate(x, y, z);
			applyRotation(shape, matrix);
//			{
//				float angle = (float)Math.toRadians((System.currentTimeMillis() % 7200L) / 20.0f);
//				matrix.rotateY(angle);
//			}
		}
		
		shader.setModelMatrix(matrix);
		{
			int rgba = shape.colorRGBA;
			float r = ((rgba >> 24) & 0xff) / 255.0f;
			float g = ((rgba >> 16) & 0xff) / 255.0f;
			float b = ((rgba >>  8) & 0xff) / 255.0f;
			float a = ((rgba      ) & 0xff) / 255.0f;
			shader.setColor(r, g, b, a);
		}
		
		for(int i = 0; i < meshes.length; i++) {
			List<Texture> texs = textures[i];
			MeshMaterial mat = mats[i];
			
			for(Texture t : texs) t.bind();
			mat.bind(shader);
			meshes[i].render();
			mat.unbind(shader);
			for(Texture t : texs) t.unbind();
		}
	}
	
	public void render() {
		for(int i = 0; i < meshes.length; i++) {
			List<Texture> texs = textures[i];
			MeshMaterial mat = mats[i];
			
			for(Texture t : texs) t.bind();
			mat.bind(shader);
			meshes[i].render();
			mat.unbind(shader);
			for(Texture t : texs) t.unbind();
		}
	}
}
