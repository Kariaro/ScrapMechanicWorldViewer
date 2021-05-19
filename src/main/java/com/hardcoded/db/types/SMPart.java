package com.hardcoded.db.types;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hardcoded.util.ValueUtils;
import com.hardcoded.world.utils.BoxBounds;

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
	public Object scripted;
	
	// ???
	public Object otationSet;
	
	@JsonIgnore
	private BoxBounds bounds;
	
	
	public String physicsMaterial;
	public Boolean flammable = false;
	public Integer qualityLevel = 0;
	public Double density = 0.0;
	
	public BoxBounds getBounds() {
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
			String axis = ValueUtils.toString(map.get("axis"));
			
			float diameter = ValueUtils.toFloat(map.get("diameter"));
			float depth = ValueUtils.toFloat(map.get("depth"));
			
			switch(axis.toLowerCase()) {
				case "x": {
					bounds = new BoxBounds(depth, diameter, diameter);
					break;
				}
				case "y": {
					bounds = new BoxBounds(diameter, depth, diameter);
					break;
				}
				case "z": {
					bounds = new BoxBounds(diameter, diameter, depth);
					break;
				}
			}
		} else if(name.equals("sphere")) {
			float diameter = ValueUtils.toFloat(map.get("diameter"));
			bounds = new BoxBounds(diameter, diameter, diameter);
		}
		
//		if(bounds == null) {
//			System.out.println(name + ", " + map);
//			try {
//				System.in.read();
//			} catch(IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	@Override
	public String toString() {
		return "Part@" + uuid + '@' + name;
	}
}
