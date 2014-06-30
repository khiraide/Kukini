package gov.hawaii.digitalarchives.hida.core.xml;

import java.nio.file.Path;

/**
 * Interface which various XML parsers will implement.
 * 
 * @author Keone Hiraide
 */
public interface XMLParser<T> {
    /**
     * Parses a model object XML file and returns a model object instance
     * with its fields set by the information that was parsed from the model
     * object XML file.
     * 
     * @param pathToFile The path to the model object xml file that will be
     *                   parsed.
     * @return A model object instance with its fields set by the information
     *         that was parsed from the model object XML file.
     */
    public T parse(Path pathToFile);
}
