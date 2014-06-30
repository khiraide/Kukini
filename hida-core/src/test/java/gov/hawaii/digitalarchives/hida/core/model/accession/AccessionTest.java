package gov.hawaii.digitalarchives.hida.core.model.accession;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
public class AccessionTest extends AbstractTestNGSpringContextTests {

    @AutowiredLogger
    private final Logger logger = null;
    
    private int callCount_ = 0;
    
    private Accession makeTestAccession() {
        Accession accession = new Accession();

        accession.setAccessionCreationDate(new Date());
        accession.setAccessionReceiveDate(new Date());
        return accession;
    }

    /**
     * Basic test to ensure that {@link
     * Accession#addMachineInfoPair(MachineInfoPair)} throws the appropriate
     * exception when given a null pair.
     * <p>
     * Note: Should we just let the null ptr exception happen instead???
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNullMachineInfoPair() {
        Accession accession = makeTestAccession();
        accession.setMachineInfo(null);
    }

    /**
     * Test that Accession is a returning a defensive copy for the accession
     * creation date.
     */
    @Test
    public void testDefensiveAccessionCreationDate() {
        Accession accession = makeTestAccession();

        Date d = accession.getAccessionCreationDate();
        d.setTime(0L);

        assertNotEquals(d.getTime(), accession.getAccessionCreationDate().getTime());
    }

    /**
     * Same as {@link #testDefensiveAccessionCreationDate()}, except for the
     * receive date.
     */
    @Test
    public void testDefensiveAccessionReceiveDate() {
        Accession accession = makeTestAccession();

        Date d = accession.getAccessionReceiveDate();
        d.setTime(0L);

        assertNotEquals(d.getTime(), accession.getAccessionReceiveDate().getTime());
    }

    /**
     * Test to make sure {@link Accession#getManifest()} is returning a
     * defensive copy.
     */
    @Test
    public void testDefensiveManifestList() {
        Accession accession = makeTestAccession();

        List<ManifestDrive> manifest = accession.getManifest();
        ManifestDrive newDrive = new ManifestDrive("007", new ArrayList<ManifestVolume>(), accession);
        manifest.add(newDrive);

        assertNotEquals(manifest.size(), accession.getManifest().size());
    }

    /**
     * Accession objects make defensive copies of dates that are given to it.
     * Make sure that if we try set a Date as null, that we don't try to make a
     * copy of it and accidentally dereference it.
     */
    @Test
    public void testAddNullDates() {
        Accession accession = makeTestAccession();

        accession.setAccessionCreationDate(null);
        accession.setAccessionReceiveDate(null);
    }


    /**
     * Accession objects will return defensive copies of Dates it returns.  Make
     * sure that if it's holding nulls, it doesn't try to dereference in an
     * attempt to make a copy.
     */
    @Test
    public void testGetNullDates() {
        Accession accession = makeTestAccession();

        try {
            accession.setAccessionCreationDate(null);
            accession.setAccessionReceiveDate(null);

            accession.getAccessionCreationDate();
            accession.getAccessionReceiveDate();
        } catch (NullPointerException npe) {
            fail("Oops we tried to deref the pointer internally!");
        }
    }
    
    /** Tests for an infinite recursion bug that was occurring when Accession
     * tried to reflectively call toString on Events. Count the number of times
     * toString is called when logging and check for unreasonable values. */
    @Test
    public void testToString() {
        callCount_ = 0;
        Accession accession = new Accession();
        Event eventSpy = Mockito.spy(
                new Event("eventId:/07111/abcd", Event.EventType.CAPTURE, new Date(), "testing"));
        Mockito.when(eventSpy.toString()).then(new Answer<String>()
        {
            @Override
            public String answer (InvocationOnMock invocation) throws Throwable
            {
                AccessionTest.this.callCount_++;
                return (String) invocation.callRealMethod();
            }
        });
        accession.addEvent(eventSpy);
        this.logger.debug("accession = {}", accession.toString());
        
        // Check that toString wasn't called an unreasonable number of times.
        Assert.assertTrue(callCount_ < 5, "Unexpectedly large number of calls to toString.");
    }
}
