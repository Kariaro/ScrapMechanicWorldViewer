package com.hardcoded.sm.api;

import java.util.List;
import java.util.UUID;

import com.hardcoded.util.Util;

/**
 * A world header interface.
 * 
 * @author HardCoded
 * @since v0.1
 */
public interface WorldHeader {
	/**
	 * Returns the version of this world.
	 * @return the version of this world
	 */
	int getVersion();
	
	/**
	 * Returns the flags of this world.
	 * @return the flags of this world
	 */
	int getFlags();
	
	/**
	 * Returns the seed of this world.
	 * @return the seed of this world
	 */
	int getSeed();
	
	/**
	 * Returns the current tick of this world.
	 * @return the current tick of this world
	 */
	int getGameTick();
	
	
	/**
	 * Change the version of this world.
	 * @param version the new version
	 * @return {@code true} if the value was applied successfully
	 */
	boolean setSaveGameVersion(int version);
	
	/**
	 * Change the flags of this world.
	 * @param flags the new flags
	 * @return {@code true} if the value was applied successfully
	 */
	boolean setFlags(int flags);

	/**
	 * Change the current seed of this world.
	 * @param seed the new seed
	 * @return {@code true} if the value was applied successfully
	 */
	boolean setSeed(int seed);

	/**
	 * Change the current tick of this world.
	 * @param tick the new tick
	 * @return {@code true} if the value was applied successfully
	 */
	boolean setGameTick(int tick);
	
	/**
	 * Returns the number of mods used.
	 * @return the number of mods used
	 */
	int getModsCount();
	
	/**
	 * Returns a list of all mods used in this world.
	 * @return a list of all mods used in this world
	 */
	List<Mod> getMods();
	
	
	/**
	 * A simple implementation of a mod header.
	 * 
	 * @author HardCoded
	 * @since v0.1
	 */
	public class Mod {
		public final UUID localId;
		public final int fileId;
		
		public Mod(byte[] bytes, int offset) {
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
			if(obj instanceof Mod) return obj.hashCode() == fileId;
			return false;
		}
	}
}
