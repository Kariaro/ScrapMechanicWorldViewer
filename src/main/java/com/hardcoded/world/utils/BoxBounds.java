package com.hardcoded.world.utils;

import org.joml.Vector3f;

/**
 * A box bounds implementation.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class BoxBounds {
	private final float width;
	private final float height;
	private final float depth;
	private final Vector3f middle;
	
	public BoxBounds(float x, float y, float z) {
		this.width = x;
		this.height = y;
		this.depth = z;
		this.middle = new Vector3f(x / 2.0f, y / 2.0f, z / 2.0f);
	}
	
	public Vector3f getMiddle() {
		return middle;
	}
	
	public float getHeight() {
		return height;
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getDepth() {
		return depth;
	}
	
	@Override
	public String toString() {
		return String.format("{ %.4f, %.4f, %.4f }", width, height, depth);
	}
}
