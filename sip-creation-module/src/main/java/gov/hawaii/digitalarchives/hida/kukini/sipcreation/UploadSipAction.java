package gov.hawaii.digitalarchives.hida.kukini.sipcreation;

import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static gov.hawaii.digitalarchives.hida.kukini.sipcreation.Bundle.*;
import gov.hawaii.digitalarchives.hida.kukini.springservice.SpringServiceProvider;
import org.openide.util.Lookup;

@ActionID(
        category = "File",
        id = "gov.hawaii.digitalarchives.hida.kukini.sipcreation.UploadSipAction"
)
@ActionRegistration(
        displayName = "#CTL_UploadSipAction"
)
@ActionReference(path = "Menu/File", position = 1300)
@Messages("CTL_UploadSipAction=Upload")
/**
 * Creates an action which will be activated when the user selects
 * DataObjects. These DataObjects represent the files that the user currently
 * has selected. If the action has been executed, the selected DataObjects
 * will be copied, bagged, and then uploaded to HiDA.
 */
public class UploadSipAction implements ActionListener {

     // A List holding the files that the user currently has selected.
    private final List<DataObject> selectedContext;
    
     // Used in order to create and upload SIPS to HiDA.
    private final SipUploader sipUploader;
    
     // {@link RestTemplate} instance used to send POST requests to a servlet
     // in order to upload files.
    private final RestTemplate restTemplate;
    
     // Used for logging.
    private final Logger log = LoggerFactory.getLogger(UploadSipAction.class);
    

    /**
     * 
     * Retrieve the selected context, inject our Bag and restTemplate beans.
     * 
     * @param context The DataObjects that the user has currently selected.
     */
    public UploadSipAction(List<DataObject> selectedContext) {
        this.selectedContext = selectedContext;
        SpringServiceProvider ssp = Lookup.getDefault().lookup(SpringServiceProvider.class);
        this.sipUploader = (SipUploader) ssp.getBean("upload");
        this.restTemplate =(RestTemplate) ssp.getBean("restTemplate");
    }
    

    /**
     * Uploads the files that are currently selected to HiDA.
     * 
     * @param ev A semantic event which indicates that a component-defined 
     *            action occurred.
     */
    @Override
    @Messages({
         "# {0} - response",
         "unsuccessfulMessage=Could not transfer your records at this time. "
                 + "We apologize for inconvenience. "
                 + "\n Please contact the Hawaii State Digital Archives at 'hiraide@hawaii.edu' "
                 + "for support. \n Error: {0}",
         "successfulMessage=Your records have been successfully transferred. Thank you."
    })
    public void actionPerformed(ActionEvent ev) {
        log.debug("Entering actionPerformed(ev={})", ev);
       
        Path sipPath = null;
        try {
            sipPath = sipUploader.createSipFromContext(selectedContext);
            ResponseEntity<String> response = sipUploader.uploadSip(sipPath, restTemplate);
 
            // Were we able to successfully upload the SIP?
            if (response.getStatusCode() == HttpStatus.CREATED) {
                JOptionPane.showMessageDialog(null, successfulMessage());
            }
            else {
                JOptionPane.showMessageDialog(null,  unsuccessfulMessage(response));
                log.error("Upload failed with a response of: " + response);
            }
        } finally {
            try {
                if (sipPath != null) {     
                    FileUtils.forceDelete(sipPath.toFile());
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, unsuccessfulMessage(e));
                String errorMessage = "Failed to delete the sip.";
                log.error(errorMessage, e);
                throw new HidaIOException(errorMessage, e);
            }
        }
    }    
}
