package sm.lwjgl.mesh;

import java.util.ArrayList;
import java.util.List;

import sm.lwjgl.shader.PartShader;
import sm.objects.BodyList.ChildShape;
import sm.world.types.Part;
import sm.world.types.Renderable;
import sm.world.types.Renderable.Lod;
import sm.world.types.ShapeUtils.Bounds3D;

// TODO: Load all the parts and unload them when we don't need them..
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
	
	public void render(ChildShape shape, Bounds3D bounds) {
		/*
		// TODO: Bind texture
		float x = shape.yPos_33_2;
		float y = shape.zPos_35_2;
		float z = shape.xPos_31_2;
		
		shader.setUniform("transformationMatrix", new Matrix4f().translate(x, y, z));
		
		// TODO: Implement Lod objects
		*/
		

		for(PartMesh mesh : meshes) {
			mesh.render(shape, bounds);
		}
	}
}
