package gov.hawaii.digitalarchives.hida.persistentid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.Test;

/** Tests the application context in the main directory.
 * 
 * @author Micah Takabayashi */
@ContextConfiguration(locations = { "classpath:spring/applicationContext.xml" })
public class MainAppContextTest extends AbstractTestNGSpringContextTests
{
    @Autowired
    private DefaultPersistentIdFactory defaultFactory;
    
    /** Tests that the module can be used as intended when the main application
     * context is loaded. */
    @Test
    public void appContextTest() {
        PersistentId id = this.defaultFactory.createPersistentId();
        Assert.isTrue(Hark.ARK_FORMAT.matcher(id.toString()).matches(), 
                "Minted PID does not match expected format.");
        PersistentId copyId = this.defaultFactory.createPersistentId(id.toString());
        org.testng.Assert.assertEquals(id.toString(), copyId.toString(), 
                "Copied PID was not as expected.");
    }
}
