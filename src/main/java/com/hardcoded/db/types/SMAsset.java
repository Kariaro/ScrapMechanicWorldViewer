package com.hardcoded.db.types;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonSetter;

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
	public Map<String, List<String>> defaultColors;
	
	@SuppressWarnings("unchecked")
	@JsonSetter(value = "defaultColors")
	private void setDefaultColors(Map<String, Object> map) {
		defaultColors = new LinkedHashMap<>();
		
		for(String key : map.keySet()) {
			Object value = map.get(key);
			
			if(value instanceof ArrayList) {
				defaultColors.put(key, (List<String>)value);
			} else if(value instanceof String) {
				defaultColors.put(key, List.of((String)value));
			} else {
				throw new RuntimeException("Unknown 'defaultColors' type '" + value + "' (" + (value == null ? "<null>":value.getClass()) + ")");
			}
		}
	}
	
	@Override
	public String toString() {
		return "Asset@" + uuid + '@' + name;
	}
}
