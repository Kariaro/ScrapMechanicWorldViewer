package com.hardcoded.sm.lua;

import java.util.Map;
import java.util.Set;

public class Data {
	private final Object obj;
	
	public Data(Object obj) {
		this.obj = obj;
	}
	
	public Data get(Object key) {
		if(obj instanceof Map) {
			return new Data(toMap().get(key));
		}
		
		return null;
	}
	
	public Set<Object> keySet() {
		if(obj instanceof Map) {
			return toMap().keySet();
		}
		
		return Set.of();
	}
	
	@SuppressWarnings("unchecked")
	public Map<Object, Object> toMap() {
		if(obj instanceof Map) {
			return (Map<Object, Object>)obj;
		}
		
		return Map.of();
	}
	
	public int getInt() {
		if(obj instanceof Number) {
			return ((Number)obj).intValue();
		}
		
		return 0;
	}
	
	public int getInt(Object key) {
		Object obj = toMap().get(key);
		
		if(obj instanceof Number) {
			return ((Number)obj).intValue();
		}
		
		return 0;
	}
	
	public float getFloat(Object key) {
		Object obj = toMap().get(key);
		
		if(obj instanceof Number) {
			return ((Number)obj).floatValue();
		}
		
		return 0.0f;
	}
	
	public String toStr() {
		return obj == null ? null:obj.toString();
	}
}
