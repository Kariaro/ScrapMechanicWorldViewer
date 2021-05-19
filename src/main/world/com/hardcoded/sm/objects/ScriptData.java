package com.hardcoded.sm.objects;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hardcoded.data.Memory;
import com.hardcoded.lua.LuaDeserializer;
import com.hardcoded.sm.lua.Data;
import com.hardcoded.sm.sqlite.SQLite;
import com.hardcoded.sm.sqlite.SQLiteObject;
import com.hardcoded.util.Util;

/**
 * @author HardCoded
 * @since v0.1
 */
public class ScriptData extends SQLiteObject {
	public ScriptData(SQLite sqlite) {
		super(sqlite);
	}
	
	public void test() throws SQLException {
		ResultSet set = sqlite.execute("SELECT data FROM ScriptData WHERE channel = 1");
		
		if(set.getFetchSize() < 1) return;
		byte[] data = set.getBytes("data");
		
		StringBuilder sb = new StringBuilder();
		int a = 1;
		for(byte b : data) {
			sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
		}
		String nows = sb.toString();
		
		int id_0_4 = Util.getInt(data, 0, true);
		int worldId_4_2 = Short.toUnsignedInt(Util.getShort(data, 4, true));
		int channel_6_1 = Byte.toUnsignedInt(data[6]);
		int flags_7_1 = Byte.toUnsignedInt(data[7]);
		int unk_8_4 = Util.getInt(data, 8, true);
		
		System.out.println(nows);
		System.out.println("id_0_4: " + id_0_4);
		System.out.println("worldId_4_2: " + worldId_4_2);
		System.out.println("channel_6_1: " + channel_6_1);
		System.out.println("flags_7_1: " + flags_7_1);
		System.out.println("unk_8_4: " + unk_8_4);
		System.out.println();
	}
	
	public void test2() throws SQLException {
		loadTileData();

		ResultSet set = sqlite.execute("SELECT data FROM ScriptData WHERE flags = 7");
		set = sqlite.execute("SELECT data FROM ScriptData WHERE worldId = 65534");
		set = sqlite.execute("SELECT data FROM ScriptData WHERE id = 49 AND channel = 48 AND worldId = 1 AND flags = 3");
		
		if(set.isClosed()) return;
		byte[] data = set.getBytes(1);
		Memory mem = new Memory(data);
		// System.out.println(StringUtils.getHexString(data, 4096, 64));
		
		
		int id_0_4 = Util.getInt(data, 0, true);
		int worldId_4_2 = Short.toUnsignedInt(Util.getShort(data, 4, true));
		int channel_6_1 = Byte.toUnsignedInt(data[6]);
		int flags_7_1 = Byte.toUnsignedInt(data[7]);
		int size_8_4 = Util.getInt(data, 8, true);
		mem.move(12); // Skip header
		
		System.out.println("id_0_4: " + id_0_4);
		System.out.println("worldId_4_2: " + worldId_4_2);
		System.out.println("channel_6_1: " + channel_6_1);
		System.out.println("flags_7_1: " + flags_7_1);
		System.out.println("size_8_4: " + size_8_4);
		System.out.println();
		
		//System.exit(0);
	}
	
	private void loadTileData() throws SQLException {
		ResultSet set = sqlite.execute("SELECT data FROM ScriptData WHERE channel = 1");
		if(set.isClosed()) return;
		
		byte[] data = set.getBytes(1);
		Memory mem = new Memory(data);
		
//		int id_0_4 = Util.getInt(data, 0, true);
//		int worldId_4_2 = Short.toUnsignedInt(Util.getShort(data, 4, true));
//		int channel_6_1 = Byte.toUnsignedInt(data[6]);
//		int flags_7_1 = Byte.toUnsignedInt(data[7]);
		int size_8_4 = Util.getInt(data, 8, true);
		mem.move(12); // Skip header
		
//		System.out.println("id_0_4: " + id_0_4);
//		System.out.println("worldId_4_2: " + worldId_4_2);
//		System.out.println("channel_6_1: " + channel_6_1);
//		System.out.println("flags_7_1: " + flags_7_1);
//		System.out.println("size_8_4: " + size_8_4);
//		System.out.println();
		
		Data obj = new Data(LuaDeserializer.Deserialize(mem, size_8_4));
		
		{
			TileData.setWorldTileIds(obj);
			
//			System.out.println("{");
//			for(Object key : obj.keySet()) {
//				System.out.printf("    key=(%s)\n", key);
//			}
//			System.out.println("}");
		}
	}
}
