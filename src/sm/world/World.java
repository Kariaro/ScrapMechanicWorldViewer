package sm.world;

import java.util.logging.Logger;

import sm.game.SaveFile;
import sm.objects.Game;

/**
 * This class will load SQLite database worlds from ScrapMechanic
 * 
 * @author HardCoded
 */
public class World {
	private static final Logger LOGGER = Logger.getLogger(World.class.getName());
	
	private final SaveFile save;
	private World(SaveFile save) {
		this.save = save;
	}
	
	public Game getGame() {
		return save.getGame();
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
}
