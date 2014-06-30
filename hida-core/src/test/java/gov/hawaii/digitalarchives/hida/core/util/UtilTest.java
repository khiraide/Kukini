
package gov.hawaii.digitalarchives.hida.core.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test the {@link Util} class.
 * 
 * @author Dongie Agnir
 */
public class UtilTest {
	
	@BeforeTest
	public void setup() {
		
	}
	
	/**
	 * Test {@link Util#byteArrayToString(byte[])} function.
	 */
	@Test
	public void testByteToStr() {
		//Passing in null array should give us null...
		assertNull(Util.byteArrayToString(null));
		
		//Passing in empty erray should give us null.
		assertNull(Util.byteArrayToString(new byte[]{}));
		
		//Should convert to ""0xFFFE11"
		byte[] array1 = new byte[]{(byte)0xff, (byte)0xfe, (byte)0x11};
		assertEquals(Util.byteArrayToString(array1), "0xFFFE11");
	}
}
