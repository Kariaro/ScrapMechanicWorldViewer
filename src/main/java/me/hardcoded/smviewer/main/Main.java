package me.hardcoded.smviewer.main;

import me.hardcoded.smviewer.game.World;
import me.hardcoded.smreader.logger.Log;
import me.hardcoded.smviewer.lwjgl.LwjglWindowSetup;
import me.hardcoded.smviewer.sm.api.WorldHeader;

public class Main {
	private static final Log LOGGER = Log.getLogger();
	
	public static void main(String[] args) throws Exception {
		LwjglWindowSetup viewer = new LwjglWindowSetup();
		viewer.start();
		//new Main();
	}
	
	public Main() {
		World world;
		try {
			//world = World.loadWorldFromAppdata("TestingSQLite.db");
			world = World.loadWorldFromAppdata("Survival/Amazing World.db");
		} catch (Exception e) {
			LOGGER.error("Failed to load world file");
			LOGGER.throwing(e);
			return;
		}
		
		WorldHeader game = world.getWorldHeader();
		LOGGER.info("Version: %d", game.getVersion());
		LOGGER.info("Flags: %d", game.getFlags());
		LOGGER.info("Seed: %d", game.getSeed());
		LOGGER.info("Tick: %d", game.getGameTick());
		
		LOGGER.info("Mods:");
		for (WorldHeader.Mod mod : game.getMods()) {
			LOGGER.info("    Mod: %s", mod);
			LOGGER.info("      localId: %d", mod.localId);
			LOGGER.info("      fileId : %d", mod.fileId);
		}
	}
}
