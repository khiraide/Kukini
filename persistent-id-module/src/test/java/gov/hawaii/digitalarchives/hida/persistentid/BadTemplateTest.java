package gov.hawaii.digitalarchives.hida.persistentid;

import java.io.File;

import org.aspectj.util.FileUtil;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** Test for exception when an ArkFactory with a bad template is created.
 * 
 * @author Calvin Wong
 * @author Micah Takabayashi */
@ContextConfiguration(locations = { "classpath:spring/testApplicationContext.xml" })
@ActiveProfiles("badTemplate")
public class BadTemplateTest extends AbstractTestNGSpringContextTests {

    /** Test that IllegalArgumentException is thrown for a bad template. */
    @Test
    public void testInvalidTemplate ()
    {
        String errString = "No exception thrown.";
        try {
            this.applicationContext.getBean("bad-template-factory");
        } catch (Exception e) {
            errString = e.getMessage();
        }
        Assert.assertTrue(errString.contains("IllegalArgumentException"));
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