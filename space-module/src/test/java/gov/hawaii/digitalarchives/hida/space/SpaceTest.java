package gov.hawaii.digitalarchives.hida.space;

import static org.mockito.Mockito.*;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Unit tests for {@link SpaceImpl}.
 *
 * @author Dongie Agnir
 */
@ContextConfiguration(locations = {"classpath:/spring/applicationContext.xml"})
public class SpaceTest extends AbstractTestNGSpringContextTests {
    @AutowiredLogger
    private Logger log;

    private HazelcastInstance hzInstance = null;
    
    @BeforeMethod
    public void setUp() {
    	hzInstance = mock(HazelcastInstance.class);
    	IMap mockMap = mock(IMap.class);
    	when(hzInstance.getMap(any(String.class))).thenReturn(mockMap);
    }

    /**
     * Just a dummy test so Bamboo doesn't mark our build as a FAIL until some
     * real tests are written.
     */
    @Test
    public void dummyTest() {
        log.warn("This is a dummy test.  Please write some real ones!");
    }
}
