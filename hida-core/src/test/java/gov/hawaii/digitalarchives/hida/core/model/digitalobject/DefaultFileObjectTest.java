package gov.hawaii.digitalarchives.hida.core.model.digitalobject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.DigitalSignature;
import gov.hawaii.digitalarchives.hida.core.model.Event;
import gov.hawaii.digitalarchives.hida.core.model.HashValue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.URIException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test the functionality of {@link FileObject} implementation(s).
 * 
 * @author Dongie Agnir
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
public class DefaultFileObjectTest extends AbstractTestNGSpringContextTests {
    
    private static final String TEST_RECORD = "/testrecord.txt";
    private static final long TEST_RECORD_SIZE = 49; //bytes

    private FormatRegistryEntry formatInfo;
    private String hashAlgorithm;
    private byte[] hashBytes;
    private HashValue hashValue;
    private String id;
    private Path rawFile;
    private String representationId;
    private DigitalSignature signature;
    private Property[] significantProps;

    private FileObject testFileObject;

    /**
     * Convenience method for creating a test FileObject.
     *
     * @return The test object.
     */
    private FileObject makeTestObject() {
        DefaultFileObject fileObject = new DefaultFileObject();

        fileObject.setPrimaryId(id);
        fileObject.setRepresentationId(representationId);
        fileObject.setFilePath(rawFile.toString());
        fileObject.setHash(hashValue.getHashBytes(), hashValue.getHashAlgorithm());
        fileObject.setFormatInformation(formatInfo);
        fileObject.setDigitalSignature(signature);
        for (Property p : significantProps) {
            fileObject.addSignificantProperty(p);
        }

        return fileObject;
    }

    /**
     * Set up default values for each subsequent test.
     * @throws MalformedURLException
     * @throws URIException
     * @throws URISyntaxException
     */
    @BeforeTest
    public void setup() throws URIException, URISyntaxException {

        id = "http://digitalarchives.hawaii.gov/ark:/blah";
        representationId = "http://digitalarchives.hawaii.gov/ark:/rep1";
        rawFile = new File(getClass().getResource(TEST_RECORD).getPath()).toPath();
        hashBytes = new byte[]{(byte) 0xff};
        hashAlgorithm = "simple";

        /**
         * Mock some objects here, so we don't depend on a specific implementation.
         */
        hashValue = mock(HashValue.class);

        when(hashValue.getHashAlgorithm()).thenReturn(hashAlgorithm);
        when(hashValue.getHashBytes()).thenReturn(hashBytes);

        formatInfo  = mock(FormatRegistryEntry.class);

        signature = mock(DigitalSignature.class);
        when(signature.getAlgorithm()).thenReturn("DSA");
        when(signature.getSignatureBytes()).thenReturn(new byte[]{(byte)0xff, (byte)0xfe});

        significantProps = new Property[]{ mock(Property.class), mock(Property.class) };
        when(significantProps[0].getName()).thenReturn("prop1");
        when(significantProps[0].getValue()).thenReturn("property");
        when(significantProps[1].getName()).thenReturn("prop2");
        when(significantProps[1].getValue()).thenReturn("property");
    }

    /**
     * Test to make sure that we get a thrown exception when trying to add a
     * null Property.
     */
    @Test
    public void testAddNullProperty() {
        FileObject fileObj = makeTestObject();

        try {
            fileObj.addSignificantProperty(null);
            fail("addSignificantPrperties did not throw when trying to add null property.");
        } catch (Exception e) {

        }
    }

    /**
     * Test to see that adding two properties of the same name result in the
     * object overwriting the previous entry, rather than keep both.
     */

    @Test
    public void testAddSamePropTwice() {
        FileObject fileObj = makeTestObject();

        Property p = mock(Property.class);
        String name = significantProps[0].getName();
        String value = significantProps[0].getValue();
        String newValue = "new " + value;

        when(p.getName()).thenReturn(name);
        when(p.getValue()).thenReturn(newValue);

        // This operation should overwrite the old property value, not just add
        // it alongside the old one.
        fileObj.addSignificantProperty(p);

        List<Property> propMatches = new ArrayList<Property>();

        // Find all Property objects with this name.  We expect only one.
        for (Property pp : fileObj.getSignificantProperties()) {
            if (pp.getName().equals(name)) {
                propMatches.add(pp);
            }
        }

        // Make sure we only found one property of this name.
        assertEquals(propMatches.size(), 1,
                "DefaultFileObject does not contain the expected quantity of Properties " +
                "with the name "  + "\"" + name + "\".");

        // Make sure that the value of the property is our expected new value.
        assertTrue(propMatches.get(0).getValue().equals(newValue),
                "Old value of property was not overwritten by the new property.");
    }

    /**
     * Test the digital signature property.
     */
    @Test
    public void testDigitalSignature() {
        FileObject fileObj = makeTestObject();

        DigitalSignature signature = fileObj.getDigitalSignature();

        assertEquals(this.signature.getAlgorithm(), signature.getAlgorithm());
        assertEquals(this.signature.getSignatureBytes(), signature.getSignatureBytes());
    }

    /**
     * Test the events property.
     */
    @Test
    public void testEvents() {
        FileObject fileObj = makeTestObject();

        Set<Event> events = fileObj.getEvents();

        //modify our copy
        Event e = mock(Event.class);
        events.add(e);

        //fileObj's list is not modified as a result
        assertFalse(fileObj.getEvents().contains(e));
    }

    /**
     * Test that {@link DefaultFileObject#getSize()} returns 0 when asked to
     * get the size of the file if the Path to it is actually null.
     *
     * Note: that it should return 0 because that's the behavior exhibited by
     * {@link java.io.File#length()} when the file doesn't actually exist.
     */
    @Test
    public void testGetSizeWhenPathNull() {
        DefaultFileObject fileObject = new DefaultFileObject();
        assertNull(fileObject.getFilePath());
        assertEquals(fileObject.getSize(), 0);
    }

    /**
     * Test the hash property.
     */
    @Test
    public void testHash() {
        FileObject fileObj = makeTestObject();

        HashValue hashValue = fileObj.getHash();

        // Instances should now be the same. When you grab a Fileobject from the
        // database, you would the associated hash to match what is set by
        // the file object.
        assertEquals(this.hashValue.getHashAlgorithm(), hashValue.getHashAlgorithm());
        assertEquals(this.hashValue.getHashBytes(), hashValue.getHashBytes());
    }

    /**
     * Make sure that we are getting defensive copies of the list of
     * significant properties.
     */
    @Test
    public void testSignificantProperties() {
        FileObject fileObj = makeTestObject();

        //Get the list of properties.  makeTestObject() should have added some
        //for us beforehand, check just to make sure.
        List<Property> props = fileObj.getSignificantProperties();
        assertFalse(props.isEmpty());

        //Clear our copy, should not affect the internal copy in fileObj.
        props.clear();
        assertFalse(fileObj.getSignificantProperties().isEmpty());
    }

    /**
     * Test the size property.
     */
    @Test
    public void testSize() {
        FileObject fileObj = makeTestObject();

        assertEquals(fileObj.getSize(), TEST_RECORD_SIZE);
    }
}
