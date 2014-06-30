package gov.hawaii.digitalarchives.hida.core.model.record;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

public class AgentTest {
    private final String name = "HiDA";
    private final String note = "Super cool.";

    /**
     * Test to make sure the constructor throws when given any null args.
     */
    @Test
    public void TestConstructor() {
        Agent a;

        try {
            a = new MyTestAgent(null, null);
            fail("Constructor didn't throw!");
        } catch (Exception e) {
            //gulp
        }

        try {
            a = new MyTestAgent(name, null);
            fail("Constructor didn't throw!");
        } catch (Exception e) {
            //gulp
        }

        try {
            a = new MyTestAgent(null, note);
            fail("Constructor didn't throw!");
        } catch (Exception e) {
            //gulp
        }

        try {
            a = new MyTestAgent(name, note);
        } catch (Exception e) {
            fail("Constructor thew when given valid args!");
        }
    }

    /**
     * Test the accessors.
     */
    @Test
    public void testAccessors() {
        Agent a = new MyTestAgent(name, note);

        assertEquals(a.getName(), name);
        assertEquals(a.getNote(), note);
    }

    /**
     * All of our Agents derive from {@link Agent}, so we just create
     * our own flavor here.
     *
     * @author Dongie Agnir
     */
    class MyTestAgent extends Agent {
        public MyTestAgent(String name, String note) {
            super(name, note);
        }
    }
}
