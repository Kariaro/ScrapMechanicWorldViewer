package com.hardcoded.world.types;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hardcoded.util.ValueUtils;

/**
 * An implementation of a part asset.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class SMPart {
	public UUID uuid;
	public String name;
	public Integer legacyId = 0;
	public Renderable renderable;
	public String rotationSet;
	public String sticky;
	public String color;
	public Integer stackSize = 0;
	public Double friction = 0.0;
	public Double restitution = 0.0;
	public Double destroyTime = 0.0;
	
	public Object previewRotation;
	public Object restrictions;
	public Boolean carryItem = false;
	public Boolean harvest = false;
	public Boolean consumable = false;
	public Boolean harvestablePart = false;
	public Boolean showInInventory = false;
	public UUID autoTool;
	public UUID baseUuid;
	
	@JsonIgnore
	private PartBounds bounds;
	
	
	public String physicsMaterial;
	public Boolean flammable = false;
	public Integer qualityLevel = 0;
	public Double density = 0.0;
	
	public PartBounds getBounds() {
		return bounds;
	}
	
	@JsonAnySetter
	public void setTesting(String name, Map<String, Object> map) {
		if(name.equals("box") || name.equals("hull")) {
			bounds = new BoxBounds(
				ValueUtils.toInt(map.get("x")),
				ValueUtils.toInt(map.get("y")),
				ValueUtils.toInt(map.get("z"))
			);
		} else if(name.equals("cylinder")) {
			if(map.containsKey("margin")) {
				bounds = new CylinderBounds(
					ValueUtils.toInt(map.get("diameter")),
					ValueUtils.toInt(map.get("depth")),
					ValueUtils.toFloat(map.get("margin")),
					ValueUtils.toString(map.get("axis"))
				);
			} else {
				bounds = new CylinderBounds(
					ValueUtils.toInt(map.get("diameter")),
					ValueUtils.toInt(map.get("depth")),
					0,
					ValueUtils.toString(map.get("axis"))
				);
			}
		}
	}
	
	@Override
	public String toString() {
		return "Part@" + uuid + '@' + name;
	}
}
