package me.hardcoded.smviewer.asset;

import static me.hardcoded.smviewer.asset.ScrapMechanicAssetHandler.*;

import java.io.File;
import java.util.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.hardcoded.smreader.logger.Log;
import me.hardcoded.smviewer.db.types.*;
import me.hardcoded.smviewer.util.FileUtils;
import me.hardcoded.smviewer.util.StringUtils;
import me.hardcoded.smviewer.util.ValueUtils;

/**
 * This class is used to load data.
 * 
 * @author HardCoded
 * @since v0.1
 */
class ScrapMechanicLoader {
	private static final Log LOGGER = Log.getLogger();
	
	// Materials
	public static final String TERRAINMATERIALS = "Terrain/Materials/terrainmaterials.json";
	public static final String PARTMATERIALS    = "Objects/Materials/partmaterials.json";
	
	// Database
	public static final String HARVESTABLE_SETS = "Harvestables/Database/harvestablesets.json";
	public static final String SHAPE_SETS       = "Objects/Database/shapesets.json";
	public static final String ASSET_SETS       = "Terrain/Database/assetsets.json";
	public static final String CLUTTER          = "Terrain/Database/clutter.json";
	
	private final ScrapMechanicAssetHandler handler;
	public ScrapMechanicLoader(ScrapMechanicAssetHandler handler) {
		this.handler = handler;
	}
	
	private String[] paths;
	protected void load() throws Exception {
		this.paths = new String[] {
			$GAME_DATA,
			$SURVIVAL_DATA,
			$CHALLENGE_DATA
		};
		
		loadAllShapeSets();
		loadAllAssetSets();
		loadAllHarvestableSets();
		loadAllClutter();
		loadAllMaterials();
		
//		Set<String> set = new LinkedHashSet<>();
//		for (String key : handler.materials.keySet()) {
//			SMMaterial mat = handler.materials.get(key);
//
//			set.addAll(mat.flags);
//		}
//
//		System.out.println(set);
//		System.in.read();
	}
	
	private void loadAllShapeSets() throws Exception {
		Set<String> set = new HashSet<>();
		for (String path : paths) {
			loadShapeSets(new File(path, SHAPE_SETS), set);
		}
	}
	
	private void loadAllAssetSets() throws Exception {
		Set<String> set = new HashSet<>();
		for (String path : paths) {
			loadAssetSets(new File(path, ASSET_SETS), set);
		}
	}
	
	private void loadAllHarvestableSets() throws Exception {
		Set<String> set = new HashSet<>();
		for (String path : paths) {
			loadHarvestableSets(new File(path, HARVESTABLE_SETS), set);
		}
	}
	
	private void loadAllClutter() throws Exception {
		for (String path : paths) {
			loadClutter(new File(path, CLUTTER));
		}
	}
	
	private void loadAllMaterials() throws Exception {
		Set<String> set = new HashSet<>();
		for (String path : paths) {
			loadPartMaterials(new File(path, PARTMATERIALS), set);
		}
		
		for (String path : paths) {
			loadPartMaterials(new File(path, TERRAINMATERIALS), set);
		}
	}
	
	private void loadPartMaterials(File partMaterials, Set<String> loaded) throws Exception {
		if (!partMaterials.exists()) return;
		LOGGER.debug("Reading path: %s", partMaterials);
		
		String content = FileUtils.readFile(partMaterials);
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(StringUtils.removeComments(content));
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
		List<Map<String, SMMaterial>> list = mapper.readValues(parser, new TypeReference<Map<String, SMMaterial>>() {}).readAll();
		
		for (int i = 0; i < list.size(); i++) {
			Map<String, SMMaterial> map = list.get(i);
			
			for (String key : map.keySet()) {
				SMMaterial mat = map.get(key);
				
				if (!handler.materials.containsKey(key)) {
					mat.name = key;
					handler.materials.put(key, mat);
				}
			}
		}
	}
	
