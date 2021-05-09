package com.hardcoded.asset;

import static com.hardcoded.asset.ScrapMechanicAssetHandler.*;

import java.io.File;
import java.util.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hardcoded.logger.Log;
import com.hardcoded.util.FileUtils;
import com.hardcoded.util.StringUtils;
import com.hardcoded.util.ValueUtils;
import com.hardcoded.world.types.*;

/**
 * This class is used to load data.
 * 
 * @author HardCoded
 * @since v0.1
 */
class ScrapMechanicLoader {
	private static final Log LOGGER = Log.getLogger();
	public static final String SHAPE_SETS = "Objects/Database/shapesets.json";
	public static final String ASSET_SETS = "Terrain/Database/assetsets.json";
	public static final String CLUTTER    = "Terrain/Database/clutter.json";
	
	private ScrapMechanicAssetHandler handler;
	public ScrapMechanicLoader(ScrapMechanicAssetHandler handler) {
		this.handler = handler;
	}
	
	protected void load() throws Exception {
		loadAllShapeSets();
		loadAllAssetSets();
		loadAllClutter();
	}
	
	private void loadAllShapeSets() throws Exception {
		Set<String> set = new HashSet<>();
		loadShapeSets(new File($GAME_DATA, SHAPE_SETS), set);
		loadShapeSets(new File($SURVIVAL_DATA, SHAPE_SETS), set);
		loadShapeSets(new File($CHALLENGE_DATA, SHAPE_SETS), set);
	}
	
	private void loadAllAssetSets() throws Exception {
		Set<String> set = new HashSet<>();
		loadAssetSets(new File($GAME_DATA, ASSET_SETS), set);
		loadAssetSets(new File($SURVIVAL_DATA, ASSET_SETS), set);
		loadAssetSets(new File($CHALLENGE_DATA, ASSET_SETS), set);
	}
	
	private void loadAllClutter() throws Exception {
		loadClutter(new File($GAME_DATA, CLUTTER));
		loadClutter(new File($SURVIVAL_DATA, CLUTTER));
		loadClutter(new File($CHALLENGE_DATA, CLUTTER));
	}
	
	
	private void loadShapeSets(File shapesets, Set<String> loaded) throws Exception {
		if(!shapesets.exists()) return;
		LOGGER.debug("Reading path: %s", shapesets);
		
		String shapes = FileUtils.readFile(shapesets);
		String[] values;
		{
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(StringUtils.removeComments(shapes));
			parser.nextValue();
			parser.nextToken();
			parser.nextToken();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
			values = mapper.readValue(parser, String[].class);
		}
		
		for(String value : values) {
			if(loaded.contains(value)) continue;
			loaded.add(value);
			
			String path = resolvePath(value);
			String json = StringUtils.removeComments(FileUtils.readFile(path));
			
			LOGGER.debug("Reading path: %s", path);
			JsonFactory factory = new JsonFactory();
			loadObject(factory.createParser(json));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void loadAssetSets(File assetsets, Set<String> loaded) throws Exception {
		if(!assetsets.exists()) return;
		LOGGER.debug("Reading path: %s", assetsets);
		
		String shapes = FileUtils.readFile(assetsets);
		List<String> values = new ArrayList<>();
		{
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(StringUtils.removeComments(shapes));
			parser.nextValue();
			parser.nextToken();
			parser.nextToken();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
			
			for(Map map : mapper.readValue(parser, Map[].class)) {
				Object obj = map.get("assetSet");
				if(obj != null) {
					values.add(ValueUtils.toString(obj, ""));
				}
			}
		}
		
		for(String value : values) {
			if(loaded.contains(value)) continue;
			loaded.add(value);
			
			String path = resolvePath(value);
			String json = StringUtils.removeComments(FileUtils.readFile(path));
			
			LOGGER.debug("Reading path: %s", path);
			JsonFactory factory = new JsonFactory();
			loadObject(factory.createParser(json));
		}
	}
	
	private void loadObject(JsonParser parser) throws Exception {
		parser.nextValue();
		String typeName = parser.nextFieldName();
		parser.nextValue();
		
		switch(typeName) {
			case "partList":
				loadParts(parser);
				break;
			case "blockList":
				loadBlocks(parser);
				break;
			case "assetListRenderable":
				loadAssets(parser);
				break;
			default:
				LOGGER.warn("Unsupported type '%s'", typeName);
		}
	}
	
	private void loadParts(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMPart[] array = mapper.readValue(parser, SMPart[].class);
		
		for(SMPart part : array) {
			if(handler.parts.containsKey(part.uuid)) continue;
			handler.parts.put(part.uuid, part);
		}
	}
	
	private void loadBlocks(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMBlock[] array = mapper.readValue(parser, SMBlock[].class);
		
		for(SMBlock block : array) {
			if(handler.blocks.containsKey(block.uuid)) continue;
			handler.blocks.put(block.uuid, block);
		}
	}
	
	private void loadAssets(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMAsset[] array = mapper.readValue(parser, SMAsset[].class);
		
		for(SMAsset asset : array) {
			if(handler.assets.containsKey(asset.uuid)) continue;
			handler.assets.put(asset.uuid, asset);
		}
	}
	
	private void loadClutter(File file) throws Exception {
		if(!file.exists()) return;
		
		String content = FileUtils.readFile(file);
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(StringUtils.removeComments(content));
		parser.nextToken();
		parser.nextToken();
		parser.nextToken();
		
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMClutter[] array = mapper.readValue(parser, SMClutter[].class);
		
		for(SMClutter clutter : array) {
			if(handler.clutters.containsKey(clutter.uuid)) continue;
			handler.clutters.put(clutter.uuid, clutter);
		}
	}
	
//	@Deprecated(forRemoval = true)
//	private void loadDebugSets() throws Exception {
//		File dir = new File($USER_DATA, "Mods/SQLiteTesting/Objects/Database/ShapeSets");
//		
//		for(File file : dir.listFiles()) {
//			if(!file.getName().endsWith(".json")) continue;
//			
//			String json = StringUtils.removeComments(FileUtils.readFile(file));
//			
//			LOGGER.debug("Reading path: %s", file);
//			JsonFactory factory = new JsonFactory();
//			JsonParser parser = factory.createParser(json);
//			parser.nextValue();
//			String typeName = parser.nextFieldName();
//			parser.nextValue();
//			
//			switch(typeName) {
//				case "partList":
//					loadParts(parser);
//					break;
//				case "blockList":
//					loadBlocks(parser);
//					break;
//				default:
//					LOGGER.warn("Unsupported type '%s'", typeName);
//			}
//		}
//	}
}
