package me.hardcoded.smviewer.lwjgl.cache;

import java.util.*;

import me.hardcoded.smviewer.db.types.Renderable;
import me.hardcoded.smviewer.db.types.Renderable.Lod;
import me.hardcoded.smviewer.db.types.SMAsset;
import me.hardcoded.smviewer.lwjgl.mesh.AssetMesh;
import me.hardcoded.smviewer.lwjgl.shader.AssetShader;

/**
 * An asset cache.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldAssetCache implements WorldObjectCache {
	public final List<AssetMesh> meshes;
	
	public final Map<String, int[]> defaultColors;
	
	public WorldAssetCache(SMAsset asset, AssetShader shader) {
		this.defaultColors = new HashMap<>();
		this.meshes = new ArrayList<>();
		
		if (asset.defaultColors != null) {
			Map<String, List<String>> def = asset.defaultColors;
			
			for (String key : def.keySet()) {
				List<String> list = def.get(key);
				
				// Skip bad values
				if (list == null || list.size() == 0) continue;
				
				int[] array = new int[list.size()];
				defaultColors.put(key, array);
				
				for (int i = 0; i < list.size(); i++) {
					String color = list.get(i);
					
					try {
						int value = Integer.parseInt(color, 16);
						
						if (color.length() == 6) {
							array[i] = (value << 8) | 0xff;
						} else {
							array[i] = value;
						}
					} catch (NumberFormatException e) {
						// Do nothing
					}
				}
			}
		}
		
		try {
			Renderable rend = asset.renderable;
			for (Lod lod : rend.lodList) {
				AssetMesh mesh = new AssetMesh(lod, shader, asset, defaultColors);
				meshes.add(mesh);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
