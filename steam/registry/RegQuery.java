package registry;

import sm.util.FileUtils;

public class RegQuery {
	private static final String QUERY = "REG QUERY ";
	
	/**
	 * Reads a value from the registry with the selected path and key.
	 * @param path
	 * @param key
	 * @return
	 */
	public static String readRegistryValue(String path, String key) {
		try {
			return executeRegistry(path, key);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String executeRegistry(String path, String key) throws Exception {
		Process process = Runtime.getRuntime().exec(QUERY + path + " /v " + key);
		byte[] bytes = FileUtils.readStreamBytes(process.getInputStream());
		process.waitFor();
		
		if(bytes.length < 1) return null;
		
		String value = new String(bytes);
		value = value.substring(2);
		value = value.substring(value.indexOf('\n') + 1).trim();
		value = value.substring(value.indexOf(' ')).trim();
		value = value.substring(value.indexOf(' ')).trim();
		return value;
	}
}
