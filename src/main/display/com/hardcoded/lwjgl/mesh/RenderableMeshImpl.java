package com.hardcoded.lwjgl.mesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL20;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.db.types.SMMaterial;
import com.hardcoded.lwjgl.LwjglWorldViewer;
import com.hardcoded.lwjgl.async.LwjglAsyncThread;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
import com.hardcoded.lwjgl.util.RenderException;
import com.hardcoded.lwjgl.util.StaticMeshLoaderAsync;
import com.hardcoded.lwjgl.util.StaticMeshLoaderAsync.AsyncMesh;
import com.hardcoded.util.ValueUtils;

/**
 * An abstract implementation of a renderable mesh.
 * 
 * @author HardCoded
 * @since v0.2
 * 
 * TODO: Animations
 */
public abstract class RenderableMeshImpl implements RenderableMesh {
	protected final Lod lod;
	public final double maxViewDistance;
	public final double minViewSize;
	
	protected boolean isLoaded;
	//protected Mesh[][] animations;
	protected Mesh[] meshes;
	protected List<Texture>[] textures;
	protected MeshMaterial[] mats;
	
	protected RenderableMeshImpl(Lod lod) {
		this.lod = lod;
		this.maxViewDistance = ValueUtils.toDouble(lod.maxViewDistance);
		this.minViewSize = ValueUtils.toDouble(lod.minViewSize);
		
		if(!LwjglAsyncThread.isCurrentThread()) {
			LwjglAsyncThread.runAsync(this::initialize);
			return;
		}
		
		initialize();
	}
	
	/**
	 * Initialize this mesh
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
//		if(lod.animationList != null) {
//			//animations = null;
//			/*animations = new Mesh[1][];
//			Animation anim = lod.animationList.get(0);
//			System.out.println("Loading animation: " + anim.name);
//			System.out.println("                 : " + anim.file);
//			String animPath = ScrapMechanicAssets.resolvePath(anim.file);
//			animations[0] = StaticMeshLoader.load(animPath);
//			*/
//		} else {
//			//animations = null;
//		}
		
		String path = ScrapMechanicAssetHandler.resolvePath(lod.mesh);
		AsyncMesh[] loaded = new AsyncMesh[0];
		
		try {
			loaded = StaticMeshLoaderAsync.load(path);
		} catch(Exception e) {
			throw new RenderException("Failed to load mesh model '" + path + "'");
		}
		
		final AsyncMesh[] async_meshes = loaded;
		final int size = async_meshes.length;
		
		this.meshes = new Mesh[size];
		this.textures = new List[size];
		this.mats = new MeshMaterial[size];
		for(int i = 0; i < size; i++) {
			this.textures[i] = new ArrayList<>();
			this.mats[i] = new MeshMaterial("", null);
		}
		
		LwjglWorldViewer.runLater(() -> {
			Map<String, MeshMap> maps = lod.subMeshMap;
			for(int i = 0; i < size; i++) {
				Mesh mesh = new Mesh(async_meshes[i]);
				this.meshes[i] = mesh;
				
				String name = mesh.getName();
				MeshMap map = maps.get(name);
				if(map == null) {
					map = maps.get(Integer.toString(i));
					if(map == null) {
						System.err.printf("Failed to find mesh map of '%s'\n", name);
						// throw new RenderException("Failed to find mesh map of '" + name + "'");
						continue;
					}
				}
				
				MeshMaterial material = new MeshMaterial(name, map);
				List<Texture> list = loadTextures(material);
				
				mats[i] = material;
				textures[i] = list;
			}
			
			// Make sure we mark this model as loaded
			isLoaded = true;
		});
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
	
	// TODO: Make this the universal call function
	public void render() {
		
	}
	
	@Override
	public void renderShadows() {
		if(!isLoaded) return;
		
		for(int i = 0; i < meshes.length; i++) {
			meshes[i].render();
		}
	}
}
