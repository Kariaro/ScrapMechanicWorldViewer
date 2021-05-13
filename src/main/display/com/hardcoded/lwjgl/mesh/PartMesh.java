package com.hardcoded.lwjgl.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.PartShader;
import com.hardcoded.lwjgl.util.StaticMeshLoader;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;
import com.hardcoded.world.utils.PartBounds;
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
	
	@SuppressWarnings("unchecked")
	public PartMesh(Lod lod, PartShader shader, SMPart part) throws Exception {
		super(lod);
		this.part = part;
		this.shader = shader;
		
		String path = ScrapMechanicAssetHandler.resolvePath(lod.mesh);
		this.meshes = StaticMeshLoader.load(path);
		this.textures = new List[meshes.length];
		this.mats = new MeshMaterial[meshes.length];
		
		
		Map<String, MeshMap> maps = lod.subMeshMap;
		for(String name : maps.keySet()) {
			int index = getMeshIndex(name);
			
			List<Texture> list = new ArrayList<>();
			MeshMaterial meshMat = loadTextures(maps.get(name), list);
			if(meshMat == null) {
				System.out.println("PartMesh has no texture!!!");
				System.out.printf("    SubMesh \"%s\" -> %s\n", name, list);
				MeshMap meshMap = maps.get(name);
				System.out.println("    TextureList: " + meshMap.textureList);
				System.out.println("    Material: " + meshMap.material);
				
			}
			
			// TODO: Fallback option?
			if(index < 0) index = 0;
			mats[index] = meshMat;
			textures[index] = list;
		}
		System.out.println("--------------------");
	}
	
	private void applyRotation(ChildShape shape, Matrix4f matrix) {
		Matrix4f mul = PartRotation.getRotationMultiplier(shape.partRotation);
		if(mul != null) matrix.mul(mul);
		
		PartBounds bounds = part.getBounds();
		if(bounds != null) {
			matrix.translate(
				(bounds.getWidth() - 1) / 2.0f,
				(bounds.getHeight() - 1) / 2.0f,
				(bounds.getDepth() - 1) / 2.0f
			);
		}
	}
	
	public boolean render(ChildShape shape, Bounds3D bounds) {
		float x = shape.xPos - 0.5f;
		float y = shape.yPos - 0.5f;
		float z = shape.zPos - 0.5f;
		
		Matrix4f matrix = new Matrix4f();
		matrix.scale(1 / 4.0f);
		
		RigidBody body = shape.body;
		if(body.isStatic_0_2 == 2) {
			matrix.translateLocal(
				(x / 4.0f) + body.xWorld,
				(y / 4.0f) + body.yWorld,
				(z / 4.0f) + body.zWorld
			);
			matrix.rotateAroundLocal(body.quat,
				body.xWorld,
				body.yWorld,
				body.zWorld
			);
		} else {
			matrix.translateLocal(
				(x / 4.0f) + body.xWorld,
				(y / 4.0f) + body.yWorld,
				(z / 4.0f) + body.zWorld
			);
			
			if(body.staticFlags < -1) {
				matrix.rotateAroundLocal(body.quat,
					body.xWorld,
					body.yWorld,
					body.zWorld
				);
			}
		}
		
		applyRotation(shape, matrix);
		shader.setUniform("transformationMatrix", matrix);
		{
			int rgba = shape.colorRGBA;
			float r, g, b, a;
			{
				r = ((rgba >> 24) & 0xff) / 255.0f;
				g = ((rgba >> 16) & 0xff) / 255.0f;
				b = ((rgba >>  8) & 0xff) / 255.0f;
				a = ((rgba      ) & 0xff) / 255.0f;
			}
			shader.setUniform("color", r, g, b, a);
		}
		
		// TODO: Implement Lod objects
		/*
		if(animations != null) {
			Mesh[] msh = animations[0];
			for(int i = 0; i < msh.length; i++) {
				List<Texture> texs = textures[0];
				if(texs != null) {
					for(Texture t : texs) t.bind();
				}
				
				msh[i].render();
				if(texs != null) {
					for(Texture t : texs) t.unbind();
				}
			}
		} else {
			
		}
		*/
		
		for(int i = 0; i < textures.length; i++) {
			List<Texture> texs = textures[i];
			MeshMaterial mat = mats[i];
			
			if(texs != null) {
				for(Texture t : texs) t.bind();
				
				if(mat != null) {
					mat.bind(shader);
					meshes[i].render();
					mat.unbind(shader);
				} else {
					meshes[i].render();
				}
				
				for(Texture t : texs) t.unbind();
			} else {
				if(mat != null) {
					mat.bind(shader);
					meshes[i].render();
					mat.unbind(shader);
				} else {
					meshes[i].render();
				}
			}
		}
		
		
		// TODO: If size is smaller than minViewSize return 'false'
		// TODO: If distance is greater than maxViewDistance return 'false'
		return true;
	}
	
	public boolean render(Vector3f pos, Quaternionf quat, Vector3f scale) {
//		float x = shape.xPos - 0.5f;
//		float y = shape.yPos - 0.5f;
//		float z = shape.zPos - 0.5f;
		
		Matrix4f matrix = new Matrix4f();
		matrix.translate(pos);
		matrix.rotate(quat);
		matrix.scale(scale);
		matrix.scale(1 / 4.0f);
		
//		applyRotation(shape, matrix);
		shader.setUniform("transformationMatrix", matrix);
//		{
//			int rgba = shape.colorRGBA;
//			float r, g, b, a;
//			{
//				r = ((rgba >> 24) & 0xff) / 255.0f;
//				g = ((rgba >> 16) & 0xff) / 255.0f;
//				b = ((rgba >>  8) & 0xff) / 255.0f;
//				a = ((rgba      ) & 0xff) / 255.0f;
//			}
//			shader.setUniform("color", r, g, b, a);
//		}
		shader.setUniform("color", 1, 1, 1, 1);
		
		// TODO: Implement Lod objects
		/*
		if(animations != null) {
			Mesh[] msh = animations[0];
			for(int i = 0; i < msh.length; i++) {
				List<Texture> texs = textures[0];
				if(texs != null) {
					for(Texture t : texs) t.bind();
				}
				
				msh[i].render();
				if(texs != null) {
					for(Texture t : texs) t.unbind();
				}
			}
		} else {
			
		}
		*/
		
		for(int i = 0; i < textures.length; i++) {
			List<Texture> texs = textures[i];
			MeshMaterial mat = mats[i];
			
			if(texs != null) {
				for(Texture t : texs) t.bind();
				
				if(mat != null) {
					mat.bind(shader);
					meshes[i].render();
					mat.unbind(shader);
				} else {
					meshes[i].render();
				}
				
				for(Texture t : texs) t.unbind();
			} else {
				if(mat != null) {
					mat.bind(shader);
					meshes[i].render();
					mat.unbind(shader);
				} else {
					meshes[i].render();
				}
			}
		}
		
		return true;
	}
}
