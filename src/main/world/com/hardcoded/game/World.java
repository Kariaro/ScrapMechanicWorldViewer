package com.hardcoded.game;

import java.io.File;
import java.sql.SQLException;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.logger.Log;
import com.hardcoded.sm.api.WorldHeader;
import com.hardcoded.sm.objects.BodyList;
import com.hardcoded.sm.objects.WorldHeaderImpl;
import com.hardcoded.sm.objects.ScriptData;
import com.hardcoded.sm.sqlite.SQLite;

/**
 * This world file contains all information about stuff inside the SQLite object.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class World implements AutoCloseable {
	private static final Log LOGGER = Log.getLogger();
	
	private final SQLite sqlite;
	private final File path;
	
	private WorldHeader game;
	private BodyList bodyList;
	private ScriptData scriptData;
	
	private World(File path) throws Exception {
		this.sqlite = new SQLite(path);
		this.path = path;
		
		game = new WorldHeaderImpl(sqlite);
		bodyList = new BodyList(sqlite);
		scriptData = new ScriptData(sqlite);
		scriptData.test();
		scriptData.test2();
	}
	
	
	/**
	 * Returns the path of this world.
	 * @return the path of this world
	 */
	public File getWorldPath() {
		return path;
	}
	
	/**
	 * Returns the header of this world.
	 * @return the header of this world
	 */
	public WorldHeader getWorldHeader() {
		return game;
	}
	
	/**
	 * Returns the body list of this world.
	 * @return the body list of this world
	 */
	public BodyList getBodyList() {
		return bodyList;
	}
	
	@Override
	public void close() throws SQLException {
		sqlite.close();
	}
	
	public static World loadWorld(String path) {
		return loadWorld(new File(path));
	}
	
	public static World loadWorldFromAppdata(String path) {
		return loadWorld(new File(ScrapMechanicAssetHandler.$USER_DATA, "Save/" + path));
	}
	
	public static World loadWorld(File path) {
		try {
			return new World(path);
		} catch(Exception e) {
			LOGGER.throwing(e);
		}
		
		return null;
	}
}