	private void loadShapeSets(File shapeSets, Set<String> loaded) throws Exception {
		if (!shapeSets.exists()) return;
		LOGGER.debug("Reading path: %s", shapeSets);
		
		String content = FileUtils.readFile(shapeSets);
		String[] values;
		{
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(StringUtils.removeComments(content));
			parser.nextValue();
			parser.nextToken();
			parser.nextToken();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
			values = mapper.readValue(parser, String[].class);
		}
		
		for (String value : values) {
			if (loaded.contains(value)) continue;
			loaded.add(value);
			
			String path = resolvePath(value);
			String json = StringUtils.removeComments(FileUtils.readFile(path));
			
			LOGGER.debug("Reading path: %s", path);
			JsonFactory factory = new JsonFactory();
			loadObject(factory.createParser(json));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void loadHarvestableSets(File harvestableSets, Set<String> loaded) throws Exception {
		if (!harvestableSets.exists()) return;
		LOGGER.debug("Reading path: %s", harvestableSets);
		
		String content = FileUtils.readFile(harvestableSets);
		List<String> values = new ArrayList<>();
		{
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(StringUtils.removeComments(content));
			parser.nextValue();
			parser.nextToken();
			parser.nextToken();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
			
			for (Map map : mapper.readValue(parser, Map[].class)) {
				Object obj = map.get("name");
				if (obj != null) {
					values.add(ValueUtils.toString(obj, ""));
				}
			}
		}
		
		for (String value : values) {
			if (loaded.contains(value)) continue;
			loaded.add(value);
			
			String path = resolvePath(value);
			String json = StringUtils.removeComments(FileUtils.readFile(path));
			
			LOGGER.debug("Reading path: %s", path);
			JsonFactory factory = new JsonFactory();
			loadObject(factory.createParser(json));
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void loadAssetSets(File assetSets, Set<String> loaded) throws Exception {
		if (!assetSets.exists()) return;
		LOGGER.debug("Reading path: %s", assetSets);
		
		String content = FileUtils.readFile(assetSets);
		List<String> values = new ArrayList<>();
		{
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(StringUtils.removeComments(content));
			parser.nextValue();
			parser.nextToken();
			parser.nextToken();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
			
			for (Map map : mapper.readValue(parser, Map[].class)) {
				Object obj = map.get("assetSet");
				if (obj != null) {
					values.add(ValueUtils.toString(obj, ""));
				}
			}
		}
		
		for (String value : values) {
			if (loaded.contains(value)) continue;
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
		
		switch (typeName) {
			case "partList" -> loadParts(parser);
			case "blockList" -> loadBlocks(parser);
			case "assetListRenderable" -> loadAssets(parser);
			case "harvestableList" -> loadHarvestables(parser);
			default -> LOGGER.warn("Unsupported type '%s'", typeName);
		}
	}
	
	private void loadParts(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMPart[] array = mapper.readValue(parser, SMPart[].class);
		
		for (SMPart part : array) {
			if (handler.parts.containsKey(part.uuid)) continue;
			handler.parts.put(part.uuid, part);
		}
	}
	
	private void loadBlocks(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMBlock[] array = mapper.readValue(parser, SMBlock[].class);
		
		for (SMBlock block : array) {
			if (handler.blocks.containsKey(block.uuid)) continue;
			handler.blocks.put(block.uuid, block);
		}
	}
	
	private void loadAssets(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMAsset[] array = mapper.readValue(parser, SMAsset[].class);
		
		for (SMAsset asset : array) {
			if (handler.assets.containsKey(asset.uuid)) continue;
			handler.assets.put(asset.uuid, asset);
		}
	}
	
	private void loadHarvestables(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMHarvestable[] array = mapper.readValue(parser, SMHarvestable[].class);
		
		for (SMHarvestable harvestable : array) {
			if (handler.harvestables.containsKey(harvestable.uuid)) continue;
			handler.harvestables.put(harvestable.uuid, harvestable);
		}
	}
	
	private void loadClutter(File file) throws Exception {
		if (!file.exists()) return;
		
		String content = FileUtils.readFile(file);
		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createParser(StringUtils.removeComments(content));
		parser.nextToken();
		parser.nextToken();
		parser.nextToken();
		
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMClutter[] array = mapper.readValue(parser, SMClutter[].class);
		
		for (SMClutter clutter : array) {
			if (handler.clutters.containsKey(clutter.uuid)) continue;
			handler.clutters.put(clutter.uuid, clutter);
		}
	}
}
