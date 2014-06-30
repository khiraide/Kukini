package gov.hawaii.digitalarchives.hida.core.model.record;

import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of an Agent. 
 * 
 * @author Keone Hiraide
 */
@Configurable
@Component
public class AgentDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of Agents to be used for testing purposes.
     */
    private List<Agent> data;

    
    
    /** 
     * Builds a transient Agent to be used for testing.
     * 
     * @return A properly formed Agent to be used for testing.
     */
    public Agent getNewTransientAgent(int index) {
        Agent agent = new Agent("name_" + index, "note_" + index);
        return agent;
    }

    /**
     * Grab a specific Agent instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the Agent instance that you 
     *               want to grab from the database.
     * @return       The Agent.
     */
    public Agent getSpecificAgent(int index) {
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        Agent obj = data.get(index);
        long id = obj.getPrimaryId();
        return Agent.findAgent(id);
    }

    
    
    /**
     * Gets a random Agent from the database.
     * 
     * @return A random Agent from the database.
     */
    public Agent getRandomAgent() {
        Agent obj = data.get(rnd.nextInt(data.size()));
        long id = obj.getPrimaryId();
        return Agent.findAgent(id);
    }

    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  obj Agent object.
     * @return Whether a Agent persistence context 
     *         has been modified.
     */
    //TODO Still need to do this... should be good enough for this story for now though.
    public boolean modifyAgent(Agent obj) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link Agent}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link AgentIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional objects
     * to the database, use the  Agent classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of Agents that
     *                            you want persisted into the database. 
     */
    public void init(int initialDatabaseSize) {
        data = Agent.findAgentEntries(0, initialDatabaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for" 
                    + " 'Agent' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<Agent>();
        for (int i = 0; i < initialDatabaseSize; i++) {
            Agent obj = getNewTransientAgent(i);
            try {
                obj.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            obj.flush();
            data.add(obj);
        }
    }
}