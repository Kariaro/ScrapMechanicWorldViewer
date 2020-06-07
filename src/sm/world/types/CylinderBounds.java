package sm.world.types;

import org.joml.Vector3f;

public class CylinderBounds implements PartBounds {
	private final float diameter;
	private final float depth;
	private final int axis;
	
	public CylinderBounds(float diameter, float depth, float z) {
		this.diameter = diameter;
		this.depth = depth;
		this.axis = 0; // TODO: Rotate depending on the axis
	}
	
	public Vector3f getMiddle() {
		return null;
	}
	
	public float getHeight() {
		return depth;
	}
	
	public float getWidth() {
		return diameter;
	}
	
	public float getDepth() {
		return diameter;
	}
}
