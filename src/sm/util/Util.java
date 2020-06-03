package sm.util;

import java.util.UUID;

public class Util {
	public static short getShort(byte[] bytes, int offset, boolean bigEndian) {
		if(bigEndian) {
			return (short)(((bytes[0 + offset] & 0xff) << 8) |
				   (bytes[1 + offset] & 0xff));
		} else {
			return (short)((bytes[0 + offset] & 0xff) |
				   ((bytes[1 + offset] & 0xff) << 8));
		}
	}
	
	public static int getInt(byte[] bytes, int offset, boolean bigEndian) {
		if(bigEndian) {
			return ((bytes[0 + offset] & 0xff) << 24) |
				   ((bytes[1 + offset] & 0xff) << 16) |
				   ((bytes[2 + offset] & 0xff) << 8) |
				   (bytes[3 + offset] & 0xff);
		} else {
			return (bytes[0 + offset] & 0xff) |
				   ((bytes[1 + offset] & 0xff) << 8) |
				   ((bytes[2 + offset] & 0xff) << 16) |
				   ((bytes[3 + offset] & 0xff) << 24);
		}
	}
	
	public static int get3Int(byte[] bytes, int offset, boolean bigEndian) {
		if(bigEndian) {
			return ((bytes[0 + offset] & 0xff) << 16) |
				   ((bytes[1 + offset] & 0xff) << 8) |
				   (bytes[2 + offset] & 0xff);
		} else {
			return (bytes[0 + offset] & 0xff) |
				   ((bytes[1 + offset] & 0xff) << 8) |
				   ((bytes[2 + offset] & 0xff) << 16);
		}
	}
	
	public static float getFloat(byte[] bytes, int offset, boolean bigEndian) {
		return Float.intBitsToFloat(getInt(bytes, offset, bigEndian));
	}
	
	public static double getDouble(byte[] bytes, int offset, boolean bigEndian) {
		return Double.longBitsToDouble(getLong(bytes, offset, bigEndian));
	}
	
	public static long getLong(byte[] bytes, int offset, boolean bigEndian) {
		long a = Integer.toUnsignedLong(getInt(bytes, offset + 0, bigEndian));
		long b = Integer.toUnsignedLong(getInt(bytes, offset + 4, bigEndian));
		
		if(bigEndian) {
			return (a << 32L) | b;
		} else {
			return (b << 32L) | a;
		}
	}
	
	public static UUID getUUID(byte[] bytes, int offset, boolean bigEndian) {
		long low = getLong(bytes, offset, bigEndian);
		long high = getLong(bytes, offset + 8, bigEndian);
		if(bigEndian) {
			return new UUID(low, high);
		} else {
			return new UUID(high, low);
		}
	}
	
}
