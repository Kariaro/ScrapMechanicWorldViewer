package com.hardcoded.sm.objects;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.sm.api.WorldHeader;
import com.hardcoded.sm.sqlite.SQLiteObject;
import com.hardcoded.sm.sqlite.SQLite;
import com.hardcoded.util.Util;

/**
 * An implementaiton of a world header.
 * 
 * <p>This class should not be used directly.
 * Use {@linkplain WorldHeader} instead.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class WorldHeaderImpl extends SQLiteObject implements WorldHeader {
	public WorldHeaderImpl(SQLite sqlite) {
		super(sqlite, "Game");
	}
	
	@Override
	public int getVersion() {
		return (int)getField("savegameversion");
	}
	
	@Override
	public int getFlags() {
		return (int)getField("flags");
	}
	
	@Override
	public int getSeed() {
		return (int)getField("seed");
	}
	
	@Override
	public int getGameTick() {
		return (int)getField("gametick");
	}
	
	@Override
	public boolean setSaveGameVersion(int version) {
		return setField("savegameversion", version);
	}
	
	@Override
	public boolean setFlags(int flags) {
		return setField("flags", flags);
	}
	
	@Override
	public boolean setSeed(int seed) {
		return setField("seed", seed);
	}
	
	@Override
	public boolean setGameTick(int gametick) {
		return setField("gametick", gametick);
	}
	
	@Override
	public int getModsCount() {
		return getNumMods(getModsBlob());
	}
	
	private int getNumMods(byte[] bytes) {
		return Util.getInt(bytes, 0, true);
	}
	
	private byte[] getModsBlob() {
		return (byte[])getField("mods");
	}
	
	@Override
	public List<Mod> getMods() {
		List<Mod> mods = new ArrayList<Mod>();
		
		try {
			byte[] bytes = getModsBlob();
			int length = getNumMods(bytes);
			
			for(int i = 0; i < length; i++) {
				mods.add(new Mod(bytes, 4 + i * 24));
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return mods;
	}
}