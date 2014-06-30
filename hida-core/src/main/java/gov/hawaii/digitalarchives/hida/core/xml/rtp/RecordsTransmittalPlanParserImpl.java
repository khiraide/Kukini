package gov.hawaii.digitalarchives.hida.core.xml.rtp;


import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;
import gov.hawaii.digitalarchives.hida.core.model.rtp.RecordsTransmittalPlan;
import gov.hawaii.digitalarchives.hida.core.xml.XMLParser;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Parses an RecordsTransmittalPlan XML file and extracts the HiDA ARK
 * and Persisent ID. The parse() method within this class will
 * return a {@link RecordsTransmittalPlan} instance with
 * its fields set by the information that was parsed from a model
 * object XML file
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class RecordsTransmittalPlanParserImpl implements XMLParser<RecordsTransmittalPlan> {
    
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
    public RecordsTransmittalPlanParserImpl() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            this.documentBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            String errorMessage = "documentBuilder was not configured correctly.";
            throw new HidaException(errorMessage, e);
        }
    }
    
    @Override
    public RecordsTransmittalPlan parse(Path pathToFile) {
        log.debug("Entering parse(pathTofile = {})", pathToFile);
        Assert.notNull(pathToFile, "pathToFile cannot be null");
        Document rtpXml = null;
        RecordsTransmittalPlan rtp = new RecordsTransmittalPlan();

        try {
            rtpXml = this.documentBuilder.parse(pathToFile.toFile());
            rtpXml.getDocumentElement().normalize();
            
            // Getting the OBJID attribute from the RTP_XML.xml file.
            rtp.setPrimaryId(rtpXml.getDocumentElement().getAttribute("OBJID"));
            
            // Getting the element whose tag is <HiDA_MD:seriesInfo> in the
            // RTP_XML.xml file. 
            Element element = (Element)rtpXml.getElementsByTagName("HiDA_MD:seriesInfo").item(0);
            
            // Getting the value from the <HiDA_MD:parserPID> tag within RTP_XML.xml;
            rtp.setParserId(element.getElementsByTagName("HiDA_MD:parserPID").item(0)
                    .getTextContent());
        } catch (SAXException e) {
            String errorMessage = pathToFile + " is malformed.";
            log.error(errorMessage, e);
            throw new HidaException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error reading " + pathToFile;
            log.error(errorMessage, e);
            throw new HidaIOException(errorMessage, e);
        }
        log.debug("Exiting parse(): {}", rtp);
        return rtp; 
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
