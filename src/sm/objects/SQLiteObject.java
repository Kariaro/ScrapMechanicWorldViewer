package sm.objects;

import java.util.logging.Logger;

import sm.sqlite.Sqlite;

public class SQLiteObject {
	public final Logger LOGGER;
	protected final Sqlite sqlite;
	protected final String name;
	
	protected SQLiteObject(Sqlite sqlite) {
		this(sqlite, null);
	}
	
	protected SQLiteObject(Sqlite sqlite, String name) {
		this.sqlite = sqlite;
		this.name = name;
		LOGGER = Logger.getLogger(this.getClass().getName());
	}
	
	protected Object getField(String field) {
		return getField(name, field);
	}
	
	protected Object getField(String name, String field) {
		if(name == null) throw new UnsupportedOperationException();
		return sqlite.executeSingle("SELECT " + field + " FROM " + name);
	}
	
	protected boolean setField(String field, Object value) {
		return setField(name, field, value);
	}
	
	protected boolean setField(String name, String field, Object value) {
		if(name == null) throw new UnsupportedOperationException();
		return sqlite.executeUpdate("UPDATE " + name + " SET " + field + " = " + value);
	}
}
