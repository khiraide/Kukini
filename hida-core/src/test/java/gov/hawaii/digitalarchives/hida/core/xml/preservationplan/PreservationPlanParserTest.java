package gov.hawaii.digitalarchives.hida.core.xml.preservationplan;

import gov.hawaii.digitalarchives.hida.core.model.preservationplan.FormatPlan;
import gov.hawaii.digitalarchives.hida.core.model.preservationplan.PreservationPlan;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the {@link PreservationPlanParserImpl} class.
 * @author Keone Hiraide
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
public class PreservationPlanParserTest extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * PreservationPlan parser to be used for parsing preservation plan model
     * object files.
     */
    private PreservationPlanParserImpl ppParser;
    
    @BeforeClass
    public void setup() {
        ppParser = new PreservationPlanParserImpl();
    }
    
    /**
     * Tests to see if the values parsed are correct.
     * @throws URISyntaxException The syntax of the URI string passed in
     *                            was not correctly formed.
     */
    @Test
    public void testParse() throws URISyntaxException {
        PreservationPlan pp;
        Path ppXML = Paths.get(getClass().getResource("/PP_XML.xml").toURI());
        
        // Parsing a preservation plan xml file and returning a PreservationPlan object
        // that is ready to be persisted. 
        pp = ppParser.parse(ppXML);
        
        // Checking the preservation plan's primary id.
        Assert.assertEquals(pp.getPrimaryId(), "ark:/12345/primaryId");
        
        // Checking the preservation plan's label.
        Assert.assertEquals(pp.getLabel(), "Hawaii State Archives Digital Preservation Plan v0.5");
        
        // Getting a format plan that we know exists and checks to ensure that its
        // values are correct.
        FormatPlan formatPlan = pp.getFormatPlans().get("FMT/40");
        Assert.assertEquals(formatPlan.getPrimaryId(), ".doc 97-2003");
        Assert.assertEquals(formatPlan.getNativeFormat().getFormatName(), ".doc");
        Assert.assertEquals(formatPlan.getNativeFormat().getPronomFormat(), "FMT/40");
        Assert.assertEquals(formatPlan.getPreservationFormat().getFormatName(), ".docx");
        Assert.assertEquals(formatPlan.getPreservationFormat().getPronomFormat(), "FMT/412");
        Assert.assertEquals(formatPlan.getPresentationFormat().getFormatName(), ".pdf/A");
        Assert.assertEquals(formatPlan.getPresentationFormat().getPronomFormat(), "FMT/95");
        Assert.assertEquals(formatPlan.getThumbnailFormat().getFormatName(), ".jpg");
        Assert.assertEquals(formatPlan.getThumbnailFormat().getPronomFormat(), "FMT/42");
    }
}
