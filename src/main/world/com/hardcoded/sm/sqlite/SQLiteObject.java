package com.hardcoded.sm.sqlite;

import com.hardcoded.logger.Log;

/**
 * An abstract implementation of a SQLiteObject inside the game.
 * 
 * @author HardCoded
 * @since v0.1
 */
public abstract class SQLiteObject {
	protected static final Log LOGGER = Log.getLogger();
	
	public final SQLite sqlite;
	public final String name;
	
	protected SQLiteObject(SQLite sqlite) {
		this(sqlite, null);
	}
	
	public SQLiteObject(SQLite sqlite, String name) {
		this.sqlite = sqlite;
		this.name = name;
	}
	
	public Object getField(String field) {
		return getField(name, field);
	}
	
	public Object getField(String name, String field) {
		if(name == null) throw new UnsupportedOperationException();
		return sqlite.executeSingle("SELECT " + field + " FROM " + name);
	}
	
	public boolean setField(String field, Object value) {
		return setField(name, field, value);
	}
	
	public boolean setField(String name, String field, Object value) {
		if(name == null) throw new UnsupportedOperationException();
		return sqlite.executeUpdate("UPDATE " + name + " SET " + field + " = " + value);
	}
}
