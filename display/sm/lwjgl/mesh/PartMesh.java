package sm.lwjgl.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

import sm.lwjgl.shader.PartShader;
import sm.lwjgl.util.StaticMeshLoader;
import sm.objects.BodyList.ChildShape;
import sm.world.Renderable.Lod;
import sm.world.Renderable.MeshMap;
import sm.world.World;

public class PartMesh {
	private final double minViewDistance;
	private final int minViewSize;
	private final PartShader shader;
	
	private final List<Texture>[] textures;
	private final Mesh[] meshes;
	private final Lod lod;
	
	@SuppressWarnings("unchecked")
	public PartMesh(Lod lod, PartShader shader) throws Exception {
		minViewSize = lod.minViewSize;
		minViewDistance = lod.maxViewDistance;
		
		this.lod = lod;
		this.shader = shader;
		
		String path = World.getPath(lod.mesh);
		meshes = StaticMeshLoader.load(path);
		textures = new List[meshes.length];
		
		System.out.println("Meshes: " + meshes.length);
		Map<String, MeshMap> maps = lod.subMeshMap;
		for(String name : maps.keySet()) {
			// TODO: Check for '-1'
			int index = getMeshIndex(name);
			
			List<Texture> list = new ArrayList<>();
			loadTextures(maps.get(name), list);
			System.out.printf("SubMesh \"%s\" -> %s\n", name, list);
			textures[index] = list;
		}
	}
	
	private void loadTextures(MeshMap map, List<Texture> list) throws Exception {
		String material = map.material;
		int index = 0;
		int max = 0;
		while(!material.isEmpty() && (max++ < 100)) {
			if(material.startsWith("UVAnim")) {
				material = material.substring(6);
				continue;
			}
			
			if(material.startsWith("PoseAnim")) {
				material = material.substring(8);
				continue;
			}
			
			if(material.startsWith("SkelAnim")) {
				material = material.substring(8);
				continue;
			}
			
			if(material.startsWith("Glass")) {
				material = material.substring(5);
				// TODO: Transparent
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
				texturePath = World.getPath(texturePath);
				list.add(Texture.loadTexture(texturePath, 0, GL20.GL_LINEAR));
				continue;
			}
			
			if(material.startsWith("Asg")) {
				material = material.substring(3);
				String texturePath = map.textureList.get(index++);
				texturePath = World.getPath(texturePath);
				list.add(Texture.loadTexture(texturePath, 1, GL20.GL_LINEAR));
				continue;
			}
			
			if(material.startsWith("Nor")) {
				material = material.substring(3);
				String texturePath = map.textureList.get(index++);
				texturePath = World.getPath(texturePath);
				list.add(Texture.loadTexture(texturePath, 2, GL20.GL_LINEAR));
				continue;
			}
			
			if(material.startsWith("Ao")) {
				material = material.substring(2);
				String texturePath = map.textureList.get(index++);
				texturePath = World.getPath(texturePath);
				list.add(Texture.loadTexture(texturePath, 3, GL20.GL_LINEAR));
				continue;
			}
		}
		
		if(!material.isEmpty()) {
			System.out.println("NonEmptyMat: " + material);
		}
	}
	
	private Mesh getMesh(String name) {
		if(lod.isMeshList()) {
			int index = Integer.valueOf(name);
			if(index < 0 || index >= meshes.length) return null;
			return meshes[index];
		} else {
			for(Mesh mesh : meshes) {
				if(mesh.name.equals(name)) return mesh;
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
				
				if(mesh.name.equals(name)) return i;
			}
		}
		return -1;
	}
	
	
	public boolean render(ChildShape shape) {
		// TODO: Bind texture
		float x = shape.yPos_33_2;
		float y = shape.zPos_35_2;
		float z = shape.xPos_31_2;
		
		shader.setUniform("transformationMatrix", new Matrix4f().translate(x, y, z));
		{
			int rgba = shape.color_abgr_37_4;
			float r, g, b, a;
			{
				a = ((rgba >> 24) & 0xff) / 255.0f;
				r = ((rgba      ) & 0xff) / 255.0f;
				g = ((rgba >>  8) & 0xff) / 255.0f;
				b = ((rgba >> 16) & 0xff) / 255.0f;
			}
			shader.setUniform("color", r, g, b, a);
		}
		
		// TODO: Implement Lod objects
		
		for(int i = 0; i < meshes.length; i++) {
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
		return true;
	}
}
