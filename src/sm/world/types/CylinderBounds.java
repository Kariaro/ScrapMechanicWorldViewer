package sm.world.types;

import org.joml.Vector3f;

public class CylinderBounds implements PartBounds {
	private final float diameter;
	private final float depth;
	private final String axis;
	
	public CylinderBounds(float diameter, float depth, String axis) {
		this.diameter = diameter;
		this.depth = depth;
		this.axis = axis;
	}
	
	public Vector3f getMiddle() {
		return null;
	}
	
	public float getHeight() {
		switch(axis) {
			case "Z": return diameter;
			case "Y": return diameter;
		}
		return depth;
	}
	
	public float getWidth() {
		switch(axis) {
			case "Z": return diameter;
			case "Y": return depth;
		}
		return diameter;
	}
	
	public float getDepth() {
		switch(axis) {
			case "Z": return depth;
			case "Y": return diameter;
		}
		return diameter;
	}
}
