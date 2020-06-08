package sm.world.types;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Part {
	public final UUID uuid = null;
	public final String name = null;
	public final Integer legacyId = 0;
	public final Renderable renderable = null;
	public final String rotationSet = null;
	public final String sticky = null;
	public final String color = null;
	public final Integer stackSize = 0;
	public final Double friction = 0.0;
	public final Double restitution = 0.0;
	public final Double destroyTime = 0.0; // ???
	
	public final Object previewRotation = null;
	public final Object restrictions = null;
	public final Boolean carryItem = false;
	public final Boolean harvest = false;
	public final Boolean harvestablePart = false;
	public final UUID autoTool = null; // ???
	public final UUID baseUuid = null; // ???
	
	@JsonIgnore
	private PartBounds bounds; // TODO: ???
	
	
	public final String physicsMaterial = null;
	public final Boolean flammable = false;
	public final Integer qualityLevel = 0;
	public final Double density = 0.0;
	
	public PartBounds getBounds() {
		return bounds;
	}
	
	@JsonAnySetter
	public void setTesting(String name, Map<String, Object> map) {
		if(name.equals("box") || name.equals("hull")) {
			bounds = new BoxBounds((int)map.get("x"), (int)map.get("y"), (int)map.get("z"));
		} else if(name.equals("cylinder")) {
			// TODO: Axis
			bounds = new CylinderBounds((int)map.get("diameter"), (int)map.get("depth"), map.get("axis").toString());
			
		}
		
		//System.out.println("Name: " + name);
		//System.out.println("Map: " + map);
		//System.out.println();
	}
	
	@Override
	public String toString() {
		return "Part@" + uuid + '@' + name;
	}
}
