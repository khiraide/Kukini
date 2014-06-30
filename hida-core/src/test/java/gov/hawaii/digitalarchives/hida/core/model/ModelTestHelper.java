package gov.hawaii.digitalarchives.hida.core.model;

import java.util.Iterator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * A helper class used during the the unit testing of the model classes.
 * 
 * @author Keone Hiraide
 *
 */
public class ModelTestHelper {
    
    /**
     * Creates and returns a String message containing detailed information 
     * about a ConstraintViolation that has occurred. Information such as the 
     * name of the model class entity in  which the violation happened, the 
     * property path to the value from the root bean, and the value failing to 
     * pass the constraint is returned as a String.
     * 
     * @param e The constraint violation exception that has occurred.
     * @return  A String containing detailed information about a 
     *          ConstraintViolation that has occurred.
     */
    public static String getConstraintViolationMessage(final ConstraintViolationException e) {
        final StringBuilder msg = new StringBuilder();
        for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations()
                .iterator(); iter.hasNext();) {
            final ConstraintViolation<?> cv = iter.next();
            msg.append("[")
               .append(cv.getRootBean().getClass().getName())
               .append(".")
               .append(cv.getPropertyPath())
               .append(": ")
               .append(cv.getMessage())
               .append(" (invalid value = ")
               .append(cv.getInvalidValue())
               .append(")")
               .append("]");
        }
        return msg.toString();
    }
}
