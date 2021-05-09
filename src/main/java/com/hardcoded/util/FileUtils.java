package com.hardcoded.util;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;

import com.hardcoded.logger.Log;

public class FileUtils {
	private static final Log LOGGER = Log.getLogger();
	
	public static byte[] readStreamBytes(InputStream stream) {
		try {
			return stream.readAllBytes();
		} catch(IOException e) {
			LOGGER.throwing(e);
		}
		
		return new byte[0];
	}
	
	public static byte[] readFileBytes(File file) {
		try {
			return readStreamBytes(new FileInputStream(file));
		} catch(IOException e) {
			LOGGER.throwing(e);
		}
		
		return new byte[0];
	}
	
	public static byte[] readFileBytes(String path) { return readFileBytes(new File(path)); }
	public static String readFile(File file) { return new String(readFileBytes(file)); }
	public static String readFile(String path) { return new String(readFileBytes(new File(path))); }
	public static String readFile(File path, String name) { return new String(readFileBytes(new File(path, name))); }
	public static String readStream(InputStream stream) { return new String(readStreamBytes(stream)); }
	
	public static void copy(File source, File target, CopyOption... options) throws IOException {
		Files.copy(source.toPath(), target.toPath(), options);
	}
}
