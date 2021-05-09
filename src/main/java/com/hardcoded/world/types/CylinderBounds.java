package com.hardcoded.world.types;

import org.joml.Vector3f;

/**
 * A cylinder bounds implementation.
 * 
 * @author HardCoded
 * @since v0.1
 * 
 * @deprecated The margin value is not correctly handles and should be fixed before reaching v1.0.
 */
public class CylinderBounds implements PartBounds {
	private final float diameter;
	private final float depth;
	private final String axis;
	private final float margin;
	
	public CylinderBounds(float diameter, float depth, float margin, String axis) {
		this.diameter = diameter;
		this.depth = depth;
		this.axis = axis;
		this.margin = margin;
	}
	
	public Vector3f getMiddle() {
		return null;
	}
	
	public float getHeight() {
		switch(axis) {
			case "X": return diameter;
			case "Y": return depth;
			case "Z": return diameter; // good
		}
		
		return -1;
	}
	
	public float getWidth() {
		switch(axis) {
			case "X": return depth;
			case "Y": return diameter;
			case "Z": return diameter; // good
		}
		
		return -1;
	}
	
	public float getDepth() {
		switch(axis) {
			case "X": return diameter;
			case "Y": return diameter;
			case "Z": return depth; // good
		}
		
		return -1;
	}
	
	public float getCylinderDiameter() {
		return diameter;
	}
	
	public float getCylinderDepth() {
		return depth;
	}
	
	public float getCylinderMargin() {
		return margin;
	}
	
	public String getCylinderAxis() {
		return axis;
	}
}
