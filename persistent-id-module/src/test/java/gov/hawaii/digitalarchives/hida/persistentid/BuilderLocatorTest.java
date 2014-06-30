package gov.hawaii.digitalarchives.hida.persistentid;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import java.io.File;

import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** Tests for the {@link PersistentIdBuilderLocator}.
 * 
 * @author Micah Takabayashi */
@ContextConfiguration(locations = { "classpath:spring/testApplicationContext.xml" })
@ActiveProfiles("basicFactory")
public class BuilderLocatorTest extends AbstractTestNGSpringContextTests
{
    @Autowired
    private PersistentIdBuilderLocator factoryLocator;
    
    @Autowired
    @Qualifier("basicFactory")
    private DefaultPersistentIdFactory defaultFactory;
    
    @Autowired
    @Qualifier("badSchemaFactory")
    private DefaultPersistentIdFactory badSchemaFactory;
    
    /** Test that the {@link PersistentIdBuilderLocator} can find an existing
     * {@link PersistentIdFactory}, and that the PersistentIdFactory has been
     * properly injected with its dependencies and can be used. */
    @Test
    public void findArkBuilder ()
    {
        PersistentIdFactory arkFactory = this.factoryLocator.createPersistentIdBuilder(Ark.SCHEME);
        PersistentId id = arkFactory.createPersistentId();
        Assert.isTrue(
                Hark.ARK_FORMAT.matcher(id.toString()).matches(),
                "Returned PersistentId does not match expected format. " +
                "Incorrect PersistentIdFactory may have been retrieve, " +
                "or factory was not injected properly.");
    }
    
    /** Test the basic behavior of the {@link DefaultPersistentIdFactory}. */
    @Test
    public void defaultFactoryTest ()
    {
        PersistentId id = this.defaultFactory.createPersistentId();
        Assert.isTrue(Hark.ARK_FORMAT.matcher(id.toString()).matches(), 
                "Minted PID does not match expected format.");
        PersistentId copyId = this.defaultFactory.createPersistentId(id.toString());
        org.testng.Assert.assertEquals(id.toString(), copyId.toString(), 
                "Copied PID was not as expected.");
    }
    
    /** Test for expected exception when a {@link DefaultPersistentIdFactory}
     * constructed with a valid schema is asked to wrap an existing PID with a
     * bad schema. */
    @Test(expectedExceptions = HidaException.class)
    public void defaultFactoryBadSchemaCopyTest ()
    {
        // Fake schema used here conforms to the ARK schema, save for the name.
        this.defaultFactory.createPersistentId("someRubbishThatIsntASchema:/12345/someId");
    }
    
    /** Test for expected exception when the {@link DefaultPersistentIdFactory}
     * cannot find an appropriate builder for a given schema when minting a new
     * PID. */
    @Test(expectedExceptions = HidaException.class)
    public void defaultFactoryBadSchemaMintingTest ()
    {
        this.badSchemaFactory.createPersistentId();
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
