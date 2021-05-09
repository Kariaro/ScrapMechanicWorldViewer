package com.hardcoded.util;

import java.util.UUID;

public class Util {
	public static String getString(byte[] data, int offset, int length, boolean bigEndian) {
		StringBuilder sb = new StringBuilder();
		sb.append(new String(data, offset, length));
		
		if(bigEndian) {
			return sb.toString();
		}
		
		return sb.reverse().toString();
	}
	
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
	
	public static void writeShort(byte[] bytes, int offset, int value, boolean bigEndian) {
		if(bigEndian) {
			bytes[offset] = (byte)(value & 0xff);
			bytes[offset + 1] = (byte)((value >> 8) & 0xff);
		} else {
			bytes[offset] = (byte)((value >> 8) & 0xff);
			bytes[offset + 1] = (byte)(value & 0xff);
		}
	}
	
	public static void writeInt(byte[] bytes, int offset, int value, boolean bigEndian) {
		if(bigEndian) {
			writeShort(bytes, offset, value & 0xffff, bigEndian);
			writeShort(bytes, offset + 2, (value >> 16) & 0xffff, bigEndian);
		} else {
			writeShort(bytes, offset, (value >> 16) & 0xffff, bigEndian);
			writeShort(bytes, offset + 2, value & 0xffff, bigEndian);
		}
	}
	
	public static void writeBytes(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length, boolean bigEndian) {
		if(bigEndian) {
			for(int i = 0; i < length; i++) {
				dst[dstOffset + i] = src[srcOffset + i];
			}
		} else {
			for(int i = 0; i < length; i++) {
				dst[dstOffset + i] = src[srcOffset + length - i - 1];
			}
		}
	}
}
