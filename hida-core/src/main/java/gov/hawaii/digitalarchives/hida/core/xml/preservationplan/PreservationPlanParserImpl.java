package gov.hawaii.digitalarchives.hida.core.xml.preservationplan;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIdSyntaxException;
import gov.hawaii.digitalarchives.hida.core.model.preservationplan.FileFormat;
import gov.hawaii.digitalarchives.hida.core.model.preservationplan.FormatPlan;
import gov.hawaii.digitalarchives.hida.core.model.preservationplan.PreservationPlan;
import gov.hawaii.digitalarchives.hida.core.xml.XMLParser;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.URIException;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parses a PreservationPlan XML file and extracts the (info)
 * The parse() method within this class will
 * return a {@link PreservationPlan} instance with
 * its fields set by the information that was parsed from a model
 * object preservation plan file.
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class PreservationPlanParserImpl implements XMLParser<PreservationPlan> {

    /**
     * Defines the API to obtain DOM Document instances from an XML
     * document. This instance, a {@link Document} can be obtained
     * from XML.
     */
    private DocumentBuilder documentBuilder = null;
    
    private Logger log = null;
    
    /**
     * Sets the documentBuilder field which can be reused each time we call 
     * the parse method.
     */
    public PreservationPlanParserImpl() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            this.documentBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            String errorMessage = "documentBuilder was not configured correctly.";
            throw new HidaException(errorMessage, e);
        }
    }

    @Override
    public PreservationPlan parse(Path pathToFile) {
        log.debug("Entering parse(pathTofile = {})", pathToFile);
        Assert.notNull(pathToFile, "pathToFile cannot be null");
        
        // The PreservationPlan XML document that will be parsed.
        PreservationPlan preservationPlan = new PreservationPlan();
        Map<String, FormatPlan> formatPlans = new HashMap<String, FormatPlan>();
        
        try {
            Document ppXml = null; 
            ppXml = this.documentBuilder.parse(pathToFile.toFile());
            ppXml.getDocumentElement().normalize();
       
            // Setting the primaryId for the PreservationPlan.
            preservationPlan.setPrimaryId(ppXml.getDocumentElement().getAttribute("OBJID"));
            
            // Setting the createdDate for a PreservationPlan.
            String strCreatedDate = ppXml.getDocumentElement().getAttribute("createdate");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            preservationPlan.setCreatedDate(dateFormat.parse(strCreatedDate));
            
            // Setting the lastModifedDate for a PreservationPlan.
            String strLastModifedDate = ppXml.getDocumentElement().getAttribute("lastmoddate");
            preservationPlan.setLastModifiedDate(dateFormat.parse(strLastModifedDate));
            
            // Setting the label for a PreservationPlan.
            preservationPlan.setLabel(ppXml.getDocumentElement().getAttribute("LABEL"));
            
            
            NodeList nList = ppXml.getElementsByTagName("HiDA_MD:formatPlan");
            for (int i=0; i < nList.getLength(); i++) {
                FormatPlan formatPlan = new FormatPlan();                
                Element element = (Element) nList.item(i);
                
                // Setting a FormatPlan's primary id.
                formatPlan.setPrimaryId(element.getAttribute("ID"));
                
                // Setting the FormatPlan's fields.
                formatPlan.setNativeFormat(createFormatFromElement(element, "HiDA_MD:nativeFormat"));
                
                formatPlan.setPresentationFormat(createFormatFromElement(element, 
                        "HiDA_MD:presentationFormat"));
                
                formatPlan.setPreservationFormat(createFormatFromElement(element, 
                        "HiDA_MD:preservationFormat"));
                
                formatPlan.setThumbnailFormat(createFormatFromElement(element, "HiDA_MD:thumbnailFormat"));
                
                formatPlans.put(formatPlan.getNativeFormat().getPronomFormat(), formatPlan);
            }
            
            preservationPlan.setFormatPlans(formatPlans);
            
        } catch (ParseException e) {
            String errorMessage = "Failed to parse " + pathToFile;
            log.error(errorMessage, e);
            throw new HidaException(errorMessage, e);
        } catch (URIException e) {
            String errorMessage = "xml file '" +  pathToFile + "' contains malformed Format ID.";
            log.error(errorMessage, e);
            throw new HidaIdSyntaxException(errorMessage, e);
        } catch (SAXException e) {
            String errorMessage = pathToFile + " is malformed.";
            log.error(errorMessage, e);
            throw new HidaException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error reading " + pathToFile;
            log.error(errorMessage, e);
            throw new HidaIOException(errorMessage, e);
        }
        log.debug("Exiting parse(): {}", preservationPlan);
        return preservationPlan; 
    }
    
    /**
     * Helper method that will used to set the fields
     * within a Format instance.
     * 
     * @param eElement A format plan element within a preservation plan 
     *                 xml file.
     * @param tagName  The format tag name within the xml file.
     * @return format  A format with its pronom and format fields set.
     */
    private FileFormat createFormatFromElement(Element eElement, String tagName) {
        FileFormat format = new FileFormat();
        format.setPronomFormat(eElement.getElementsByTagName(tagName).item(0).getTextContent());
        format.setFormatName(((Element)eElement.getElementsByTagName(tagName).item(0)).getAttribute("ID"));
        return format;
    }
    
    /**
     * Sets the logger for this class.
     * 
     * @param log The logger for this class.
     */
    @AutowiredLogger
    public void setLog(Logger log) {
        this.log = log;
    }
}
