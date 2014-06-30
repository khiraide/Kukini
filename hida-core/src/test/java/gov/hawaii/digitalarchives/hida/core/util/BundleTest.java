
package gov.hawaii.digitalarchives.hida.core.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test class for testing the {@link Bundle} utility class.
 * 
 * @author Dongie Agnir
 */
public class BundleTest {
	Bundle bundle;
	
	/**
	 * Test the clear() method.
	 */
	@Test
	public void clear() {
		//Keys to use
		String[] keys = new String[] {"1", "2", "3", "4", "5"};

		//Load the bundle up.
		bundle.putDouble(keys[0], 1.0);
		bundle.putFloat(keys[1], 1.0f);
		bundle.putInteger(keys[2], 1);
		bundle.putLong(keys[3], 1L);
		bundle.putString(keys[4], "one");

		//Poof!
		bundle.clear();

		//Test to make sure they're gone.
		//The numerical getters should be returning the default values.
		assertEquals(bundle.getDouble(keys[0], 2.0), 2.0);
		assertEquals(bundle.getFloat(keys[1], 2.0f), 2.0f);
		assertEquals(bundle.getInteger(keys[2], 2), 2);
		assertEquals(bundle.getLong(keys[3], 2L), 2L);

		assertNull(bundle.getString(keys[4]));
	}
	
	/**
	 * Test copy constructor to make sure data is being copied to new isntance
	 * properly.
	 */
	@Test
	public void copyCtorTest() {
		bundle.putString("string", "string");

		Bundle bundle2 = new Bundle(bundle);

		//clearing the origin bundle shouldn't affect bundle2
		bundle.clear();

		//Check that bundle2 still has the string, while the other does not
		assertTrue(bundle2.getString("string") != null
				&& bundle.getString("string") == null);
	}
	
	@BeforeTest
	public void setUp() {
		bundle = new Bundle();

		//Load up all the types supported
		bundle.putDouble("double", 1.0);
		bundle.putFloat("float", 1.0f);
		bundle.putInteger("integer", 1);
		bundle.putLong("long", 1L);
		bundle.putString("string", "one");
	}
	
	/**
	 * Test getDouble().
	 */
	@Test
	public void testDouble() {
		bundle.putDouble("double", 1.0);
		
		double d = 0.0;
		
		try {
			d = bundle.getDouble("double", 5.0);
		} catch (Exception e) {
			fail();
		}
		
		assertEquals(d, 1.0);
	}

	/**
	 * Test getFloat().
	 */
	@Test
	public void testFloat() {
		bundle.putFloat("float", 1.0f);
		
		float f = 0.0f;
		
		try {
			f = bundle.getFloat("float", 5.0f);
		} catch (Exception e) {
			fail();
		}
		
		//Should not be default value.
		assertEquals(f, 1.0f);
	}

	/**
	 * Small test to see that the getters are returning expected values when
	 * trying to retrieve values using keys that don't have associated values
	 * within the Bundle.
	 */
	@Test
	public void testGetNonExistent() {
		assertNull(bundle.getString("none"));
		
		//No value stored under "none", should return default value (-1)
		assertEquals(bundle.getFloat("none", -1.0f), -1.0f);
		
		//Identical tests as above for Double, Integer, and Long
		assertEquals(bundle.getDouble("none", -1.0), -1.0);
		assertEquals(bundle.getInteger("none", -1), -1);
		assertEquals(bundle.getLong("none", -1L), -1L);
	}

	/**
	 * Test to see that the Bundle behaves properly when asked to retrieve an
	 * object using a valid key (it has an associated value in the Bundle), 
	 * but the type is wrong (e.g. used getFloat() when the value paired with 
	 * the key is a String).
	 */
	@Test
	public void testGetWrongType() {
		//Add our initial values
		bundle.putDouble("double", 1.0);
		bundle.putFloat("float", 1.0f);
		bundle.putInteger("integer", 1);
		bundle.putLong("long", 1L);
		bundle.putString("string", "one");
		
		//Double
		assertNull(bundle.getString("double"));
		//Next three getters should all return the default values
		assertEquals(bundle.getFloat("double", -3.0f), -3.0f);
		assertEquals(bundle.getInteger("double", -3), -3);
		assertEquals(bundle.getLong("double", -3L), -3L);
		
		//Float
		assertNull(bundle.getString("float"));
		//Next three getters should all return the default values
		assertEquals(bundle.getDouble("float", -3.0), -3.0);
		assertEquals(bundle.getInteger("float", -3), -3);
		assertEquals(bundle.getLong("float", -3L), -3L);
		
		//Integer
		assertNull(bundle.getString("intger"));
		//Next three getters should all return the default values
		assertEquals(bundle.getFloat("integer", -3.0f), -3.0f);
		assertEquals(bundle.getDouble("integer", -3.0), -3.0);
		assertEquals(bundle.getLong("integer", -3L), -3L);
		
		//Long
		assertNull(bundle.getString("long"));
		//Next three getters should all return the default values
		assertEquals(bundle.getFloat("long", -3.0f), -3.0f);
		assertEquals(bundle.getInteger("long", -3), -3);
		assertEquals(bundle.getDouble("long", -3.0), -3.0);
		
		//String
		//Next getters should all return the default values
		assertEquals(bundle.getFloat("string", -3.0f), -3.0f);
		assertEquals(bundle.getInteger("string", -3), -3);
		assertEquals(bundle.getDouble("string", -3.0), -3.0);
		assertEquals(bundle.getLong("string", -3L), -3L);
	}
	
	/**
	 * Test getInteger().
	 */
	@Test
	public void testInteger() {
		bundle.putInteger("integer", 1);
		
		int i = 0;
		
		try {
			i = bundle.getInteger("integer", 5);
		} catch (Exception e) {
			fail();
		}
		
		//Should not be default value (5);
		assertEquals(i, 1);
	}
	
	/**
	 * Tests the isEmpty() method.
	 */
	@Test
	public void testIsEmpty() {
		Bundle b = new Bundle();

		//Our new-ly created bundle should be empty.
		assertTrue(b.isEmpty());

		//setUp() loads 'bundle' with default values, should not be empty.
		assertFalse(this.bundle.isEmpty());

		this.bundle.clear();

		//After being cleared, this should be empty.
		assertTrue(this.bundle.isEmpty());
	}
	
	/**
	 * Test getLong().
	 */
	@Test
	public void testLong() {
		bundle.putLong("long", 1L);
		
		long l = 0;
		
		try {
			l = bundle.getLong("long", 5L);
		} catch (Exception e) {
			fail();
		}
		
		//Should not be default value.
		assertEquals(l, 1L);
	}
	
	/**
	 * Test getString().
	 */
	@Test
	public void testString() {
		bundle.putString("name", "bundle");
		
		String name = null;
		
		try {
			name = bundle.getString("name");
		} catch (Exception e) {
			fail();
		}
		
		//Should not be default value.
		assertEquals(name, "bundle");
	}
}
