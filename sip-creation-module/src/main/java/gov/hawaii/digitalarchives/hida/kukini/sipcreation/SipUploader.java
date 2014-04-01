package gov.hawaii.digitalarchives.hida.kukini.sipcreation;

import java.nio.file.Path;
import java.util.List;
import org.openide.loaders.DataObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Interface for creating and uploaded SIPs to a web servlet.
 * 
 * @author Keone Hiraide
 */
public interface SipUploader {
 
    /**
     * Creates a SIP using the @link{DataObject}s that are contained 
     * within a List. The DataObjects in this List represent the files that
     * the user currently has selected. The created SIP will reside
     * in the default temporary-file directory. 
     * 
     * @param selectedContext The list of DataObjects which represent the files
     *                        that the user currently has selected.
     * @return The path to the newly created SIP or the throwing
     *          of an exception if this method failed to create the SIP.
     */
    public Path createSipFromContext(List<DataObject> selectedContext);
    
    /**
     * Creates a SIP using the @link{DataObject}s that are contained 
     * within a List. The DataObjects in this List represent the files that
     * the user currently has selected. 
     * 
     * @param selectedContext The list of DataObjects which represent the files
     *                        that the user currently has selected.
     * @param destinationDirectory destinationDirectory The destination where 
     *                             the SIP will be bagged in place to.
     * 
     * @return  The path to the newly created SIP or the throwing
     *          of an exception if this method failed to create the SIP.
     */
    public Path createSipFromContext(List<DataObject> selectedContext, Path destinationDirectory);
    
    /**
     * Uploads the SIP to the Hawaii State Digital Archives.
     * 
     * @param sipPath The path to the SIP that will be uploadSiped.
     * @param restTemplate The {@link RestTemplate} that will be used in order
     *                     to create and execute an uploadSip request.
     * 
     * @return A {@link ResponseEntity} an instance which encapsulates 
     *         information about the upload such as the the HTTP status code of the 
     *         response, a message body, and headers. 
     */
    public ResponseEntity<String> uploadSip(Path sipPath, RestTemplate restTemplate);
}
