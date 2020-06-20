package sm.world;

import java.io.File;

import sm.asset.ScrapMechanic;
import sm.game.SaveFile;
import sm.objects.Game;

/**
 * This class will load SQLite database worlds from ScrapMechanic
 * 
 * @author HardCoded
 */
public class World {
	private final SaveFile save;
	private World(SaveFile save) {
		this.save = save;
	}
	
	public Game getGame() {
		return save.getGame();
	}
	
	public SaveFile getSaveFile() {
		return save;
	}
	
	
	
	public static World loadWorld(String path) {
		return new World(SaveFile.loadSaveFile(path));
	}
	
	public static World loadWorld(File path) {
		return new World(SaveFile.loadSaveFile(path));
	}
	
	public static World loadWorldFromAppdata(String path) {
		return loadWorld(new File(ScrapMechanic.$USER_DATA, "Save/" + path));
	}
}
