package valve;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.hardcoded.smreader.logger.Log;
import me.hardcoded.smreader.logger.Log.Level;

/**
 * A simple implementation for finding games inside steam.
 * 
 * @author HardCoded <https://github.com/Kariaro>
 */
public final class Steam {
	private static final Log LOGGER = Log.getLogger();
	
	private Steam() {
		
	}
	
	/**
	 * Returns the installation path of Steam.
	 * @return the installation path of Steam
	 */
	public static File getSteamInstallPath() {
		String installPath = RegQuery.readRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Valve\\Steam", "InstallPath");
		if(installPath == null) { // 32 bit
			installPath = RegQuery.readRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Valve\\Steam", "InstallPath");
		}
		
		if(installPath == null) {
			LOGGER.log(Level.WARNING, "Could not find the steam installPath in the registry");
			return null;
		}
		
		return new File(installPath);
	}
	
	/**
	 * Returns a list with all Steam library paths.
	 * @return a list with all Steam library paths
	 */
	public static List<File> getSteamLibraryPaths() {
		File install_path = getSteamInstallPath();
		
		List<File> library_paths = new ArrayList<>();
		library_paths.add(new File(install_path, "steamapps/common"));
		
		File file = new File(install_path, "steamapps/libraryfolders.vdf");
		if(!file.exists()) {
			return library_paths;
		}
		
		ValveData data = new ValveData(file).get("libraryfolders");
		for(String key : data.getGroupNames()) {
			String path = data.get(key).getValue("path");
			library_paths.add(new File(path, "steamapps/common"));
		}
		
		return library_paths;
	}
	
	/**
	 * Returns the folder that contains the game.
	 * @param name the name of the game to find
	 * @return the folder that contains the game or {@code null} if the game was not found 
	 */
	public static File findGamePath(String name) {
		List<File> library_paths = getSteamLibraryPaths();
		for(File file : library_paths) {
			if(!file.exists()) continue;
			File[] games = file.listFiles();
			
			for(File game : games) {
				if(game.getName().equals(name)) {
					return game;
				}
			}
		}
		
		return null;
	}
}
