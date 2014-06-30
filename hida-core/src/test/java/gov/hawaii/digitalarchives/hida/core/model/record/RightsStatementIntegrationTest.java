package gov.hawaii.digitalarchives.hida.core.model.record;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration test class for a RightsStatement model class.
 * 
 * @author Keone Hiraide
 */
@Configurable
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
@Transactional
public class RightsStatementIntegrationTest 
extends AbstractTransactionalTestNGSpringContextTests {

    /**
     * Helper class used to test the RightsStatement model class methods.
     */
    @Autowired // Marking '@Autowired' so that this RightsStatementOnDemand
               // instance is autowired by Spring's dependency injection facilities
               // to be used as service.
    RightsStatementDataOnDemand dod;
    
    // The initial amount of objects that you want persisted database to be used for testing
    // purposes. Note: "10" is an arbitrary value used for these tests. 
    final int INITIAL_DB_SIZE = 10;
    
    
    
    /**
     * Initializes our in-memory database with RightsStatement objects
     * to be used for testing purposes.
     */
    @BeforeClass(alwaysRun = true)
    public void setup() {
        dod.init(INITIAL_DB_SIZE);
    }

    
    
    /**
     * Gets a RightsStatement from the database.
     */
    @Test
    public void testGetRightsStatement() {
        // Grabbing a random RightsStatements from the database.
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            assertNotNull(dod.getRandomRightsStatement()); 
        }
    }
    
    
    
    /**
     * Tests to see if the RightsStatement count method works correctly.
     */
    @Test
    public void testCountRightsStatements() {
        // Grabbing a count of the RightsStatements that are currently in the database.
        long count = RightsStatement.countRightsStatements(); 
        
        // Look at RightsStatementDataOnDemand.init(int initialDatabaseSize) method for more info.
        assertTrue(count == INITIAL_DB_SIZE); 
    }

    
    
    /**
     * Throughly testing the grabbing of a RightsStatement 
     * from the database.
     */
    @Test
    public void testFindRightsStatement() {
        RightsStatement rightsStatement = null;
        for (int i=0; i < INITIAL_DB_SIZE; i++) {
            rightsStatement = dod.getSpecificRightsStatement(i);
            String PrimaryId = rightsStatement.getPrimaryId().toString();
           
            rightsStatement = RightsStatement.findRightsStatement(PrimaryId);
            
            assertNotNull(rightsStatement, "Find method for 'RightsStatement' illegally returned " +
                    "null for PrimaryId '" + PrimaryId + "'");
            
            assertEquals(PrimaryId, rightsStatement.getPrimaryId().toString(), "Find method for " +
                    "'RightsStatement' returned the incorrect identifier");
        }
    }

    
    
    /**
     * Tests a RightsStatement find all method.
     */
    @Test
    public void testFindAllRightsStatements() {
        // Grabbing all RightsStatements from the database.
        List<RightsStatement> result = RightsStatement
                .findAllRightsStatements();
        assertNotNull(result, "Find all method for 'RightsStatement' " +
                "illegally returned null");
        assertTrue(result.size() == INITIAL_DB_SIZE, "Find all method for 'RightsStatement' " +
                "failed.");
    }

    
    
    /**
     * Tests the findRightsStatementEntries 
     * method of a RightsStatement.
     */
    @Test
    public void testFindRightsStatementEntries() {
        long count = RightsStatement.countRightsStatements();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<RightsStatement> result = RightsStatement
                .findRightsStatementEntries(firstResult, maxResults);
        
        assertNotNull(result, "Find entries method for 'RightsStatement' " +
                "illegally returned null");
        
        assertEquals(count, result.size(), "Find entries method for 'RightsStatement' " +
                "returned an incorrect number of entries");
    }
    
    

    /**
     * Testing to see if our flush method is working as intended.
     */
    @Test
    public void testFlush() {
        RightsStatement rightsStatement = dod.getRandomRightsStatement();
        String PrimaryId = rightsStatement.getPrimaryId().toString();
        rightsStatement = RightsStatement.findRightsStatement(PrimaryId);
        
        assertNotNull(rightsStatement, "Find method for 'RightsStatement' illegally " +
                "returned null for PrimaryId '" + PrimaryId + "'");
        boolean modified =  dod.modifyRightsStatement(rightsStatement);
        Integer currentVersion = rightsStatement.getVersion();
        rightsStatement.flush();
        
        assertTrue((currentVersion != null && rightsStatement.getVersion() 
                > currentVersion) || !modified, "Version for 'RightsStatement' " +
                        "failed to increment on flush directive");
    }

    
    
    /**
     * Testing if our merge method is working as intended.
     */
    @Test
    public void testMergeUpdate() {
        RightsStatement rightsStatement = dod.getRandomRightsStatement();
        String PrimaryId = rightsStatement.getPrimaryId().toString();

        rightsStatement = RightsStatement.findRightsStatement(PrimaryId);
        boolean modified =  dod.modifyRightsStatement(rightsStatement);
        Integer currentVersion = rightsStatement.getVersion();
        RightsStatement merged = rightsStatement.merge();
        rightsStatement.flush();
        
        assertEquals(merged.getPrimaryId().toString(), PrimaryId, "Identifier of merged object " +
                "not the same as identifier of original object");
        
        assertTrue((currentVersion != null && rightsStatement.getVersion() 
                > currentVersion) || !modified, "Version for 'RightsStatement' " +
                        "failed to increment on merge and flush directive");
    }
    
    
    
    /**
     * Testing a persist to the database.
     */
    @Test
    public void testPersist() {
        // Creating a RightsStatement from scratch.
        RightsStatement rightsStatement = dod.getNewTransientRightsStatement(Integer.MAX_VALUE);
        assertNotNull(rightsStatement, "Data on demand for 'RightsStatement' failed to " +
                "provide a new transient entity");
       
        try {
            rightsStatement.persist();
        } catch (final ConstraintViolationException e) {
            String msg = ModelTestHelper.getConstraintViolationMessage(e);
            fail(msg, e);
        }
        // Synchronizing our persistence context to the database.
        rightsStatement.flush();
        
        // Grabbing a count of the RightsStatements that are currently in the database.
        long count = RightsStatement.countRightsStatements(); 

        // The persist should've incremented the count of RightsStatements in the DB from
        // 10 to 11.
        assertTrue(count == INITIAL_DB_SIZE + 1); 
    }

    
    
    /**
     * Tests to see if our remove method is working properly. 
     */
    @Test
    public void testRemove() {
        RightsStatement rightsStatement = dod.getRandomRightsStatement();
        String primaryId = rightsStatement.getPrimaryId().toString();
        rightsStatement = RightsStatement.findRightsStatement(primaryId);
        rightsStatement.remove();
        rightsStatement.flush();
        
        assertNull(RightsStatement.findRightsStatement(primaryId), "Failed to " +
                "remove 'RightsStatement' with identifier '" + primaryId + "'");
    }
}
