package sm.game;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;

import sm.objects.BodyList;
import sm.objects.Game;
import sm.sqlite.Sqlite;
import sm.world.World;

public class SaveFile implements AutoCloseable {
	private final Sqlite sqlite;
	private final Game game;
	private final BodyList bodyList;
	
	private SaveFile(File file) throws SQLException {
		this.sqlite = new Sqlite(file);
		
		game = new Game(sqlite);
		bodyList= new BodyList(sqlite);
	}
	
	public Game getGame() {
		return game;
	}
	
	public BodyList getBodyList() {
		return bodyList;
	}
	
	public void close() throws SQLException {
		sqlite.close();
	}
	
	public static final SaveFile loadSaveFile(File file) {
		try {
			return new SaveFile(file);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public static final SaveFile loadSaveFile(String path) {
		return loadSaveFile(new File(path));
	}
	
	public static final SaveFile loadSaveFile(URL url) {
		try {
			return new SaveFile(new File(url.toURI()));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static final SaveFile loadLocalSaveFile(String name) {
		return loadSaveFile(World.class.getResource("/world/" + name));
	}
}
