package me.hardcoded.smviewer.lwjgl.mesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.hardcoded.smviewer.lwjgl.async.LwjglAsyncThread;
import me.hardcoded.smviewer.lwjgl.util.RenderException;
import me.hardcoded.smviewer.lwjgl.util.StaticMeshLoaderAsync;
import org.lwjgl.opengl.GL20;

import me.hardcoded.smviewer.asset.ScrapMechanicAssetHandler;
import me.hardcoded.smviewer.db.types.Renderable.Lod;
import me.hardcoded.smviewer.db.types.Renderable.MeshMap;
import me.hardcoded.smviewer.db.types.SMMaterial.Types;
import me.hardcoded.smviewer.db.types.SMMaterial;
import me.hardcoded.smviewer.lwjgl.LwjglWindowSetup;
import me.hardcoded.smviewer.lwjgl.data.MeshMaterial;
import me.hardcoded.smviewer.lwjgl.data.Texture;
import me.hardcoded.smviewer.util.ValueUtils;

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
	
	protected boolean isLoaded;
	public Mesh[] meshes;
	public List<Texture>[] textures;
	public MeshMaterial[] mats;
	
	public Texture DIF_TEX = Texture.NONE;
	public Texture ASG_TEX = Texture.NONE;
	public Texture NOR_TEX = Texture.NONE;
	public Texture AO_TEX = Texture.NONE;
	
	protected RenderableMeshImpl(Lod lod) {
		this.lod = lod;
		this.maxViewDistance = ValueUtils.toDouble(lod.maxViewDistance);
		this.minViewSize = ValueUtils.toDouble(lod.minViewSize);
		
		if (!LwjglAsyncThread.isCurrentThread()) {
			LwjglAsyncThread.runAsync(this::initialize);
			return;
		}
		
		initialize();
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}
	
	/**
	 * Initialize this mesh
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		String path = ScrapMechanicAssetHandler.resolvePath(lod.mesh);
		StaticMeshLoaderAsync.AsyncMesh[] loaded = new StaticMeshLoaderAsync.AsyncMesh[0];
		
		try {
			loaded = StaticMeshLoaderAsync.load(path);
		} catch (Exception e) {
			throw new RenderException("Failed to load mesh model '" + path + "'");
		}
		
		final StaticMeshLoaderAsync.AsyncMesh[] async_meshes = loaded;
		final int size = async_meshes.length;
		
		this.meshes = new Mesh[size];
		this.textures = new List[size];
		this.mats = new MeshMaterial[size];
		for (int i = 0; i < size; i++) {
			this.textures[i] = new ArrayList<>();
			this.mats[i] = new MeshMaterial("", null);
		}
		
		LwjglWindowSetup.runLater(() -> {
			Map<String, MeshMap> maps = lod.subMeshMap;
			for (int i = 0; i < size; i++) {
				Mesh mesh = new Mesh(async_meshes[i]);
				this.meshes[i] = mesh;
				
				String name = mesh.getName();
				MeshMap map = maps.get(name);
				if (map == null) {
					map = maps.get(Integer.toString(i));
					if (map == null) {
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
		final int len = material.map.textureList.size();
		for (int i = 0; i < len; i++) {
			String texturePath = ScrapMechanicAssetHandler.resolvePath(material.map.textureList.get(i));
			
			try {
				textures.add(Texture.loadTexture(texturePath, i, GL20.GL_LINEAR));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
//		setUniform("dif_tex", 0);
//		setUniform("asg_tex", 1);
//		setUniform("nor_tex", 2);
//		setUniform("ao_tex", 3);
		
		int index = 0;
		if (index <= len) DIF_TEX = textures.get(index++);
		if (index <= index && sm_mat.hasDefined(Types.ASG_TEX)) ASG_TEX = textures.get(index++);
		if (index <= index && sm_mat.hasDefined(Types.NOR_TEX)) NOR_TEX = textures.get(index++);
		if (index <= index && sm_mat.hasDefined(Types.AO_TEX)) AO_TEX = textures.get(index++);
		
		return textures;
	}
	
	// TODO: Make this the universal call function
	public void render() {
		
	}
	
	@Override
	public void renderShadows() {
		if (!isLoaded) return;
		
		for (int i = 0; i < meshes.length; i++) {
			meshes[i].render();
		}
	}
}
