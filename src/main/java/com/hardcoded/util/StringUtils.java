package com.hardcoded.util;

public final class StringUtils {
	public static String removeComments(String string) {
		return string.replaceAll("//.*?[\r\n]+", "\r\n");
	}
	
	public static String getHexString(byte[] bytes, int maxLength, int lineLength) {
		StringBuilder sb = new StringBuilder();
		int a = 1;
		for(int i = 0; i < Math.min(bytes.length, maxLength); i++) {
			sb.append(String.format("%02x", bytes[i]));
			if((a ++) % lineLength == 0) sb.append('\n');
		}
		
		return sb.toString();
	}
	
	public static String getHexString(int[] ints, int maxLength, int lineLength) {
		StringBuilder sb = new StringBuilder();
		int a = 1;
		for(int i = 0; i < Math.min(ints.length, maxLength); i++) {
			sb.append((ints[i] > 0) ? '#':'.').append(" ");
			if((a ++) % lineLength == 0) sb.append('\n');
		}
		
		return sb.toString();
	}
}
