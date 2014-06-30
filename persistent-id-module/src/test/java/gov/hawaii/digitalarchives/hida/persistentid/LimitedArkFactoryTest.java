package gov.hawaii.digitalarchives.hida.persistentid;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import java.io.File;

import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** Test class for for testing behavior of {@link PersistentIdFactory} with a
 * limited number of IDs.
 * 
 * @author Calvin Wong
 * @author Micah Takabayashi */
@ContextConfiguration(locations = { "classpath:spring/testApplicationContext.xml" })
@ActiveProfiles("limitedId")
public class LimitedArkFactoryTest extends AbstractTestNGSpringContextTests
{
    @Autowired
    /**Limited decimal generation bean.*/
    private PersistentIdFactory limitedDecimalArkFactory;
    private int        idLimit = 1000;

    /**
     * Test createPersistentId method by exhausting all available IDs for given
     * template. Expecting an IllegalArgumentException when the limit is
     * reached.
     */
    @Test(expectedExceptions = HidaException.class)
    public void testLimitedDecimals ()
    {
        // Initialize the factory since it is not initialized automatically in
        // the application context.
        for (int i = 0; i < idLimit; i++)
        {
            PersistentId ark = limitedDecimalArkFactory.createPersistentId();
            if (ark == null)
            {
                // Call again and the next output should be null which means we
                // exhausted all available IDs.
                Assert.assertNull(limitedDecimalArkFactory.createPersistentId());
                break;
            }
        }
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