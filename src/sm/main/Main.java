package sm.main;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import sm.lwjgl.LwjglWorldViewer;
import sm.objects.Game;
import sm.objects.Game.Mod;
import sm.world.World;

public class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	static {
		try {
			// The ConsoleHandler is initialized once inside LogManager.RootLogger
			// if we change Sytem.err to System.out when the ConsoleHandler is created
			// we change it's output stream to System.out.
			
			PrintStream error_stream = System.err;
			System.setErr(System.out);
			
			Locale.setDefault(Locale.ENGLISH);
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream((
				"handlers=java.util.logging.ConsoleHandler\r\n" + 
				".level=INFO\r\n" + 
				"java.util.logging.ConsoleHandler.level=ALL\r\n" + 
				"java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\r\n" + 
				"java.util.logging.SimpleFormatter.format=%1$tF %1$tT [%4$s] %3$s - %5$s%n"
			).getBytes()));
			
			// Interact with the RootLogger so that it calls LogManager.initializeGlobalHandlers();
			LogManager.getLogManager().getLogger("").removeHandler(null);
			
			// Switch back to normal error stream
			System.setErr(error_stream);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		LwjglWorldViewer viewer = new LwjglWorldViewer();
		viewer.start();
		//new Main();
	}
	
	public Main() {
		World world;
		try {
			world = World.loadWorld("C:\\Users\\Admin\\AppData\\Roaming\\Axolot Games\\Scrap Mechanic\\User\\User_76561198251506208\\Save\\TestingSQLite.db");
		} catch(Exception e) {
			LOGGER.severe("Failed to load world file");
			e.printStackTrace();
			return;
		}
		
		Game game = world.getGame();
		LOGGER.log(Level.INFO, "Version: {0,number,#}", game.getSaveGameVersion());
		LOGGER.log(Level.INFO, "Flags: {0,number,#}", game.getFlags());
		LOGGER.log(Level.INFO, "Seed: {0,number,#}", game.getSeed());
		LOGGER.log(Level.INFO, "Tick: {0,number,#}", game.getGameTick());
		
		LOGGER.log(Level.INFO, "Mods:");
		for(Mod mod : game.getMods()) {
			LOGGER.log(Level.INFO, "    Mod: {0}", mod);
			LOGGER.log(Level.INFO, "      localId: {0}", mod.localId);
			LOGGER.log(Level.INFO, "      fileId : {0,number,#}", mod.fileId);
		}
		/*
		LOGGER.log(Level.INFO, "");
		LOGGER.log(Level.INFO, "RigidBodies");
		List<RigidBody> bodies = world.getSaveFile().getBodyList().getAllRigidBodies();
		for(RigidBody body : bodies) {
			LOGGER.log(Level.INFO, "    Body: {0}", body);
			LOGGER.log(Level.INFO, "      Shapes: {0}", body.shapes);
			LOGGER.log(Level.INFO, "      Bounds: {0}", body.bounds);
			LOGGER.log(Level.INFO, "");
		}
		*/
	}
}
