package sm.util;

public final class StringUtils {
	public static String removeComments(String string) {
		return string.replaceAll("//.*?[\r\n]+", "\r\n");
	}
}
