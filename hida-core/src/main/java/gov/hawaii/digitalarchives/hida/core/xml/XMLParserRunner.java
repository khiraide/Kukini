package gov.hawaii.digitalarchives.hida.core.xml;


import gov.hawaii.digitalarchives.hida.core.model.preservationplan.PreservationPlan;
import gov.hawaii.digitalarchives.hida.core.model.rtp.RecordsTransmittalPlan;
import gov.hawaii.digitalarchives.hida.core.xml.preservationplan.PreservationPlanParserImpl;
import gov.hawaii.digitalarchives.hida.core.xml.rtp.RecordsTransmittalPlanParserImpl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * This class provides developers with the ability to persist model objects 
 * into the database via the command line by specifying the model object xml
 * file type and the path to a model object xml file.
 * 
 * @author Keone Hiraide
 *
 */
public class XMLParserRunner {
    
    /**
     * Provides users the ability to persist information into the database
     * via the command line. The command line arguments include the type of 
     * model object xml that you want to persist, as well as the path to this
     * model object xml file.
     * <p>
     * Example of usage: $mvn exec:java -Dexec.mainClass="gov.hawaii
     * .digitalarchives.hida.core.xml.XMLParserRunner" \
     * -Dexec.args="rtp /home/keone/Desktop/workspace/hida-core-155/src
     * /test/resources/RTP_XML.xml"
     * <p>
     * The command above will persist a records transmittal plan into the 
     * database. Its fields will be populated according to the information 
     * found in the RTP_XML.xml file.
     * 
     * @param args args[0] = <The type of xml file> 
     *             args[1] = <The path to the model object xml file>.
     */
    public static void main(String[] args) {
        final AbstractApplicationContext context = 
                new ClassPathXmlApplicationContext("/META-INF/spring/applicationContext.xml");

        XMLParser<?> parser = null;
        
        // We are expecting two arguments.
        if (args.length <= 0 || args.length >= 3) {
            String errorMessage = "Please enter command in the form of: <type> <path to file>\n" +
                    "Example: 'rtp /home/hida/rtpXmlFile.xml'\n" +
                    "The supported types include: 'rtp', 'pp'.\n";
            System.out.print(errorMessage);
        }
        else {      
            try {
                Path path = Paths.get(args[1]);
                switch (args[0]) {
                case "rtp":
                    parser = new RecordsTransmittalPlanParserImpl();   
                    RecordsTransmittalPlan rtp = (RecordsTransmittalPlan) parser.parse(path);
                    rtp.persist();
                    break;

                case "pp":
                    parser = new PreservationPlanParserImpl();   
                    PreservationPlan pp = (PreservationPlan) parser.parse(path);
                    pp.persist();
                    break;
                    
                default:
                   System.out.println("Unsupported type: '" + args[0] + 
                           "' The supported types include, 'rtp', 'pp'.");
                }
            } 
            catch (RuntimeException e) { // JPA exceptions are represented by a hierarchy of 
                                         // unchecked exceptions.
                e.printStackTrace();
            }
        }  
    }
}
