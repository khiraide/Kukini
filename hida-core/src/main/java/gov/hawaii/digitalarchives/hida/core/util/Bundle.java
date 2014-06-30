
package gov.hawaii.digitalarchives.hida.core.util;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * For storing multiple, (and possibly) differing bits of information.  This
 * class is largely based on the class of the same name from the Android 
 * Project.
 * 
 * @author Dongie Agnir
 *
 */
public class Bundle {
	//String -> Object mappings
	private final HashMap<String, Object> map;
	
	/**
	 * Default constructor.
	 */
	public Bundle() {
		map = new LinkedHashMap<String, Object>();
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param rhs The Bundle to copy.
	 */
	public Bundle(final Bundle rhs) {
		map = new LinkedHashMap<String, Object>(rhs.map);
	}
	
	/**
	 * Removes all of the contents of the Bundle.
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * Retrieve a double from the bag.
	 * 
	 * @param key The key that identifies the double.
	 * @param defaultVal If {@code key} does not identify a valid double, this
	 * value is returned instead.
	 * 
	 * @return The double value identified by {@code key} if it exists,
	 * {@code defaultVal} otherwise.
	 */
	public double getDouble(String key, double defaultVal) {
		Object o = map.get(key);
		
		if (o == null) {
			return defaultVal;
		}
		
		try {
			return (Double) o;
		} catch (ClassCastException e) {
			return defaultVal;
		}
	}
	
	/**
	 * Retrieve a float value from the bag.
	 * 
	 * @param key The key that identifies the float.
	 * @param defaultVal If {@code key} does not identify a valid float, this
	 * value will be returned instead.
	 * 
	 * @return The float value identified by {@code key} if it exists, 
	 * {@code defaultVal} otherwise.
	 */
	public float getFloat(String key, float defaultVal) {
		Object o = map.get(key);
		
		if (o == null) {
			return defaultVal;
		}
		
		try {
			return (Float) o;
		} catch (ClassCastException e) {
			return defaultVal;
		}
	}
	
	/**
	 * Retrieve an integer from the bundle.
	 * 
	 * @param key The key that identifies the integer.
	 * @param defaultVal If {@code key} does not identify a valid integer,
	 * this value is returned instead.
	 * 
	 * @return The integer identified by {@code key} if it exists,
	 * {@code defaultVal} otherwise.
	 */
	public int getInteger(String key, int defaultVal) {
		Object o = map.get(key);
		
		if (o == null) {
			return defaultVal;
		}
		
		try {
			return (Integer) o;
		} catch (ClassCastException e) {
			return defaultVal;
		}
	}
	
	/**
	 * Retrieve a long from the bundle.
	 * 
	 * @param key The key that identifies the long.
	 * @param defaultVal If {@code key} does not identify a valid long,
	 * this value is returned instead.
	 * 
	 * @return The long identified by {@code key} if it exists,
	 * {@code defaultVal} otherwise.
	 */
	public long getLong(String key, long defaultVal) {
		Object o = map.get(key);
		
		if (o == null) {
			return defaultVal;
		}

		try {
			return (Long) o;
		} catch (ClassCastException e) {
			return defaultVal;
		}
	}
	
	/**
	 * Retrieve a string from the bag.
	 * 
	 * @param key The key that identifies the string.
	 * 
	 * @return The string if {@code key} identifies a valid string object.
	 * {@code null} otherwise.
	 */
	public String getString(String key) {
		Object o = map.get(key);
		
		if (o == null) {
			return null;
		}
		
		try {
			return (String) o;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	/**
	 * @return {@code true} if the Bundle is empty, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	/**
	 * Add a {@code double} to the bundle.
	 * 
	 * @param key The key to identify the {@code double}.
	 * @param d The double to put into the bundle.
	 */
	public void putDouble(String key, double d) {
		map.put(key, Double.valueOf(d));
	}
	
	/**
	 * Add a {@code float} to the bundle.
	 * 
	 * @param key They key to identify the {@code float}.
	 * @param f The float to put into the Bundle.
	 */
	public void putFloat(String key, float f) {
		map.put(key, Float.valueOf(f));
	}
	
	/**
	 * Add an {@code integer} to the bundle.
	 * 
	 * @param key The key to identify the {@code integer}.
	 * @param i The integer to put into the bundle.
	 */
	public void putInteger(String key, int i) {
		map.put(key, Integer.valueOf(i));
	}
	
	/**
	 * Add a {@code long} to the bundle.
	 * 
	 * @param key The key to identify the {@code long}.
	 * @param l The long to put into the bundle.
	 */
	public void putLong(String key, long l) {
		map.put(key, Long.valueOf(l));
	}
	
	/**
	 * Add a string to the bundle.
	 * 
	 * @param key The key to identify the string.
	 * @param string The string to put into the Bundle.
	 */
	public void putString(String key, String string) {
		map.put(key, string);
	}
}
