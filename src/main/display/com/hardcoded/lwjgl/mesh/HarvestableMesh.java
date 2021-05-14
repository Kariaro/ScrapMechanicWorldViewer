package com.hardcoded.lwjgl.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.db.types.SMHarvestable;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.Shader;
import com.hardcoded.lwjgl.util.StaticMeshLoader;
import com.hardcoded.tile.object.Harvestable;

/**
 * A harvestable mesh.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class HarvestableMesh extends RenderableMeshImpl {
	public final Shader shader;
	public final SMHarvestable harvestable;
	
	private final List<Texture>[] textures;
	private final MeshMaterial[] mats;
	
	@SuppressWarnings("unchecked")
	public HarvestableMesh(Lod lod, Shader shader, SMHarvestable harvestable) throws Exception {
		super(lod);
		this.harvestable = harvestable;
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
			} else {
				meshMat.key = name;
			}
			
			// TODO: Fallback option?
			if(index < 0) index = 0;
			mats[index] = meshMat;
			textures[index] = list;
		}
		System.out.println("--------------------");
	}
	
	public boolean render(Harvestable harvestable, Vector3f pos, Quaternionf quat, Vector3f scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(pos);
		matrix.rotate(quat);
		matrix.scale(scale);
		
		shader.setUniform("transformationMatrix", matrix);
		{
			int color = this.harvestable.color;
			float r = ((color >> 24) & 0xff) / 255.0f;
			float g = ((color >> 16) & 0xff) / 255.0f;
			float b = ((color >>  8) & 0xff) / 255.0f;
			float a = ((color >>  0) & 0xff) / 255.0f;
			shader.setUniform("color", r, g, b, a);
		}
		
		for(int i = 0; i < meshes.length; i++) {
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
