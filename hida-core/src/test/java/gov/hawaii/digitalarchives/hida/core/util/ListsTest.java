
package gov.hawaii.digitalarchives.hida.core.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.testng.annotations.Test;

public class ListsTest {
	
	@Test
	public void testNewArrayList() {
		List<String> strList = Lists.newArrayList();
		assertNotNull(strList);
		
		strList.add("Hello, World");
		assertEquals(strList.size(), 1);
		
	}
}
