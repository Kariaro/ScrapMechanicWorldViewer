package com.hardcoded.db.types;

import java.util.UUID;

/**
 * A implementation of a clutter asset.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class SMClutter {
	public UUID uuid;
	public String name;
	public String texture;
	public String impostor_texture;
	public String mesh;
	public String material;
	public Double height;
	public Double scaleVariance;
	public Boolean wind;
	public Boolean groundNormal;
	public String slope;
	
	@Override
	public String toString() {
		return "Clutter@" + uuid + '@' + name;
	}
}
