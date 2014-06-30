package gov.hawaii.digitalarchives.hida.core.model.record;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Collection of simple test cases for {@link Representation}.
 *
 * @author Dongie Agnir
 */
public class RepresentationTest {
    private Representation rep;
    private String digitalRecordId;
    private String id;
    private String source;
    private String previous;
    private List<String> fileObjects;

    @BeforeTest
    public void setUp() throws URIException {
        digitalRecordId = "ark://record1";
        id = "ark://rep3";
        source = "ark://rep2";
        previous = "ark://rep1";

        String objPref = "obj";
        fileObjects = new ArrayList<String>(10);
        for (int i = 0; i < 10; ++i) {
            fileObjects.add("ark://" + objPref + i);
        }

        rep = new Representation(id, digitalRecordId, previous, source, 
                fileObjects);
    }

    /**
     * Simple test to make sure properties are returned properly.
     * @throws URIException 
     */
    @Test
    public void testProperties() throws URIException {
        // Id's won't be the same instance, since we're creating a new
        // digitalRecordId when calling Representation.get()
        assertEquals(id, rep.getId());
        assertEquals(digitalRecordId, rep.getDigitalRecordId());
        assertEquals(source, rep.getSource());
        assertEquals(previous, rep.getPrevious());
        assertTrue(fileObjects.equals(rep.getFileObjects()));
    }

    /**
     * Test to ensure that the list of URI objects is a defensive copy, and
     * not backed by the object's internal copy.
     */
    @Test
    public void testDefensiveObjList() {
        List<String> myCopy = rep.getFileObjects();

        myCopy.clear();

        assertFalse(myCopy.equals(rep.getFileObjects()));
    }
}
