
package gov.hawaii.digitalarchives.hida.core.util;

/**
 * This is a simple utility class that pulls together useful functions.
 * 
 * @author Dongie Agnir
 */
public class Util {
	
	/**
	 * Convert a byte array to a hex String representation.
	 * 
	 * @param array The array of bytes to convert to a hex string.
	 * @return The string representation of the byte array.
	 */
	public static String byteArrayToString(final byte[] array) {
		//hex 0-F
		final String hexChars = "0123456789ABCDEF";
		
		if ((array == null) || array.length == 0) {
			return null;
		}
		
		StringBuilder hexStrBuilder = new StringBuilder("0x");
		
		for (int i = 0; i < array.length; ++i) {
			//Convert the upper 4 bits (bit 5-8) first
			hexStrBuilder.append(hexChars.charAt((array[i] >> 4) & 0x0f));
			//Convert the lower 4 bits (bits 0-4)
			hexStrBuilder.append(hexChars.charAt(array[i] & 0x0F));
		}

		return hexStrBuilder.toString();
	}
}
