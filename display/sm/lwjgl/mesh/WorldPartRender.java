package sm.lwjgl.mesh;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import sm.lwjgl.shader.PartShader;
import sm.lwjgl.util.StaticMeshLoader;
import sm.objects.BodyList.ChildShape;
import sm.world.World;
import sm.world.types.Part;
import sm.world.types.Renderable;
import sm.world.types.Renderable.Lod;

public class WorldPartRender {
	private final Part part;
	private final PartShader shader;
	private final List<PartMesh> meshes;
	
	// TODO: Load models and draw them
	// AIScene mesh = aiImportFileEx();
	public WorldPartRender(Part part, PartShader shader) {
		this.part = part;
		this.shader = shader;
		meshes = new ArrayList<>();
		
		try {
			Renderable rend = part.renderable;
			for(Lod lod : rend.lodList) {
				meshes.add(new PartMesh(lod, shader, part));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(ChildShape shape) {
		/*
		// TODO: Bind texture
		float x = shape.yPos_33_2;
		float y = shape.zPos_35_2;
		float z = shape.xPos_31_2;
		
		shader.setUniform("transformationMatrix", new Matrix4f().translate(x, y, z));
		
		// TODO: Implement Lod objects
		*/
		

		for(PartMesh mesh : meshes) {
			mesh.render(shape);
		}
	}
}
