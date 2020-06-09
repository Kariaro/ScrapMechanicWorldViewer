package sm.game;

import java.io.File;
import java.sql.SQLException;

import sm.objects.BodyList;
import sm.objects.Game;
import sm.objects.ScriptData;
import sm.sqlite.Sqlite;

/**
 * Should load a '.db' file and give us access to read data from it.
 * 
 * @author HardCoded
 */
public class SaveFile implements AutoCloseable {
	private final Sqlite sqlite;
	private final Game game;
	private final BodyList bodyList;
	private final ScriptData scriptData;
	
	private SaveFile(File file) throws SQLException {
		this.sqlite = new Sqlite(file);
		
		game = new Game(sqlite);
		bodyList = new BodyList(sqlite);
		scriptData = new ScriptData(sqlite);
		scriptData.test();
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
}
