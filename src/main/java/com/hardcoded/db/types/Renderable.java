package com.hardcoded.db.types;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.util.FileUtils;

/**
 * A renderable asset implementation.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class Renderable {
	public List<Lod> lodList;
	
	
	Renderable() {
		
	}
	
	Renderable(String path) throws IOException {
		String json = FileUtils.readFile(ScrapMechanicAssetHandler.resolvePath(path));
		// Remove all comments
		json = json.replaceAll("//.*?[\r\n]+", "\r\n");
		
		ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		JsonParser parser = mapper.createParser(json);
		mapper.readerForUpdating(this).readValue(parser);
	}
	
	public static class Lod {
		public Integer minViewSize = 0;
		public Double maxViewDistance = 0.0;
		public String mesh;
		public String pose0;
		public String pose1;
		public String pose2;
		public List<String> includes;
		
		public Map<String, MeshMap> subMeshMap = new LinkedHashMap<>();
		
		@JsonIgnore
		private boolean isMeshList;
		
		public List<Animation> animationList;
		
		@JsonSetter(value = "subMeshList")
		private void setSubMeshList(List<MeshMap> list) {
			isMeshList = true;
			for(int i = 0; i < list.size(); i++) {
				subMeshMap.put(String.valueOf(i), list.get(i));
			}
		}
		
		@JsonSetter(value = "subMeshMap")
		private void setSubMesMap(Map<String, MeshMap> map) {
			subMeshMap.putAll(map);
		}
		
		@JsonAnySetter
		public void setAnything(String string, Object obj) {
			System.out.println("setAnything: " + string);
			System.out.println("setAnything: " + obj);
		}
		
		public boolean isMeshList() {
			return isMeshList;
		}
	}
	
	public static class MeshMap {
		public List<String> textureList;
		public Map<String, Object> custom;
		public String material;
	}
	
	public static class Animation {
		public String name;
		public String file;
		public Boolean looping = false;
	}
}
