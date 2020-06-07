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
import sm.world.types.BoxBounds;
import sm.world.types.Part;
import sm.world.types.PartBounds;
import sm.world.types.Renderable.Lod;
import sm.world.types.Renderable.MeshMap;

public class PartMesh {
	private final double minViewDistance;
	private final int minViewSize;
	private final PartShader shader;
	private final Part part;
	
	private final List<Texture>[] textures;
	private final Mesh[] meshes;
	private final Mesh[][] animations;
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
		
		if(lod.animationList != null) {
			animations = null;
			/*animations = new Mesh[1][];
			Animation anim = lod.animationList.get(0);
			System.out.println("Loading animation: " + anim.name);
			System.out.println("                 : " + anim.file);
			String animPath = ScrapMechanicAssets.resolvePath(anim.file);
			animations[0] = StaticMeshLoader.load(animPath);
			*/
		} else {
			animations = null;
		}
		
		textures = new List[meshes.length];
		
		//System.out.println("Meshes: " + meshes.length);
		Map<String, MeshMap> maps = lod.subMeshMap;
		for(String name : maps.keySet()) {
			// TODO: Check for '-1'
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
			textures[index] = list;
		}
	}
	
	private boolean loadTextures(MeshMap map, List<Texture> list) throws Exception {
		String material = map.material;
		int index = 0;
		int max = 0;
		
		// TODO: This should be better???
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
			String texturePath = map.textureList.get(0);
			texturePath = ScrapMechanicAssets.resolvePath(texturePath);
			list.add(Texture.loadTexture(texturePath, 0, GL20.GL_LINEAR));
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
	
	private void applyRotation(ChildShape shape, Matrix4f matrix) {
		/*
		// Y
		{ "south": -Y, "east":  X },
		{ "south": -X, "east": -Y },
		{ "south":  Y, "east": -X },
		{ "south":  X, "east":  Y },
		// NegY
		{ "south":  Y, "east":  X },
		{ "south":  X, "east": -Y },
		{ "south": -Y, "east": -X },
		{ "south": -X, "east":  Y },
		// Z
		{ "south":  Z, "east":  X },
		{ "south":  Z, "east": -Y },
		{ "south":  Z, "east": -X },
		{ "south":  Z, "east":  Y },
		// NegZ
		{ "south": -Z, "east":  X },
		{ "south": -Z, "east": -Y },
		{ "south": -Z, "east": -X },
		{ "south": -Z, "east":  Y },
		// X
		{ "south": -Y, "east":  Z },
		{ "south": -X, "east":  Z },
		{ "south":  Y, "east":  Z },
		{ "south":  X, "east":  Z },
		// NegX
		{ "south": -Y, "east": -Z },
		{ "south": -X, "east": -Z },
		{ "south":  Y, "east": -Z },
		{ "south":  X, "east": -Z }
		*/
		
		String[] types = {
		//        001  010  011
			null,"-Z","-Y","-X",null,
		//        101  110  111
			      "X", "Y", "Z"
		};
		
		int NZ = 0b001;
		int NY = 0b010;
		int NX = 0b011;
		int PX = 0b101;
		int PY = 0b110;
		int PZ = 0b111;
		
		int rot = shape.rotation_41_1;
		int south = (rot >> 4) & 0b111;
		int east = rot & 0b111;
		
		float pi = (float)Math.PI;
		float hp = pi / 2.0f;
		
		boolean debug = !shape.uuid.toString().equals("ea4237f4-851a-4751-a1bc-3f85b7488243");
		if(debug) {
			if(south == NY && east == PX) System.out.println("-----------------------------------");
			System.out.printf("%2s, %2s: %8x\n", types[south], types[east], rot);
		}
		
		// Y
		if(south == NY && east == PX) {
			//matrix.rotate(pi / 2.0f,  0, -1,  0).translate(0, 1, 0);
			//matrix.rotate(        0,  1,  0,  0).translate(1, 0, 0);
			/*matrix.translate(0, 1, 1);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getWidth() - 1;
				matrix.translate(0, 0, xx / 2.0f);
			}
			matrix.rotate(-pi / 2.0f, 0, 1, 0);*/
			//matrix.translate(1, 1, 0);
		}
		
		if(south == NX && east == NY) {
			//matrix.translate(1, 0, 0).rotate(hp, 1, 0, 0);
			//matrix.rotate(hp, 0, 1, 0).translate(0, 1, 0);
			matrix.translate(0, 1, 0);
			matrix.rotate(pi, 0, 1, 0);
			//matrix.translate(0, 1, 0);
		}

		if(south == PY && east == NX) {
			matrix.translate(1, 1, 0);
			matrix.rotate(pi / 2.0f, 0, 1, 0);
			//matrix.translate(0, 1, 1);
		}
		
		if(south == PX && east == PY) {
			matrix.translate(1, 1, 1);
			matrix.rotate(0, 0, 1, 0);
		}
		
		
		// NegY
		if(south == PY && east == PX) {
			matrix.translate(1, 0, 1);
			matrix.rotate(pi, 1, 0, 0);
			matrix.rotate(pi / 2.0f, 0, 1, 0);
			//matrix.translate(1, 0, 1);
		}
		
		if(south == PX && east == NY) {
			matrix.translate(0, 0, 1);
			matrix.rotate(pi, 1, 0, 0);
			matrix.rotate(pi, 0, 1, 0);
			//matrix.translate(0, 0, 1);
		}
		
		if(south == NY && east == NX) {
			matrix.translate(0, 0, 0);
			matrix.rotate(pi, 1, 0, 0);
			matrix.rotate(-pi / 2.0f, 0, 1, 0);
			//matrix.translate(0, 0, 0);
		}
		
		if(south == NX && east == PY) {
			matrix.translate(1, 0, 0);
			matrix.rotate(pi, 1, 0, 0);
			matrix.rotate(0, 0, 1, 0);
			//matrix.translate(1, 0, 0);
		}

		// Z
		if(south == PZ && east == PX) {
			matrix.translate(1, 1, 1);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getDepth() - 1;
				float bb = bounds.getWidth() - 1;
				float cc = bounds.getHeight() - 1;
				matrix.translate(cc / 2.0f, xx / 2.0f, bb / 2.0f);
			}
			matrix.rotate(-pi / 2.0f, 1, 0, 0);
			matrix.rotate(-pi / 2.0f, 0, 0, 1);
			//matrix.translate(1, 1, 1);
		}
		
		if(south == PZ && east == NY) {
			matrix.translate(0, 1, 1);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getWidth() - 1;
				float bb = bounds.getDepth() - 1;
				float cc = bounds.getHeight() - 1;
				matrix.translate(-xx / 2.0f, bb / 2.0f, cc / 2.0f);
			}
			matrix.rotate(-pi / 2.0f, 1, 0, 0);
			matrix.rotate(pi, 0, 0, 1);
			//matrix.translate(0, 1, 1);
		}
		
		if(south == PZ && east == NX) {
			matrix.translate(0, 1, 0);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getWidth() - 1;
				float bb = bounds.getDepth() - 1;
				float cc = bounds.getHeight() - 1;
				matrix.translate(-cc / 2.0f, bb / 2.0f, -xx / 2.0f);
			}
			
			matrix.rotate(-pi / 2.0f, 1, 0, 0);
			matrix.rotate(pi / 2.0f, 0, 0, 1);
			//matrix.translate(0, 0, 1);
		}
		
		if(south == PZ && east == PY) {
			matrix.translate(1, 1, 0);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getWidth() - 1;
				float bb = bounds.getDepth() - 1;
				float cc = bounds.getHeight() - 1;
				matrix.translate(xx / 2.0f, bb / 2.0f, -cc / 2.0f);
			}
			matrix.rotate(-pi / 2.0f, 1, 0, 0);
			matrix.rotate(0, 0, 0, 1);
			//matrix.translate(1, 0, 1);
		}
		
		// NegZ
		if(south == NZ && east == PX) {
			matrix.translate(0, 0, 1);
			matrix.rotate(pi / 2.0f, 1, 0, 0);
			matrix.rotate(pi / 2.0f, 0, 0, 1);
			//matrix.translate(1, 0, 0);
		}
		
		if(south == NZ && east == NY) {
			matrix.translate(0, 0, 0);
			matrix.rotate(pi / 2.0f, 1, 0, 0);
			matrix.rotate(pi, 0, 0, 1);
			//matrix.translate(0, 0, 0);
		}
		
		if(south == NZ && east == NX) {
			matrix.translate(1, 0, 0);
			matrix.rotate(pi / 2.0f, 1, 0, 0);
			matrix.rotate(-pi / 2.0f, 0, 0, 1);
			//matrix.translate(0, 1, 0);
		}
		
		if(south == NZ && east == PY) {
			matrix.translate(1, 0, 1);
			matrix.rotate(pi / 2.0f, 1, 0, 0);
			matrix.rotate(0, 0, 0, 1);
			//matrix.translate(1, 1, 0);
		}
		
		
		// X
		if(south == NY && east == PZ) {
			matrix.translate(0, 1, 0);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getWidth() - 1;
				float zz = bounds.getDepth() - 1;
				matrix.translate(-zz / 2.0f, xx / 2.0f, 0);
			}
			
			matrix.rotate(pi / 2.0f, 0, 0, 1);
			matrix.rotate(-pi / 2.0f, 1, 0, 0);
			//matrix.translate(1, 0, 0);
		}
		
		if(south == NX && east == PZ) {
			matrix.translate(1, 1, 0);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getWidth() - 1;
				float zz = bounds.getDepth() - 1;
				float bb = bounds.getHeight() - 1;
				matrix.translate(xx / 2.0f, bb / 2.0f, -zz / 2.0f);
			}
			
			matrix.rotate(pi / 2.0f, 0, 0, 1);
			matrix.rotate(pi, 1, 0, 0);
			//matrix.translate(1, 1, 0);
		}
		
		if(south == PY && east == PZ) {
			matrix.translate(1, 1, 1);
			if(part.getBounds() != null) {
				PartBounds bounds = part.getBounds();
				//System.out.println(bounds.getWidth());
				
				float xx = bounds.getWidth() - 1;
				float zz = bounds.getDepth() - 1;
				float bb = bounds.getHeight() - 1;
				matrix.translate(zz / 2.0f, xx / 2.0f, bb / 2.0f);
			}
			matrix.rotate(pi / 2.0f, 0, 0, 1);
			matrix.rotate(pi / 2.0f, 1, 0, 0);
			//matrix.translate(1, 1, 1);
		}
		
		if(south == PX && east == PZ) {
			matrix.translate(0, 1, 1);
			matrix.rotate(pi / 2.0f, 0, 0, 1);
			matrix.rotate(0, 1, 0, 0);
			//matrix.translate(1, 0, 1);
		}
		
		
		// NegX
		if(south == NY && east == NZ) {
			matrix.translate(0, 0, 1);
			matrix.rotate(-pi / 2.0f, 0, 0, 1);
			matrix.rotate(pi / 2.0f, 1, 0, 0);
			//matrix.translate(0, 1, 0);
		}
		
		if(south == NX && east == NZ) {
			matrix.translate(0, 0, 0);
			matrix.rotate(-pi / 2.0f, 0, 0, 1);
			matrix.rotate(pi, 1, 0, 0);
			//matrix.translate(0, 0, 0);
		}
		
		if(south == PY && east == NZ) {
			matrix.translate(1, 0, 0);
			matrix.rotate(-pi / 2.0f, 0, 0, 1);
			matrix.rotate(-pi / 2.0f, 1, 0, 0);
			//matrix.translate(0, 0, 1);
		}
		
		if(south == PX && east == NZ) {
			matrix.translate(1, 0, 1);
			matrix.rotate(-pi / 2.0f, 0, 0, 1);
			matrix.rotate(0, 1, 0, 0);
			//matrix.translate(0, 1, 1);
		}
	}
	
	// TODO: Bounding boxes load from json
	public boolean render(ChildShape shape) {
		// TODO: Bind texture
		float x = shape.xPos - 0.5f;
		float y = shape.yPos - 0.5f;
		float z = shape.zPos - 0.5f;
		
		Matrix4f matrix = new Matrix4f().translate(x, y, z);
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
		
		// "d4784875-1ede-4d00-a432-f390f0d8fc73"
		
		// TODO: Implement Lod objects
		
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
		}
		
		
		// TODO: If size is smaller than minViewSize return 'false'
		// TODO: If distance is greater than maxViewDistance return 'false'
		return true;
	}
}
