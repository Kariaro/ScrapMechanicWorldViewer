package sm.world.types;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import sm.asset.ScrapMechanicAssets;
import sm.util.FileUtils;

public class Renderable {
	public final List<Lod> lodList = null;
	
	
	Renderable() {
		
	}
	
	Renderable(String path) throws IOException {
		String json = FileUtils.readFile(ScrapMechanicAssets.resolvePath(path));
		// Remove all comments
		json = json.replaceAll("//.*?[\r\n]+", "\r\n");
		
		ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		JsonParser parser = mapper.createParser(json);
		mapper.readerForUpdating(this).readValue(parser);
	}
	
	public static class Lod {
		public final Integer minViewSize = 0;
		public final Double maxViewDistance = 0.0;
		public final String mesh = null;
		public final String pose0 = null;
		public final String pose1 = null;
		public final String pose2 = null;
		public final List<String> includes = null;
		
		@JsonProperty(value = "subMeshMap")
		private final Map<String, MeshMap> hiddenMap = new HashMap<>();
		@JsonIgnore
		public final Map<String, MeshMap> subMeshMap = Collections.unmodifiableMap(hiddenMap);
		
		@JsonIgnore
		private boolean isMeshList;
		
		public final List<Animation> animationList = null;
		
		@JsonSetter(value = "subMeshList")
		private void setSubMeshList(List<MeshMap> list) {
			isMeshList = true;
			for(int i = 0; i < list.size(); i++) {
				hiddenMap.put(String.valueOf(i), list.get(i));
			}
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
		public final List<String> textureList = null;
		public final String material = null;
	}
	
	public static class Animation {
		public String name = null;
		public String file = null;
		public Boolean looping = false;
	}
}
