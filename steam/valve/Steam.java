package valve;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import registry.RegQuery;

public final class Steam {
	private static final Logger LOGGER = Logger.getLogger(Steam.class.getName());
	
	private static List<File> LIBRARY_PATHS = new ArrayList<>();
	private static File STEAM_PATH;
	
	public static final void main(String[] args) {
		System.out.println("Steam: " + STEAM_PATH);
		System.out.println("Librares: " + LIBRARY_PATHS);
		
		
		File gamePath = Steam.findGamePath("Scrap Mechanic");
		System.out.println("Path: \"" + gamePath + "\"");
	}
	
	static {
		reload();
	}
	
	/**
	 * This function will try find the installPath of Steam and then
	 * get all the library paths.
	 * 
	 * @return true if it sucessfully found Steam
	 */
	public static boolean reload() {
		LOGGER.log(Level.FINE, "Loading Steam installation path");
		LIBRARY_PATHS.clear();
		
		String installPath = RegQuery.readRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Valve\\Steam", "InstallPath");
		if(installPath == null) {
			// 32 bit
			installPath = RegQuery.readRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Valve\\Steam", "InstallPath");
		}
		
		if(installPath == null) {
			LOGGER.log(Level.WARNING, "Could not find the steam installPath in the registry");
			return false;
		}
		
		return reload(new File(installPath));
	}
	
	/**
	 * Load all libraries with the Steam installPath
	 * 
	 * @param installPath the path of the Steam directory
	 * @return
	 */
	public static boolean reload(File installPath) {
		LOGGER.log(Level.FINE, "Loading all Library locations");
		STEAM_PATH = installPath;
		LIBRARY_PATHS.clear();
		
		// The default Steam path is also a default library location
		LIBRARY_PATHS.add(STEAM_PATH);
		
		
		File file = new File(STEAM_PATH, "steamapps/libraryfolders.vdf");
		
		// The user never changed the library path so the file never got created
		if(!file.exists()) {
			return true;
		}
		
		ValveData data = new ValveData(file).get("LibraryFolders");
		Set<String> names = data.getValueNames();
		for(String s : names) {
			if(!s.matches("[0-9]+")) continue;
			
			String path = data.getValue(s);

			LOGGER.log(Level.FINEST, "Adding library: \"{0}\"", path);
			LIBRARY_PATHS.add(new File(path));
		}
		
		return true;
	}
	
	public static File findGamePath(String name) {
		if(LIBRARY_PATHS.isEmpty()) {
			LOGGER.log(Level.WARNING, "No library paths found");
			return null;
		}
		
		for(File file : LIBRARY_PATHS) {
			File common = new File(file, "steamapps/common");
			
			if(!common.exists()) continue;
			File[] games = common.listFiles();
			
			for(File game : games) {
				if(game.getName().equals(name)) {
					return game;
				}
			}
		}
		
		return null;
	}
	
	public static File getInstallDirectory() {
		return STEAM_PATH;
	}
	
	public static List<File> getLibraryFolders() {
		return Collections.unmodifiableList(LIBRARY_PATHS);
	}
}
