package com.hardcoded.sm.sqlite;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.sqlite.JDBC;
import org.sqlite.jdbc4.JDBC4Connection;

import com.hardcoded.logger.Log;

/**
 * This class provides methods to read SQLite files.
 *  
 * @author HardCoded
 * @since v0.1
 * 
 * TODO: Maybe update how this class looks?
 */
public class SQLite implements AutoCloseable {
	private static final Log LOGGER = Log.getLogger();
	
	private final JDBC4Connection connection;
	private final Properties properties;
	
	public SQLite(File file) throws SQLException {
		properties = new Properties();
		connection = (JDBC4Connection)JDBC.createConnection("jdbc:sqlite:" + file.getAbsolutePath(), properties);
	}
	
	public Properties getConfig() {
		return properties;
	}
	
	public JDBC4Connection getConnection() {
		return connection;
	}
	
	public ResultSet execute(String sql) {
		try {
			Statement statement = connection.createStatement();
			return statement.executeQuery(sql);
		} catch(SQLException e) {
			LOGGER.throwing(e);
		}
		
		return null;
	}
	
	public Object executeSingle(String sql) {
		try {
			ResultSet set = execute(sql);
			if(set.next()) return set.getObject(1);
		} catch(SQLException e) {
			LOGGER.throwing(e);
		}
		
		return null;
	}
	
	public boolean executeUpdate(String sql) {
		try {
			Statement statement = connection.createStatement();
			return statement.execute(sql);
		} catch(SQLException e) {
			LOGGER.throwing(e);
		}
		
		return false;
	}
	
	public void close() throws SQLException {
		connection.close();
	}
}
