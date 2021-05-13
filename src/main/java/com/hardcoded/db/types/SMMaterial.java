package com.hardcoded.db.types;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * An implementation of an hlsl material.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class SMMaterial {
	public String name;
	
	public Set<String> includes = new LinkedHashSet<>();
	public Set<String> defines = new LinkedHashSet<>();
	public Set<String> flags = new LinkedHashSet<>();
	
	@JsonSetter(value = "hlsl")
	private void setContent(Map<String, Object> map) {
		if(map.containsKey("defines")) {
			Object obj = map.get("defines");
			if(obj instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> obj_list = (List<String>)obj;
				
				for(String str : obj_list) {
					defines.add(str);
				}
			}
		}
	}
	
	@JsonSetter(value = "includes")
	private void setIncludes(List<String> list) {
		includes.addAll(list);
	}
	
	@JsonSetter(value = "flags")
	private void setFlags(List<String> list) {
		flags.addAll(list);
	}
	
	
	/**
	 * Returns {@code true} if this material contains the specified {@code flag}.
	 * @param flag the flag to check
	 * @return {@code true} if this material contains the specified {@code flag}
	 */
	public boolean hasFlag(Flags flag) {
		if(flag == null) return false;
		return flags.contains(flag.name());
	}
	
	/**
	 * Returns {@code true} if this material contains the specified {@code type}.
	 * @param ype the ype to check
	 * @return {@code true} if this material contains the specified {@code ype}
	 */
	public boolean hasDefined(Types type) {
		if(type == null) return false;
		return defines.contains(type.name());
	}
	
	public static enum Flags {
		ALPHA,
		WATER,
		GLASS,
		NO_SHADOWS,
		WATER_ALPHA,
		ADDITIVE,
		BACKGROUND,
		ALPHA_BACKFACE,
	}
	
	public static enum Types {
		AO_TEX,
		ASG_TEX,
		NOR_TEX,
		
		NOR_D_TEX,
		NOR_D_UV1,
		
		ALPHA,
		FLIP_BACKFACE_NORMALS,
		
		INSTANCED,
		CUSTOM_TILING,
		TEXTURE_ARRAYS,
		
		WAVE,
		WIND_BASE,
		WIND_SOFT,
		
		SKEL_ANIM,
		UV_ANIM,
		POSE_0_ANIM,
		POSE_1_ANIM,
		POSE_2_ANIM,
		
		MATERIAL_FOILAGED,
		MATERIAL_ANISO,
		
		HARD_LIGHT_CLIP,
		SOFT_LIGHT_CLIP,
		LIGHT_MAP,
		NO_SHADOWS,
	}
}
