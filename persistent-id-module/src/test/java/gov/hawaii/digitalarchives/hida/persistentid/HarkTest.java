package gov.hawaii.digitalarchives.hida.persistentid;

import java.io.File;

import org.apache.commons.httpclient.URIException;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** Tests for the {@link Hark} class.
 * 
 * @author Micah Takabayashi */
@ContextConfiguration(locations = { "classpath:spring/testApplicationContext.xml" })
@ActiveProfiles("basicFactory")
public class HarkTest extends AbstractTestNGSpringContextTests
{
    @Autowired
    @Qualifier(Ark.SCHEME)
    private PersistentIdFactory factory;
    
    /** Test to ensure that a {@link Hark} instance returns the expected results
     * when queried. 
     * @throws NullPointerException 
     * @throws URIException */
    @Test
    public void HarkBasicTest() throws URIException, NullPointerException {
        
        // Create Hark.
        String naan = "12345";
        String arkName = "ARKname";
        String qualifier = "qua.lif.ier";
        Hark hark = (Hark) factory.createPersistentId(
                Ark.SCHEME + ":/" + naan + "/" + arkName + "/" + qualifier);
        
        // Check method return value against expected values.
        checkHark(hark, naan, arkName, qualifier);
        
        // Check that URI matches expected ARK format.
        String formatErrorMsg = "Hark URI format not as expected.";
        String expectedResult = Ark.SCHEME + ":/" + naan + "/" + arkName + "/" + qualifier;
        Assert.assertEquals(hark.toString(), expectedResult, formatErrorMsg);
        
        // As above, but for an empty qualifier.
        qualifier = "";
        hark = (Hark) factory.createPersistentId(
                Ark.SCHEME + ":/" + naan + "/" + arkName);
        checkHark(hark, naan, arkName, qualifier);
        expectedResult = Ark.SCHEME + ":/" + naan + "/" + arkName;
        Assert.assertEquals(hark.toString(), expectedResult, formatErrorMsg);
    }
    
    /** Tests that a Hark built from an existing valid Hark URI is created
     * properly.
     * 
     * @throws NullPointerException
     * @throws URIException */
    @Test
    public void HarkCopyTest() throws URIException, NullPointerException {
        
        // Create Hark.
        String naan = "12345";
        String arkName = "ARKname";
        String qualifier = "qua.lif.ier";
        Hark hark = (Hark) factory.createPersistentId(
                Ark.SCHEME + ":/" + naan + "/" + arkName + "/" + qualifier);
                
        // Check that a new Hark created from the original as a String returns
        // an identical ARK.
        Hark copiedHark = (Hark) factory.createPersistentId(hark.toString());
        checkHark(copiedHark, naan, arkName, qualifier);
        Assert.assertEquals(hark, copiedHark,
                "URI not as expected.");
        
        // Do as above, but for an empty qualifier.
        qualifier = "";
        hark = (Hark) factory.createPersistentId(
                Ark.SCHEME + ":/" + naan + "/" + arkName);
        copiedHark = (Hark) factory.createPersistentId(hark.toString());
        checkHark(copiedHark, naan, arkName, qualifier);
        Assert.assertEquals(hark, copiedHark,
                "URI not as expected.");
    }
    
    /** Check that an exception is thrown when a bad NAAN is given to the Hark.
     * 
     * @throws URIException */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void HarkBadNaanTest() throws URIException {
        // Create Hark.
        String naan = "badNAAN";
        String arkName = "ARKname";
        factory.createPersistentId(Ark.SCHEME + ":/" + naan + "/" + arkName);
    }

    /** Check that an exception is thrown when an empty name is given to the
     * Hark.
     * 
     * @throws URIException */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void HarkEmptyNameTest() throws URIException {
        // Create Hark.
        String naan = "12345";
        String arkName = "";
        String qualifier = "qua.lif.ier";
        factory.createPersistentId(Ark.SCHEME + ":/" + naan + "/" + arkName + "/" + qualifier);
        
        Assert.fail("Expected exception not thrown.");
    }
    
    /** Check that all fields returned by Hark accessors match expected values. */
    private void checkHark(Hark hark, String naan, String arkName, String qualifier) {
        
        Assert.assertEquals(hark.getNaan(), naan,
                "Hark NAAN not as expected.");
        Assert.assertEquals(hark.getArkName(), arkName,
                "Hark ARK name not as expected.");
        Assert.assertEquals(hark.getQualifier(), qualifier,
                "Hark qualifier not as expected.");
    }
    
    /** Clean up after each test. */
    @AfterMethod
    public void cleanup ()
    {
        File file = new File("webapps");
        FileUtil.deleteContents(file);
        file.delete();
    }
}
