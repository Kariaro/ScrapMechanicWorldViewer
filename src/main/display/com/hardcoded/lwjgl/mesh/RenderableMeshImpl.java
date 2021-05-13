package com.hardcoded.lwjgl.mesh;

import java.util.List;

import org.lwjgl.opengl.GL20;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.SMMaterial;
import com.hardcoded.db.types.Renderable.Lod;
import com.hardcoded.db.types.Renderable.MeshMap;
import com.hardcoded.lwjgl.data.MeshMaterial;
import com.hardcoded.lwjgl.data.Texture;
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
	
	protected Mesh[] meshes = new Mesh[0];
	//protected Mesh[][] animations; // TODO: Animations
	
	@SuppressWarnings("unchecked")
	protected List<Texture>[] textures = new List[0];
	protected MeshMaterial[] mats = new MeshMaterial[0];
	
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
	}
	
	/**
	 * TODO: Add glass transparency.
	 * TODO: Add alpha transparency.
	 * TODO: Add animations.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public MeshMaterial loadTextures(MeshMap map, List<Texture> list) throws Exception {
		MeshMaterial meshMat = new MeshMaterial();
		
		SMMaterial sm_mat = ScrapMechanicAssetHandler.getHLSLMaterial(map.material);
		//System.out.println("sm_mat:");
		//System.out.printf("    : name='%s'\n", sm_mat.name);
		//System.out.printf("    : defines=%s\n", sm_mat.defines);
		//System.out.printf("    : flags  =%s\n", sm_mat.flags);
		meshMat.sm = sm_mat;
		meshMat.map = map;
		
		// Load the textures
		{
			for(int i = 0; i < map.textureList.size(); i++) {
				String texturePath = ScrapMechanicAssetHandler.resolvePath(map.textureList.get(i));
				list.add(Texture.loadTexture(texturePath, i, GL20.GL_LINEAR));
			}
		}
		
//		int index = 0;
//		int last_size = 0;
//		while(true) {
//			int size = material.length();
//			if(last_size == size) {
//				
//				if(size != 0) {
//					System.out.printf("Unprocessed material type(s): <%s>\n", material);
//				}
//				break;
//			} else {
//				last_size = size;
//			}
//			
//			if(material.startsWith("Anim")) {
//				material = material.substring(4);
//				continue;
//			}
//			
//			if(material.startsWith("UV")) {
//				material = material.substring(2);
//				continue;
//			}
//			
//			if(material.startsWith("Pose")) {
//				material = material.substring(4);
//				continue;
//			}
//			
//			if(material.startsWith("2Pose")) {
//				material = material.substring(5);
//				continue;
//			}
//			
//			if(material.startsWith("Skel")) {
//				material = material.substring(4);
//				continue;
//			}
//			
//			if(material.startsWith("Trunk")) {
//				material = material.substring(5);
//				continue;
//			}
//			
//			if(material.startsWith("Glass")) {
//				material = material.substring(5);
//				meshMat.alpha = true;
//				
//				if(index == 0) {
//					String texturePath;
//					
//					texturePath = map.textureList.get(index++);
//					texturePath = ScrapMechanicAssetHandler.resolvePath(texturePath);
//					list.add(Texture.loadTexture(texturePath, 0, GL20.GL_LINEAR));
//					
//					texturePath = map.textureList.get(index++);
//					texturePath = ScrapMechanicAssetHandler.resolvePath(texturePath);
//					list.add(Texture.loadTexture(texturePath, 1, GL20.GL_LINEAR));
//					
//					texturePath = map.textureList.get(index++);
//					texturePath = ScrapMechanicAssetHandler.resolvePath(texturePath);
//					list.add(Texture.loadTexture(texturePath, 2, GL20.GL_LINEAR));
//				}
//				
//				continue;
//			}
//			
//			if(material.startsWith("Alpha")) {
//				material = material.substring(5);
//				meshMat.alpha = true;
//				continue;
//			}
//			
//			if(material.startsWith("Flat")) {
//				material = material.substring(4);
//				continue;
//			}
//			
//			if(material.startsWith("LightCone")) {
//				material = material.substring(9);
//				continue;
//			}
//			
//			if(material.startsWith("LightFlare")) {
//				material = material.substring(10);
//				continue;
//			}
//			
//			if(material.startsWith("Flip")) {
//				material = material.substring(4);
//				meshMat.flip = true;
//				continue;
//			}
//			
//			if(material.startsWith("Leaves")) {
//				material = material.substring(6);
//				meshMat.alpha = true;
//				meshMat.flip = true;
//				continue;
//			}
//			
//			if(material.startsWith("Dif")) {
//				material = material.substring(3);
//				String texturePath = ScrapMechanicAssetHandler.resolvePath(map.textureList.get(index++));
//				list.add(Texture.loadTexture(texturePath, 0, GL20.GL_LINEAR));
//				continue;
//			}
//			
//			if(material.startsWith("Asg")) {
//				material = material.substring(3);
//				String texturePath = ScrapMechanicAssetHandler.resolvePath(map.textureList.get(index++));
//				list.add(Texture.loadTexture(texturePath, 1, GL20.GL_LINEAR));
//				continue;
//			}
//			
//			if(material.startsWith("Nor")) {
//				material = material.substring(3);
//				String texturePath = ScrapMechanicAssetHandler.resolvePath(map.textureList.get(index++));
//				list.add(Texture.loadTexture(texturePath, 2, GL20.GL_LINEAR));
//				continue;
//			}
//			
//			if(material.startsWith("Ao")) {
//				material = material.substring(2);
//				String texturePath = ScrapMechanicAssetHandler.resolvePath(map.textureList.get(index++));
//				list.add(Texture.loadTexture(texturePath, 3, GL20.GL_LINEAR));
//				continue;
//			}
//		}
//		
//		if(!material.isEmpty()) {
//		}
//		
//		// If there is textures and nothing was added. Just load the Dif texture
//		if(index == 0 && map.textureList.size() > 0) {
//			for(int i = 0; i < map.textureList.size(); i++) {
//				String texturePath = map.textureList.get(i);
//				texturePath = ScrapMechanicAssetHandler.resolvePath(texturePath);
//				list.add(Texture.loadTexture(texturePath, i, GL20.GL_LINEAR));
//			}
//		}
		
		return meshMat;
	}
	
	@Override
	public Mesh getMesh(String name) {
		if(lod.isMeshList()) {
			int index = Integer.valueOf(name);
			if(index < 0 || index >= meshes.length) return null;
			return meshes[index];
		} else {
			for(Mesh mesh : meshes) {
				if(name.equals(mesh.getName())) return mesh;
			}
			return null;
		}
	}
	
	@Override
	public int getMeshIndex(String name) {
		if(lod.isMeshList()) {
			int index = Integer.valueOf(name);
			if(index < 0 || index >= meshes.length) return -1;
			return index;
		} else {
			for(int i = 0; i < meshes.length; i++) {
				Mesh mesh = meshes[i];
				
				if(name.equals(mesh.getName())) return i;
			}
		}
		return -1;
	}

}
