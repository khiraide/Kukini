package gov.hawaii.digitalarchives.hida.core.model.record;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Integration test class for a Agent model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class AgentIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {
    
    /**
     * Helper class used to test the Agent model class methods.
     */
    @Autowired // Marking '@Autowired' so that this AgentOnDemand
    // instance is autowired by Spring's dependency injection facilities
    // to be used as service.
    AgentDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;

    /**
     * Initializes our in-memory database with Agent objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }



    /**
     * Tests gets for a Agent from the database.
     */
    @Test
    public void testGetRandomAgent() {
        // Grabbing a random agents from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomAgent()); 
        }
    }
    
    
    
    /**
     * Tests to see if the Agent count method works correctly.
     */
    @Test
    public void testCountAgents() {
        long count = Agent.countAgents();
        
        Assert.assertEquals(count, INITIAL_DB_SIZE, "Counter for 'Agent' incorrectly " +
                "reported the amount of entries");
    }

    
    
    /**
     * Throughly testing the grabbing of a Agent 
     * from the database.
     */
    @Test
    public void testFindAgent() {
        Agent agent = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            agent = dod.getSpecificAgent(i);
            Long id = agent.getPrimaryId();
            agent = Agent.findAgent(id);
            Assert.assertNotNull(agent, "Find method for 'Agent' illegally" +
                    " returned null for id '" + id + "'");
            Assert.assertEquals(id, agent.getPrimaryId(), "Find method for" +
                    " 'Agent' returned the incorrect identifier"); 
        }
    }

    
    
    /**
     * Tests a Agent find all method.
     */
    @Test
    public void testFindAllAgents() {
        List<Agent> result = Agent.findAllAgents();
       
        Assert.assertNotNull(result, "Find all method for 'Agent' " +
                "illegally returned null");
        
        Assert.assertEquals(result.size(), INITIAL_DB_SIZE, "Find all method for" +
                " 'Agent' returned the incorrect amount of entries.");
    }

    
    
    /**
     * Tests the findAgentEntries method of a Agent.
     */
    @Test
    public void testFindAgentEntries() {
        long count = Agent.countAgents();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Agent> result = Agent
                .findAgentEntries(firstResult, maxResults);
        
        Assert.assertNotNull(result, "Find entries method for 'Agent'" +
                " illegally returned null");
        
        Assert.assertEquals(count, result.size(), "Find entries method for 'Agent'" +
                " returned an incorrect number of entries");
    }

    
    
    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        Agent agent = dod.getRandomAgent();
        long id = agent.getPrimaryId();
        agent = Agent.findAgent(id);
        Assert.assertNotNull(agent, "Find method for 'Agent' " +
                "illegally returned null for id '" + id + "'");
        boolean modified =  dod.modifyAgent(agent);
        Integer currentVersion = agent.getVersion();
        agent.flush();
        
        Assert.assertTrue((currentVersion != null && 
                agent.getVersion() > currentVersion) || 
                    !modified, "Version for 'Agent' failed " +
                            "to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        Agent obj = dod.getRandomAgent();
        Long id = obj.getPrimaryId();
        obj = Agent.findAgent(id);
        boolean modified =  dod.modifyAgent(obj);
        Integer currentVersion = obj.getVersion();
        Agent merged = obj.merge();
        obj.flush();
        Assert.assertEquals(merged.getPrimaryId(), id, "Identifier of merged object" +
                " not the same as identifier of original object");
        Assert.assertTrue((currentVersion != null && obj.getVersion() > 
            currentVersion) || !modified,
                "Version for 'Agent' failed to increment on merge and flush directive");
    }

    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        Agent agent = dod.getNewTransientAgent(Integer.MAX_VALUE);
        Assert.assertNotNull(agent, "Data on demand for 'Agent' failed to " +
                "provide a new transient entity");
   
        try {
            agent.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        agent.flush();
        
        // Grabbing a count of the Agents that are currently in the database.
        long count = Agent.countAgents();
        
        // The persist should've incremented the count of 
        // Agents in the DB from 10 to INITIAL_DB_SIZE + 1.
        assertEquals(count, INITIAL_DB_SIZE + 1, "The counter after a persist didn't " +
                "increment."); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        Agent agent = dod.getRandomAgent();
        long id = agent.getPrimaryId();
        agent = Agent.findAgent(id);
        agent.remove();
        agent.flush();
        
        Assert.assertNull(Agent.findAgent(id), "Failed to remove " +
                "'Agent' with identifier '" + id + "'");
    }
    
}