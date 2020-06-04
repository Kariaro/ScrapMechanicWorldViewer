package sm.world;

import java.io.File;
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

import sm.game.SaveFile;
import sm.objects.Game;
import sm.util.FileUtils;

/**
 * This class will load SQLite database worlds from ScrapMechanic
 * 
 * @author HardCoded
 */
public class World {
	// TODO: Ask for this path before starting or do a lookup.
	public static final String $SURVIVAL_DATA = "D:\\Steam\\steamapps\\common\\Scrap Mechanic\\Survival";
	public static final String $GAME_DATA = "D:\\Steam\\steamapps\\common\\Scrap Mechanic\\Data";
	
	@Deprecated
	public static final String $MOD_DATA = null;
	
	private static final Logger LOGGER = Logger.getLogger(World.class.getName());
	
	private final Map<UUID, Block> blocks;
	private final Map<UUID, Part> parts;
	private final SaveFile save;
	private World(SaveFile save) {
		this.save = save;
		
		blocks = new HashMap<>();
		parts = new HashMap<>();
		
		try {
			load();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// TODO: Load from '$GAME_DATA' and all mods
	private void load() throws Exception {
		Set<String> loaded = new HashSet<String>();
		
		loadShapeSets(new File($GAME_DATA, "Objects\\Database\\shapesets.json"), loaded);
		loadShapeSets(new File($SURVIVAL_DATA, "Objects\\Database\\shapesets.json"), loaded);
		
		/*for(File file : database.listFiles()) {
			if(!file.getName().endsWith(".json")) continue;
			String json = FileUtils.readFile(file);

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
		}*/
	}
	
	private void loadShapeSets(File shapesets, Set<String> loaded) throws Exception {
		String shapes = FileUtils.readFile(shapesets);
		String[] values;
		{
			// Remove all comments
			shapes = shapes.replaceAll("//.*?[\r\n]+", "\r\n");
			System.out.println(shapes);
			
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(shapes);
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
			
			String path = World.getPath(value);
			String json = FileUtils.readFile(path);
			
			System.out.println("Reading: " + path);
			
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
			parts.put(part.uuid, part);
		}
	}
	
	private void loadBlocks(JsonParser parser) throws Exception {
		ObjectMapper mapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Block[] array = mapper.readValue(parser, Block[].class);
		
		for(Block block : array) {
			blocks.put(block.uuid, block);
		}
	}
	
	public Game getGame() {
		return save.getGame();
	}
	
	public Part getPart(UUID uuid) {
		return parts.getOrDefault(uuid, null);
	}
	
	public Block getBlock(UUID uuid) {
		return blocks.getOrDefault(uuid, null);
	}
	
	public Collection<Part> getAllParts() {
		return Collections.unmodifiableCollection(parts.values());
	}
	
	public Collection<Block> getAllBlocks() {
		return Collections.unmodifiableCollection(blocks.values());
	}
	
	// TODO: GetBodies
	
	// TODO: GetPlayer
	
	// TODO: GetTools
	
	// TODO: GetStuff
	
	// TODO: Set scrap mechanic directory
	
	public SaveFile getSaveFile() {
		return save;
	}
	
	public static World loadWorld(SaveFile save) {
		return new World(save);
	}
	
	public static World loadWorld(String path) {
		return new World(SaveFile.loadSaveFile(path));
	}

	public static String getPath(String text) {
		return text.replace("$GAME_DATA", $GAME_DATA)
				   .replace("$SURVIVAL_DATA", $SURVIVAL_DATA)
				   .replace('\\', '/');
	}
}
