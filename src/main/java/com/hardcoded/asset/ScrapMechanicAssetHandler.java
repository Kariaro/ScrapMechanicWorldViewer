package com.hardcoded.asset;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import com.hardcoded.logger.Log;
import com.hardcoded.logger.Log.Level;
import com.hardcoded.world.types.*;

import valve.Steam;

/**
 * This class is used to load and read all assets in the game ScrapMechanic.
 * 
 * @author HardCoded
 * @since v0.1
 */
public final class ScrapMechanicAssetHandler {
	private static final Log LOGGER = Log.getLogger();
	
	private static final ScrapMechanicAssetHandler INSTANCE;
	
	public static final String $CHALLENGE_DATA;
	public static final String $SURVIVAL_DATA;
	public static final String $GAME_DATA;
	
	
	/**
	 * This field is only used internally and is not references inside the game ScrapMechanic.
	 */
	public static final String $USER_DATA;
	
	static {
		INSTANCE = new ScrapMechanicAssetHandler();
		
		Log.setLogLevel(Level.ALL);
		LOGGER.info("Trying to find the game 'Scrap Mechanic'");
		File game_path = Steam.findGamePath("Scrap Mechanic");
		
		LOGGER.info("Found game path: %s", game_path);
		
		$CHALLENGE_DATA = new File(game_path, "ChallengeData").getAbsolutePath();
		$SURVIVAL_DATA = new File(game_path, "Survival").getAbsolutePath();
		$GAME_DATA = new File(game_path, "Data").getAbsolutePath();
		
		// TODO: ?????
		String appdata_path = System.getenv("APPDATA");
		File sm_userpath = new File(appdata_path, "Axolot Games/Scrap Mechanic/User");
		File[] sm_users = sm_userpath.listFiles();
		
		if(sm_users.length < 1) {
			throw new RuntimeException("No steam profile found");
		}
		
		// Selected the first steam profile.
		$USER_DATA = sm_users[0].getAbsolutePath();
		
		try {
			LOGGER.info("Trying to load assets");
			
			ScrapMechanicAssetHandler.setBasePath(game_path.getAbsolutePath());
			ScrapMechanicAssetHandler.INSTANCE.load();
		} catch(FileNotFoundException e) {
			LOGGER.throwing(e);
		} catch(Exception e) {
			LOGGER.throwing(e);
		}
	}
	
	protected final Map<UUID, SMClutter> clutters;
	protected final Map<UUID, SMBlock> blocks;
	protected final Map<UUID, SMAsset> assets;
	protected final Map<UUID, SMPart> parts;
	
	private File basePath;
	private File gameDataPath;
	private File challengeDataPath;
	private File survivalDataPath;
	//private File modsDataPath;
	
	private final ScrapMechanicLoader loader;
	
	private ScrapMechanicAssetHandler() {
		loader = new ScrapMechanicLoader(this);
		
		clutters = new HashMap<>();
		blocks = new HashMap<>();
		assets = new HashMap<>();
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
		
		
		INSTANCE.basePath = file;
		INSTANCE.challengeDataPath = new File(INSTANCE.basePath, "ChallengeData");
		INSTANCE.survivalDataPath = new File(INSTANCE.basePath, "Survival");
		INSTANCE.gameDataPath = new File(INSTANCE.basePath, "Data");
	}
	
	public static String resolvePath(String path) {
		if(path == null) {
			LOGGER.warn("ScrapMechanicAssetHandler.resolvePath was called with a null value");
			return "";
		}
		
		if(path.startsWith("$GAME_DATA")) {
			return path.replace("$GAME_DATA", INSTANCE.gameDataPath.getAbsolutePath());
		}
		
		if(path.startsWith("$CHALLENGE_DATA")) {
			return path.replace("$CHALLENGE_DATA", INSTANCE.challengeDataPath.getAbsolutePath());
		}
		
		if(path.startsWith("$SURVIVAL_DATA")) {
			return path.replace("$SURVIVAL_DATA", INSTANCE.survivalDataPath.getAbsolutePath());
		}
		
//		if(path.startsWith("$MOD_DATA")) {
//			File dir = new File($USER_DATA, "Mods/SQLiteTesting/");
//			
//			String resolve = path.substring(9);
//			return new File(dir, resolve).getAbsolutePath();
//			//throw new UnsupportedOperationException("The mod path cannot be resolved without knowing the mod folder!");
//		}
		
		LOGGER.warn("Path was specified without a relative path. '%s'", path);
		return path;
	}
	
	private void load() throws Exception {
		loader.load();
	}
	
	/**
	 * Get the part with the specified {@code uuid}.
	 * @param uuid the uuid of the part object
	 * @return the part object or {@code null} if that part didn't exist
	 */
	public static SMPart getPart(UUID uuid) {
		return INSTANCE.parts.get(uuid);
	}
	
	/**
	 * Get the block with the specified {@code uuid}.
	 * @param uuid the uuid of the block object
	 * @return the block object or {@code null} if that block didn't exist
	 */
	public static SMBlock getBlock(UUID uuid) {
		return INSTANCE.blocks.get(uuid);
	}
	
	/**
	 * Get the asset with the specified {@code uuid}.
	 * @param uuid the uuid of the asset object
	 * @return the asset object or {@code null} if that part didn't exist
	 */
	public static SMAsset getAsset(UUID uuid) {
		return INSTANCE.assets.get(uuid);
	}
	
	/**
	 * Get the clutter with the specified {@code uuid}.
	 * @param uuid the uuid of the clutter object
	 * @return the clutter object or {@code null} if that clutter didn't exist
	 */
	public static SMClutter getClutter(UUID uuid) {
		return INSTANCE.clutters.get(uuid);
	}
	
	
	public static Collection<SMPart> getAllParts() {
		return Collections.unmodifiableCollection(INSTANCE.parts.values());
	}
	
	public static Collection<SMAsset> getAllAssets() {
		return Collections.unmodifiableCollection(INSTANCE.assets.values());
	}
	
	public static Collection<SMBlock> getAllBlocks() {
		return Collections.unmodifiableCollection(INSTANCE.blocks.values());
	}
	
	public static Collection<SMClutter> getAllClutters() {
		return Collections.unmodifiableCollection(INSTANCE.clutters.values());
	}
}
