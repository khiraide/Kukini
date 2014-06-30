package gov.hawaii.digitalarchives.hida.core.xml.rtp;

import gov.hawaii.digitalarchives.hida.core.model.rtp.RecordsTransmittalPlan;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the {@link RecordsTransmittalPlanParserImpl} class.
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
public class RecordsTransmittalPlanParserTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * RecordsTransmittalPlan parser to be used for parsing RTP model
     * object files.
     */
    private RecordsTransmittalPlanParserImpl rtpParser;
    
    /**
     * Creates a RecordsTransmittalPlan parser to be used for parsing RTP model
     * object files.
     */
    @BeforeClass
    public void setup() {
        rtpParser = new RecordsTransmittalPlanParserImpl();
    }
    
    /**
     * Tests to see if the values parsed and retrieved are correct.
     * @throws URISyntaxException The syntax of the URI string passed in
     *                            was not correctly formed.
     */
    @Test
    public void testParse() throws URISyntaxException {
        RecordsTransmittalPlan rtp;
        Path rtpXML = Paths.get(getClass().getResource("/RTP_XML.xml").toURI());
        rtp = (RecordsTransmittalPlan) rtpParser.parse(rtpXML);
        Assert.assertEquals("ark:/12345/primaryId", rtp.getPrimaryId());
        Assert.assertEquals("ark:/12345/parserId", rtp.getParserId());
    }
}
