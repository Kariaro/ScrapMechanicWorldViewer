package com.hardcoded.util;

/**
 * This class provides safe methods for converting objects into types.
 * If the type is not castable it will not return an error but a default type of that object.
 * 
 * @author HardCoded
 * @since v0.1
 */
public class ValueUtils {
	/**
	 * Returns the integer value for the specified {@code obj}.
	 * If the objects wasn't a number the return value will be {@code 0}.
	 * @param obj the object to convert
	 * @return the integer value or {@code 0} if the specified {@code obj} wasn't a number
	 */
	public static int toInt(Object obj) {
		return toInt(obj, 0);
	}
	
	public static int toInt(Object obj, int def) {
		if(obj instanceof Number) {
			return ((Number)obj).intValue();
		}
		
		return def;
	}
	
	
	/**
	 * Returns the float value for the specified {@code obj}.
	 * If the objects wasn't a number the return value will be {@code 0.0f}.
	 * @param obj the object to convert
	 * @return the float value or {@code 0.0f} if the specified {@code obj} wasn't a number
	 */
	public static float toFloat(Object obj) {
		return toFloat(obj, 0.0f);
	}
	
	public static float toFloat(Object obj, float def) {
		if(obj instanceof Number) {
			return ((Number)obj).floatValue();
		}
		
		return def;
	}
	
	
	/**
	 * Returns the double value for the specified {@code obj}.
	 * If the objects wasn't a number the return value will be {@code 0.0d}.
	 * @param obj the object to convert
	 * @return the double value or {@code 0.0d} if the specified {@code obj} wasn't a number
	 */
	public static double toDouble(Object obj) {
		return toDouble(obj, 0.0d);
	}
	
	public static double toDouble(Object obj, double def) {
		if(obj instanceof Number) {
			return ((Number)obj).doubleValue();
		}
		
		return def;
	}
	
	
	/**
	 * Returns the string value for the specified {@code obj}.
	 * @param obj the object to convert
	 * @return the string value or {@code ""} or the specified {@code obj} was {@code null}
	 */
	public static String toString(Object obj) {
		return toString(obj, "");
	}
	
	public static String toString(Object obj, String def) {
		if(obj == null) return def;
		if(obj instanceof String) {
			return (String)obj;
		}
		
		return obj.toString();
	}
	
	
	/**
	 * Returns the boolean value for the specified {@code obj}.
	 * @param obj the object to convert
	 * @return the boolean value or {@code false} or the specified {@code obj} was {@code null}
	 */
	public static boolean toBoolean(Object obj) {
		return toBoolean(obj, false);
	}
	
	public static boolean toBoolean(Object obj, boolean def) {
		if(obj instanceof Boolean) {
			return (Boolean)obj;
		}
		
		return def;
	}
}
