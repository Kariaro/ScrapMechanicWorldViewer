package com.hardcoded.lwjgl.mesh;

import java.util.*;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.SMAsset;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.lwjgl.util.StaticMeshLoader;
import com.hardcoded.tile.object.Asset;

/**
 * @author HardCoded
 * @since v0.1
 */
public class AssetMesh extends RenderableMeshImpl {
	public final AssetShader shader;
	public final SMAsset asset;
	
	private final List<Texture>[] textures;
	private final MeshMaterial[] mats;
	
	private Map<String, int[]> defaultColors;
	
	@SuppressWarnings("unchecked")
	public AssetMesh(Lod lod, AssetShader shader, SMAsset asset) throws Exception {
		super(lod);
		this.asset = asset;
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
		
		// Color cache
		defaultColors = new HashMap<>();
		
		if(asset.defaultColors != null) {
			Map<String, List<String>> def = asset.defaultColors;
			
			for(String key : def.keySet()) {
				List<String> list = def.get(key);
				
				// Skip bad values
				if(list == null || list.size() == 0) continue;
				
				int[] array = new int[list.size()];
				defaultColors.put(key, array);
				
				for(int i = 0; i < list.size(); i++) {
					String color = list.get(i);
					
					try {
						int value = Integer.parseInt(color, 16);
						
						if(color.length() == 6) {
							array[i] = (value << 8) | 0xff;
						} else {
							array[i] = value;
						}
					} catch(NumberFormatException e) {
						// Do nothing
					}
				}
			}
		}
	}
	
	private void bindColor(Asset asset, MeshMaterial mat) {
		float r = 1;
		float g = 1;
		float b = 1;
		float a = 1;
		
		if(mat == null) {
			shader.setUniform("color", r, g, b, a);
			return;
		}
		
		Map<String, Integer> map = asset.getMaterials();
		if(map.containsKey(mat.key)) {
			int color = map.get(mat.key);
			r = ((color >> 24) & 0xff) / 255.0f;
			g = ((color >> 16) & 0xff) / 255.0f;
			b = ((color >>  8) & 0xff) / 255.0f;
			a = ((color >>  0) & 0xff) / 255.0f;
		} else if(mat.map != null) {
			Map<String, Object> custom = mat.map.custom;
			if(custom != null && custom.containsKey("color")) {
				String value = (String)custom.get("color");
				
				if(defaultColors.containsKey(value)) {
					int[] array = defaultColors.get(value);
					
					//int index = (int)(System.currentTimeMillis() % 100000L) / 1000;
					int color = array[0];
					r = ((color >> 24) & 0xff) / 255.0f;
					g = ((color >> 16) & 0xff) / 255.0f;
					b = ((color >>  8) & 0xff) / 255.0f;
					a = ((color >>  0) & 0xff) / 255.0f;
				}
			}
		}
		
		shader.setUniform("color", r, g, b, a);
	}
	
	public boolean render(Asset asset, Vector3f pos, Quaternionf quat, Vector3f scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(pos);
		matrix.rotate(quat);
		matrix.scale(scale);
		
		shader.setUniform("transformationMatrix", matrix);
		shader.setUniform("color", 1, 1, 1, 1);
		
		for(int i = 0; i < meshes.length; i++) {
			List<Texture> texs = textures[i];
			MeshMaterial mat = mats[i];
			
			bindColor(asset, mat);
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
