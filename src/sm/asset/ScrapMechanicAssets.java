package sm.asset;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import sm.main.Main;
import sm.util.FileUtils;
import sm.util.StringUtils;
import sm.world.World;
import sm.world.types.Block;
import sm.world.types.Part;

public final class ScrapMechanicAssets {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	private static final ScrapMechanicAssets ASSET;
	static {
		ASSET = new ScrapMechanicAssets();
	}
	
	
	private File basePath;
	private File gameDataPath;
	private File challengeDataPath;
	private File survivalDataPath;
	private File modsDataPath;
	
	// TODO: Everything should have unique UUIDs so change this to only one Map!
	// TODO: Tools ??? Models ???
	// TODO: This does not work for mods!!!
	private final Map<UUID, Block> blocks;
	private final Map<UUID, Part> parts;
	
	private ScrapMechanicAssets() {
		blocks = new HashMap<>();
		parts = new HashMap<>();
	}
	
	/**
	 * Set's the base path for lookup purposes.
	 * This function initlializes all paths
	 * 
	 * @param path
	 * @throws FileNotFoundException if the path does not exist or if it was not an directory
	 */
	public static void setBasePath(String path) throws FileNotFoundException {
		File file = new File(path);
		
		if(!file.exists() || !file.isDirectory())
			throw new FileNotFoundException("The directory '" + path + "' does not exist!");
		
		
		ASSET.basePath = file;
		ASSET.challengeDataPath = new File(ASSET.basePath, "ChallengeData");
		ASSET.survivalDataPath = new File(ASSET.basePath, "Survival");
		ASSET.gameDataPath = new File(ASSET.basePath, "Data");
	}
	
	public static String resolvePath(String path) {
		if(path.startsWith("$GAME_DATA")) {
			String resolve = path.substring(10);
			return new File(ASSET.gameDataPath, resolve).getAbsolutePath();
		}
		
		if(path.startsWith("$CHALLENGE_DATA")) {
			String resolve = path.substring(15);
			return new File(ASSET.challengeDataPath, resolve).getAbsolutePath();
		}
		
		if(path.startsWith("$SURVIVAL_DATA")) {
			String resolve = path.substring(14);
			return new File(ASSET.survivalDataPath, resolve).getAbsolutePath();
		}
		
		if(path.startsWith("$MOD_DATA")) {
			File dir = new File(World.$USER_DATA, "Mods/SQLiteTesting/");
			
			String resolve = path.substring(9);
			return new File(dir, resolve).getAbsolutePath();
			//throw new UnsupportedOperationException("The mod path cannot be resolved without knowing the mod folder!");
		}
		
		return path;
	}
	
	public static boolean unloadAllAssets() {
		ASSET.blocks.clear();
		ASSET.parts.clear();
		return false;
	}
	
	public static boolean loadAllAssets() throws Exception {
		ASSET.load();
		return true;
	}
	
	// TODO: Separate the different shapeSets. // Survival, Creative, Challenge
	private boolean loaded = false;
	private void load() throws Exception {
		if(this.loaded) return;
		this.loaded = true;
		Set<String> loaded = new HashSet<String>();
		
		loadShapeSets(new File(gameDataPath, "Objects/Database/shapesets.json"), loaded);
		loadShapeSets(new File(survivalDataPath, "Objects/Database/shapesets.json"), loaded);
		//loadShapeSets(new File(challengeDataPath, "Objects/Database/shapesets.json"), loaded);
		
		// TODO: Load mod assets!!!!
		// TODO: Only load required assets to reduce time -> Inside the 'sm.lwjgl' project!
		loadDebugSets();
		
	}
	
	private void loadDebugSets() throws Exception {
		File dir = new File(World.$USER_DATA, "Mods/SQLiteTesting/Objects/Database/ShapeSets");
		
		for(File file : dir.listFiles()) {
			if(!file.getName().endsWith(".json")) continue;
			
			String json = StringUtils.removeComments(FileUtils.readFile(file));
			
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(json);
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
				default:
					LOGGER.log(Level.WARNING, "Unsupported type '{0}'", typeName);
			}
		}
	}
	
	private void loadShapeSets(File shapesets, Set<String> loaded) throws Exception {
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
			
			//System.out.println("Reading: " + path);
			//System.out.println(json);
			
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(json);
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
				default:
					LOGGER.log(Level.WARNING, "Unsupported type '{0}'", typeName);
			}
		}
	}
	
	private void loadParts(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Part[] array = mapper.readValue(parser, Part[].class);
		
		for(Part part : array) {
			// TODO: If this was an collisiton it was probably because
			//       of a mod and not because of ScrapMechanic
			if(parts.containsKey(part.uuid)) continue; // TODO: What if this was a collision???
			
			String uuidStr = part.uuid.toString();
			if(uuidStr.equals("ea4237f4-851a-4751-a1bc-3f85b7488243")
			|| uuidStr.equals("d4784875-1ede-4d00-a432-f390f0d8fc73")
			|| uuidStr.equals("f8da6b41-03d7-4bc1-ba94-011a351b1569")
			|| uuidStr.equals("9a9185d7-9709-4113-8bb0-d0e22fb08e42")
			|| uuidStr.equals("80550b05-f9eb-433c-9773-2af3beb1479d")
			|| uuidStr.equals("8f7fd0e7-c46e-4944-a414-7ce2437bb30f")) {
			}
			parts.put(part.uuid, part);
		}
	}
	
	private void loadBlocks(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Block[] array = mapper.readValue(parser, Block[].class);
		
		for(Block block : array) {
			if(blocks.containsKey(block.uuid)) continue; // TODO: What if this was a collision???
			
			blocks.put(block.uuid, block);
		}
	}
	
	
	public static Part getPart(UUID uuid) {
		return ASSET.parts.getOrDefault(uuid, null);
	}
	
	public static Block getBlock(UUID uuid) {
		return ASSET.blocks.getOrDefault(uuid, null);
	}
	
	public static Collection<Part> getAllParts() {
		return Collections.unmodifiableCollection(ASSET.parts.values());
	}
	
	public static Collection<Block> getAllBlocks() {
		return Collections.unmodifiableCollection(ASSET.blocks.values());
	}
}
