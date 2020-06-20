package sm.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {
	public static byte[] readStreamBytes(InputStream stream) {
		if(stream == null) return new byte[0];
		
		try(DataInputStream ds = new DataInputStream(stream)) {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			byte[] buffer = new byte[2048];
			int readBytes = 0;
			
			while((readBytes = ds.read(buffer)) != -1) {
				bs.write(buffer, 0, readBytes);
			}
			
			return bs.toByteArray();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return new byte[0];
	}
	
	public static byte[] readFileBytes(File file) {
		try {
			return readStreamBytes(new FileInputStream(file));
		} catch(IOException e) {
			e.printStackTrace();
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
