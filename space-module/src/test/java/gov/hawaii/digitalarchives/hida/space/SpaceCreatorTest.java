package gov.hawaii.digitalarchives.hida.space;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import org.slf4j.Logger;

/**
 * Small test to verify that SpaceCreator can be used properly in a Spring
 * context.
 *
 * @author Dongie Agnir
 */
@ContextConfiguration(locations = {"classpath:/spring/spaceCreatorTestContext.xml"})
@Test
public class SpaceCreatorTest extends AbstractTestNGSpringContextTests {
    private static final String TEST_SPACE_BEAN_NAME = "TestSpace1";
    private static final String HAZELCAST_INSTANCE_BEAN_NAME = "hcInstance";

    @Autowired
    @Qualifier(TEST_SPACE_BEAN_NAME)
    private SpaceImpl testSpace;

    @Autowired
    @Qualifier(HAZELCAST_INSTANCE_BEAN_NAME)
    private HazelcastInstance hcInstance;

    @AutowiredLogger
    private Logger log;

    @BeforeMethod
    public void setUp() {
    }

    /**
     * Test to verify that the space returns (and is using) the correct name.
     */
   @Test
    public void testCorrectName() {
        assertEquals(testSpace.getName(), TEST_SPACE_BEAN_NAME);
    }
}
