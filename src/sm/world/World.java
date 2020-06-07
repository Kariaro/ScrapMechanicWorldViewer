package sm.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import sm.asset.ScrapMechanicAssets;
import sm.game.SaveFile;
import sm.objects.Game;

/**
 * This class will load SQLite database worlds from ScrapMechanic
 * 
 * @author HardCoded
 */
public class World {
	// FIXME: This path should be put or read in some property file! (Steam lookup???)
	public static final String $SURVIVAL_DATA = "D:/Steam/steamapps/common/Scrap Mechanic/Survival";
	public static final String $GAME_DATA = "D:/Steam/steamapps/common/Scrap Mechanic/Data";
	
	// TODO: This is used by this software and not by ScrapMechanic.
	public static final String $USER_DATA;
	
	private static final Logger LOGGER = Logger.getLogger(World.class.getName());
	
	static {
		String appdata_path = System.getenv("APPDATA");
		File sm_userpath = new File(appdata_path, "Axolot Games/Scrap Mechanic/User");
		
		// TODO: Depending on your steam profile you have different user directories!
		File[] sm_users = sm_userpath.listFiles();
		
		if(sm_users.length < 1) {
			// TODO: What should we do here?
			throw new RuntimeException("No steam profile found");
		}
		
		// Selected the first steam profile.
		$USER_DATA = sm_users[0].getAbsolutePath();
		
		try {
			LOGGER.log(Level.INFO, "Loading assets");
			
			// TODO: Get the correct steam path for ScrapMechanic
			ScrapMechanicAssets.setBasePath("D:/Steam/steamapps/common/Scrap Mechanic");
			ScrapMechanicAssets.loadAllAssets();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
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
		return loadWorld(new File($USER_DATA, "Save/" + path));
	}
}
