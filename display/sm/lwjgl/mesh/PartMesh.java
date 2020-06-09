package sm.lwjgl.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import sm.asset.ScrapMechanicAssets;
import sm.lwjgl.shader.PartShader;
import sm.lwjgl.util.StaticMeshLoader;
import sm.objects.BodyList.ChildShape;
import sm.objects.BodyList.RigidBody;
import sm.world.types.Part;
import sm.world.types.PartBounds;
import sm.world.types.PartRotation;
import sm.world.types.Renderable.Lod;
import sm.world.types.Renderable.MeshMap;
import sm.world.types.ShapeUtils.Bounds3D;

public class PartMesh {
	private final double minViewDistance;
	private final int minViewSize;
	private final PartShader shader;
	private final Part part;
	
	private final List<Texture>[] textures;
	private final Mesh[] meshes;
	//private final Mesh[][] animations;
	private final Lod lod;
	
	@SuppressWarnings("unchecked")
	public PartMesh(Lod lod, PartShader shader, Part part) throws Exception {
		minViewSize = lod.minViewSize;
		minViewDistance = lod.maxViewDistance;
		
		this.lod = lod;
		this.part = part;
		this.shader = shader;
		
		String path = ScrapMechanicAssets.resolvePath(lod.mesh);
		meshes = StaticMeshLoader.load(path);
		
		// TODO: Animations
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
		
		textures = new List[meshes.length];
		
		//System.out.println("Meshes: " + meshes.length);
		Map<String, MeshMap> maps = lod.subMeshMap;
		for(String name : maps.keySet()) {
			int index = getMeshIndex(name);
			
			List<Texture> list = new ArrayList<>();
			boolean loadedFully = loadTextures(maps.get(name), list);
			if(!loadedFully) {
				System.out.println("PartMesh has no texture!!!");
				System.out.printf("    SubMesh \"%s\" -> %s\n", name, list);
				MeshMap meshMap = maps.get(name);
				System.out.println("    TextureList: " + meshMap.textureList);
				System.out.println("    Material: " + meshMap.material);
				
			}
			
			// TODO: Fallback option?
			if(index < 0) index = 0;
			textures[index] = list;
		}
		System.out.println("--------------------");
	}
	
	private boolean loadTextures(MeshMap map, List<Texture> list) throws Exception {
		String material = map.material;
		int index = 0;
		int max = 0;
		
		// TODO: Code this a little better. Use more functions!
		while(!material.isEmpty() && (max++ < 100)) {
			if(material.startsWith("UVAnim")) {
				material = material.substring(6);
				continue;
			}
			
			if(material.startsWith("PoseAnim")) {
				material = material.substring(8);
				continue;
			}
			
			if(material.startsWith("2PoseAnim")) {
				material = material.substring(9);
				continue;
			}
			
			if(material.startsWith("PoseUVAnim")) {
				material = material.substring(10);
				continue;
			}
			
			if(material.startsWith("SkelAnim")) {
				material = material.substring(8);
				continue;
			}
			
			if(material.startsWith("Glass")) {
				material = material.substring(5);
				// TODO: Transparent
				
				if(index == 0) {
					String texturePath;
					
					texturePath = map.textureList.get(index++);
					texturePath = ScrapMechanicAssets.resolvePath(texturePath);
					list.add(Texture.loadTexture(texturePath, 0, GL20.GL_LINEAR));
					
					texturePath = map.textureList.get(index++);
					texturePath = ScrapMechanicAssets.resolvePath(texturePath);
					list.add(Texture.loadTexture(texturePath, 1, GL20.GL_LINEAR));
					
					texturePath = map.textureList.get(index++);
					texturePath = ScrapMechanicAssets.resolvePath(texturePath);
					list.add(Texture.loadTexture(texturePath, 2, GL20.GL_LINEAR));
				}
				
				continue;
			}
			
			if(material.startsWith("Alpha")) {
				material = material.substring(5);
				// TODO: Implement
				continue;
			}
			
			if(material.startsWith("Flat")) {
				material = material.substring(4);
				// TODO: ?????
				continue;
			}
			
			if(material.startsWith("LightCone")) {
				material = material.substring(9);
				// TODO: ?????
				continue;
			}
			
			if(material.startsWith("Flip")) {
				material = material.substring(4);
				// TODO: No backface culling
				continue;
			}
			
			if(material.startsWith("Dif")) {
				material = material.substring(3);
				String texturePath = map.textureList.get(index++);
				texturePath = ScrapMechanicAssets.resolvePath(texturePath);
				list.add(Texture.loadTexture(texturePath, 0, GL20.GL_LINEAR));
				continue;
			}
			
			if(material.startsWith("Asg")) {
				material = material.substring(3);
				String texturePath = map.textureList.get(index++);
				texturePath = ScrapMechanicAssets.resolvePath(texturePath);
				list.add(Texture.loadTexture(texturePath, 1, GL20.GL_LINEAR));
				continue;
			}
			
			if(material.startsWith("Nor")) {
				material = material.substring(3);
				String texturePath = map.textureList.get(index++);
				texturePath = ScrapMechanicAssets.resolvePath(texturePath);
				list.add(Texture.loadTexture(texturePath, 2, GL20.GL_LINEAR));
				continue;
			}
			
			if(material.startsWith("Ao")) {
				material = material.substring(2);
				String texturePath = map.textureList.get(index++);
				texturePath = ScrapMechanicAssets.resolvePath(texturePath);
				list.add(Texture.loadTexture(texturePath, 3, GL20.GL_LINEAR));
				continue;
			}
		}
		
		if(!material.isEmpty()) {
			System.out.println("NonEmptyMat: " + material);
		}
		
		// If there is textures and nothing was added. Just load the Dif texture
		if(index == 0 && map.textureList.size() > 0) {
			for(int i = 0; i < map.textureList.size(); i++) {
				String texturePath = map.textureList.get(i);
				texturePath = ScrapMechanicAssets.resolvePath(texturePath);
				list.add(Texture.loadTexture(texturePath, i, GL20.GL_LINEAR));
			}
			return false;
		}
		
		return true;
	}
	
	private Mesh getMesh(String name) {
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
	
	private int getMeshIndex(String name) {
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
		
		Matrix4f matrix = new Matrix4f().translate(x, y, z);
		RigidBody body = shape.body;
		/*if(body.isStatic_0_2 == 1) {
			float xm = (bounds.xMin + bounds.xMax) / 2.0f;
			float ym = (bounds.yMin + bounds.yMax) / 2.0f;
			float zm = 0;//(bounds.zMin + bounds.zMax) / 2.0f;
			
			matrix.translate(-xm, -ym, -zm);
			matrix.translate(
				(body.yMin + body.yMax) * 2,
				(body.xMin + body.xMax) * 2,
				0
			);
		} else {
			float xm = (bounds.xMin + bounds.xMax) / 2.0f;
			float ym = (bounds.yMin + bounds.yMax) / 2.0f;
			float zm = (bounds.zMin + bounds.zMax) / 2.0f;
			
			matrix.translate(-xm, -ym, -zm);
			matrix.translate(
				(body.yMin + body.yMax) * 2,
				(body.xMin + body.xMax) * 2,
				0
			);
		}*/

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
			if(texs != null) {
				for(Texture t : texs) t.bind();
			}
			
			meshes[i].render();
			
			if(texs != null) {
				for(Texture t : texs) t.unbind();
			}
		}
		
		
		// TODO: If size is smaller than minViewSize return 'false'
		// TODO: If distance is greater than maxViewDistance return 'false'
		return true;
	}
}
