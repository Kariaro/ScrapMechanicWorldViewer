package com.hardcoded.lwjgl.mesh;

import java.util.List;
import java.util.Map;

import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.SMAsset;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.shader.AssetShader;
import com.hardcoded.tile.object.Asset;

/**
 * @author HardCoded
 * @since v0.1
 */
public class AssetMesh extends RenderableMeshImpl {
	public final AssetShader shader;
	public final SMAsset asset;
	
	private final Map<String, int[]> defaultColors;
	
	public AssetMesh(Lod lod, AssetShader shader, SMAsset asset, Map<String, int[]> defaultColors) throws Exception {
		super(lod);
		this.asset = asset;
		this.shader = shader;
		this.defaultColors = defaultColors;
	}
	
	public void bindColor(Asset asset, MeshMaterial mat) {
		float r = 1;
		float g = 1;
		float b = 1;
		float a = 1;
		
		// FIXME: Mat will never be null, remove this
		if(mat == null) {
			shader.setColor(r, g, b, a);
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
					int color = array[0];//index % array.length];
					r = ((color >> 24) & 0xff) / 255.0f;
					g = ((color >> 16) & 0xff) / 255.0f;
					b = ((color >>  8) & 0xff) / 255.0f;
					a = ((color >>  0) & 0xff) / 255.0f;
				}
			}
		}
		
		shader.setColor(r, g, b, a);
	}
	
	public int getColor(Asset asset, int index) {
		MeshMaterial mat = mats[index];
		if(mat == null) {
			return 0xffffff;
		}
		
		Map<String, Integer> map = asset.getMaterials();
		if(map.containsKey(mat.key)) {
			return map.get(mat.key);
		} else if(mat.map != null) {
			Map<String, Object> custom = mat.map.custom;
			if(custom != null && custom.containsKey("color")) {
				String value = (String)custom.get("color");
				
				if(defaultColors.containsKey(value)) {
					int[] array = defaultColors.get(value);
					
					//int index = (int)(System.currentTimeMillis() % 100000L) / 1000;
					if(index >= array.length) {
						return array[0];
					} else {
						return array[index];
					}
				}
			}
		}
		
		return 0xff00ffff;
	}
	
	public void render(Asset asset) {
		if(!isLoaded) return;
		
		for(int i = 0; i < meshes.length; i++) {
			List<Texture> texs = textures[i];
			MeshMaterial mat = mats[i];
			
			bindColor(asset, mat);
			for(Texture t : texs) t.bind();
			mat.bind(shader);
			meshes[i].render();
			mat.unbind(shader);
			for(Texture t : texs) t.unbind();
		}
	}
}
