package com.hardcoded.world.types;

import java.util.UUID;

/**
 * An implementation of a asset.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class SMAsset {
	public UUID uuid;
	public String name;
	public Renderable renderable;
	public String slope;
	
	public Object physics;
	public Object defaultColors;
	
	@Override
	public String toString() {
		return "Asset@" + uuid + '@' + name;
	}
}
