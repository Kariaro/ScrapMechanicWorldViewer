package sm.lwjgl.mesh;

import java.util.ArrayList;
import java.util.List;

import sm.lwjgl.Camera;
import sm.lwjgl.shader.PartShader;
import sm.objects.BodyList.ChildShape;
import sm.world.types.Part;
import sm.world.types.Renderable;
import sm.world.types.Renderable.Lod;
import sm.world.types.ShapeUtils.Bounds3D;

public class WorldPartRender {
	//private final Part part;
	//private final PartShader shader;
	private final List<PartMesh> meshes;
	
	public WorldPartRender(Part part, PartShader shader) {
		//this.part = part;
		//this.shader = shader;
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
	
	public void render(ChildShape shape, Bounds3D bounds, Camera camera) {
		// TODO: Implement lod objects
		
		//Vector3f pos = camera.getPosition();
		
		for(PartMesh mesh : meshes) {
			//float dist = pos.distance(shape.xPos, shape.yPos, 0) * 2;
			
			mesh.render(shape, bounds);
			break;
		}
	}
}
