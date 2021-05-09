package com.hardcoded.world.types;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An implementation of a block asset.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class SMBlock {
	public UUID uuid;
	public String name;
	public Integer legacyId = 0;
	public Integer tiling = 0;
	public String color;
	public String dif;
	public String asg;
	public String nor;
	public Boolean glass = false;
	
	@JsonIgnore
	public Object ratings;
	
	public String physicsMaterial;
	public Boolean flammable = false;
	public Integer qualityLevel = 0;
	public Double density = 0.0;
	
	@Override
	public String toString() {
		return "Block@" + uuid + '@' + name;
	}
}
