package com.hardcoded.db.types;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * An implementation of a harvestable asset.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class SMHarvestable {
	public String name;
	public UUID uuid;
	public Renderable renderable;
	
	public String col;
	public String type;
	public String slope;
	public Boolean showInEditor = false;
	public Boolean disableClutter = false;
	public Boolean removable = false;
	
	public Object collides;
	public Object physics;
	public Object script;
	
	public Integer size = 1;
	public Integer mass = 1;
	public String material;
	
	public int color = 0;
	
	@SuppressWarnings("unchecked")
	@JsonSetter(value = "color")
	private void setColor(Object object) {
		String string = "";
		if(object instanceof List) {
			List<String> list = (List<String>)object;
			if(!list.isEmpty()) {
				string = list.get(0);
			}
		} else if(object instanceof String) {
			string = (String)object;
		} else {
			// Bad
			return;
		}
		
		try {
			int value = Integer.parseInt(string, 16);
			
			if(string.length() == 6) {
				this.color = (value << 8) | 0xff;
			} else {
				this.color = value;
			}
		} catch(NumberFormatException e) {
			
		}
	}
}
