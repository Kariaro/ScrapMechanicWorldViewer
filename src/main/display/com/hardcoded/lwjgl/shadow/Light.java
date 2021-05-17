package com.hardcoded.lwjgl.shadow;

import org.joml.Vector3f;

/**
 * A light container class.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class Light {
	protected Vector3f position = new Vector3f();
	protected Vector3f color = new Vector3f();
	
	public Light() {
		
	}
	
	/**
	 * @return the position of this light
	 */
	public Vector3f getPosition() {
		return position;
	}
	
	/**
	 * @return the color of this light
	 */
	public Vector3f getColor() {
		return color;
	}
	
	public void setPosition(float x, float y, float z) {
		position.set(x, y, z);
	}
	
	public void setColor(float r, float g, float b) {
		color.set(r, g, b);
	}
	
	@Override
	public String toString() {
		return String.format("Light{ position=%s, color=%s }", position, color);
	}
}
