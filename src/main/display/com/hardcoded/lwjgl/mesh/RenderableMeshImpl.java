package com.hardcoded.lwjgl.mesh;

import java.io.IOException;
import java.util.*;

import org.lwjgl.opengl.GL20;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.db.types.SMMaterial;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.util.RenderException;
import com.hardcoded.lwjgl.util.StaticMeshLoader;
import com.hardcoded.util.ValueUtils;

/**
 * An abstract implementation of a renderable mesh.
 * 
 * @author HardCoded
 * @since v0.2
 */
public abstract class RenderableMeshImpl implements RenderableMesh {
	protected final Lod lod;
	public final double maxViewDistance;
	public final double minViewSize;
	
	protected final Mesh[] meshes;
	protected final List<Texture>[] textures;
	protected final MeshMaterial[] mats;
	//protected Mesh[][] animations; // TODO: Animations
	
	@SuppressWarnings("unchecked")
	protected RenderableMeshImpl(Lod lod) {
		this.lod = lod;
		
		this.maxViewDistance = ValueUtils.toDouble(lod.maxViewDistance);
		this.minViewSize = ValueUtils.toDouble(lod.minViewSize);
		
		if(lod.animationList != null) {
			//animations = null;
			/*animations = new Mesh[1][];
			Animation anim = lod.animationList.get(0);
			System.out.println("Loading animation: " + anim.name);
			System.out.println("                 : " + anim.file);
			String animPath = ScrapMechanicAssets.resolvePath(anim.file);
			animations[0] = StaticMeshLoader.load(animPath);
			*/
		} else {
			//animations = null;
		}
		
		String path = ScrapMechanicAssetHandler.resolvePath(lod.mesh);
		try {
			this.meshes = StaticMeshLoader.load(path);
		} catch(Exception e) {
			throw new RenderException("Failed to load mesh model '" + path + "'");
		}
		
		this.textures = new List[meshes.length];
		this.mats = new MeshMaterial[meshes.length];
		
		for(int i = 0; i < meshes.length; i++) {
			this.textures[i] = new ArrayList<>();
			this.mats[i] = new MeshMaterial("", null);
		}
		
		Map<String, MeshMap> maps = lod.subMeshMap;
		for(int i = 0; i < meshes.length; i++) {
			Mesh mesh = meshes[i];
			
			String name = mesh.getName();
			MeshMap map = maps.get(name);
			if(map == null) {
				map = maps.get(Integer.toString(i));
				if(map == null) {
					throw new RenderException("Failed to find mesh map of '" + name + "'");
				}
			}
			
			MeshMaterial material = new MeshMaterial(name, map);
			List<Texture> list = loadTextures(material);
			
			mats[i] = material;
			textures[i] = list;
		}
	}
	
	/**
	 * TODO: Add glass transparency.
	 * TODO: Add alpha transparency.
	 * TODO: Add animations.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public List<Texture> loadTextures(MeshMaterial material) {
		List<Texture> textures = new ArrayList<>();
		
		SMMaterial sm_mat = ScrapMechanicAssetHandler.getHLSLMaterial(material.map.material);
		material.sm = sm_mat;
		
		// Load the textures
		int len = material.map.textureList.size();
		for(int i = 0; i < len; i++) {
			String texturePath = ScrapMechanicAssetHandler.resolvePath(material.map.textureList.get(i));
			
			try {
				textures.add(Texture.loadTexture(texturePath, i, GL20.GL_LINEAR));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		return textures;
	}
	
	@Override
	public void renderShadows() {
		for(int i = 0; i < meshes.length; i++) {
			meshes[i].render();
		}
	}
}
