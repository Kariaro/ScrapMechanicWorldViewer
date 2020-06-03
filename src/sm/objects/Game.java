package sm.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sm.sqlite.Sqlite;
import sm.util.Util;

public class Game extends SQLiteObject {
	public Game(Sqlite sqlite) {
		super(sqlite, "Game");
	}
	
	public int getSaveGameVersion() {
		return (int)getField("savegameversion");
	}
	
	public int getFlags() {
		return (int)getField("flags");
	}
	
	public int getSeed() {
		return (int)getField("seed");
	}
	
	public int getGameTick() {
		return (int)getField("gametick");
	}
	

	public boolean setSaveGameVersion(int version) {
		return setField("savegameversion", version);
	}
	
	public boolean setFlags(int flags) {
		return setField("flags", flags);
	}

	public boolean setSeed(int seed) {
		return setField("seed", seed);
	}

	public boolean setGameTick(int gametick) {
		return setField("gametick", gametick);
	}
	
	
	public int getNumMods() {
		return getNumMods(getModsBlob());
	}
	
	private int getNumMods(byte[] bytes) {
		return Util.getInt(bytes, 0, true);
	}
	
	private byte[] getModsBlob() {
		return (byte[])getField("mods");
	}
	
	public List<Mod> getMods() {
		List<Mod> mods = new ArrayList<Mod>();
		
		try {
			byte[] bytes = getModsBlob();
			int length = getNumMods(bytes);
			
			for(int i = 0; i < length; i++) {
				mods.add(new Mod(sqlite, bytes, 4 + i * 24));
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return mods;
	}
	
	public class Mod extends SQLiteObject {
		public final UUID localId;
		public final int fileId;
		private Mod(Sqlite sqlite, byte[] bytes, int offset) {
			super(sqlite);
			
			fileId = Util.getInt(bytes, offset + 4, true);			
			localId = Util.getUUID(bytes, offset + 8, false);
		}
		
		public UUID getLocalId() {
			return localId;
		}
		
		public int getFileId() {
			return fileId;
		}
		
		@Override
		public int hashCode() {
			return fileId;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Mod)) return false;
			return obj.hashCode() == fileId;
		}
	}
}