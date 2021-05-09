package com.hardcoded.world.types;

import org.joml.Vector3f;

public interface PartBounds {
	public Vector3f getMiddle();
	public float getHeight();
	public float getWidth();
	public float getDepth();
}
