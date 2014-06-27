package gov.hawaii.digitalarchives.hida.kukini.sipcreation;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.hawaii.digitalarchives.hida.bag.BagUtil;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;
import gov.hawaii.digitalarchives.hida.core.model.accession.Accession;
import gov.hawaii.digitalarchives.hida.core.model.record.Agent;
import gov.hawaii.digitalarchives.hida.core.model.record.ProducerInfo;
import gov.hawaii.digitalarchives.hida.core.util.ZipUtil;
import gov.hawaii.digitalarchives.hida.kukini.provenance.MachineInfoExtractor;
import gov.hawaii.digitalarchives.hida.kukini.provenance.UserInformation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Contains methods for creating and uploading SIPs to servlets.
 * 
 * @author Keone Hiraide
 */
public class SipUploaderImpl implements SipUploader {
    
     // The URL to the servlet that will accept file uploads.
    private final String sipUploaderServletURL;

     // Bag module.
    @Autowired
    private BagUtil bagUtil;
    
     // Used for logging purposes.
    @AutowiredLogger
    private Logger log;

     // Used for json data binding for the creation of sip tags.
    private final ObjectMapper mapper = new ObjectMapper();
    

    // The ID of the records transmittal plan associated with the
    // SIP that is created and uploaded.
    //
    //TODO: Retrieve the RTP ID from an actual RTP
    //      which corresponds to the user that
    //      is currently logged to Kukini.
    private String rtpId = "ark:/0000/Stub";
    
    
    /**
     * Sets the URL to the servlet that will accept 
     * file upload requests.
     * 
     * @param sipUploaderServletURL The URL to the servlet that will accept 
     *                               file upload requests.
     */
    public SipUploaderImpl(String sipUploaderServletURL) {
        this.sipUploaderServletURL = sipUploaderServletURL;
    }

    @Override
    public Path createSipFromContext(List<DataObject> selectedContext, Path destinationDirectory) {
        log.debug("Entering createSipFromContext(selectedContext={} destinationDirectory={})", 
                selectedContext, destinationDirectory);
        Assert.notNull(selectedContext);
        Assert.notEmpty(selectedContext);
        
        try {
           // Create the bag structure.
            try {
                Path dataDirectory = Files.createDirectories(destinationDirectory
                        .resolve("accession/data"));
                
                Path rootDirectory = dataDirectory.getParent();
                
                // Copy selected files to "data" directory.
                copySelectedFilesToDirectory(selectedContext, FileUtil.
                        toFileObject(dataDirectory.toFile()));
                
                // Create sip tag within the "root" directory.
                createAccessionMetadata(rootDirectory);
                
                bagUtil.makeComplete(rootDirectory);
                
                
                // Make the bag in place at the destination directory.
                Path sipPath = ZipUtil.compress(destinationDirectory);
                    
                log.debug("Exiting createSipFromContext(): {}", sipPath);
                return sipPath;
                          
            } catch (IOException e) {
                String errorMessage = "Failed to create the SIP from the selected"
                        + " context.";
                log.error(errorMessage, e);
                throw new HidaIOException(errorMessage, e);
            }           
        } finally {
            try {
                if (destinationDirectory != null) {
                    FileUtils.forceDelete(destinationDirectory.toFile());
                }
            } catch (IOException e) {
                String errorMessage = "Failed to delete the temporary bag directory: '" 
                        + destinationDirectory + "'";
                log.error(errorMessage, e);
                throw new HidaIOException(errorMessage, e);
            }
        }
    }
    

    @Override
    public Path createSipFromContext(List<DataObject> selectedContext) {
        log.debug("Entering createSipFromContext(selectedContext={})", selectedContext);

        try {
            Path temporaryDestinationDirectory = Files.createTempDirectory(null);
            temporaryDestinationDirectory.toFile().deleteOnExit();
            temporaryDestinationDirectory = Files
                    .move(temporaryDestinationDirectory, temporaryDestinationDirectory
                    .resolveSibling("record_series_" + new Date().getTime()));
            
            Path sipPath = createSipFromContext(selectedContext, 
                temporaryDestinationDirectory);
            
            log.debug("Exiting createSipFromContext(): {}", sipPath);
            return sipPath;
        } catch (IOException e) {
            String errorMessage = "Failed to create the "
                    + "temporary destination directory.";
            log.error(errorMessage, e);
            throw new HidaIOException(errorMessage, e);
        } 
    }
    
    @Override
    public ResponseEntity<String> uploadSip(Path sipPath, RestTemplate restTemplate) {
        log.debug("Entering upload(sipPath={}, restTemplate={}", sipPath, restTemplate);
        Assert.notNull(sipPath);
        // Send a POST request to a servlet in order to upload the SIP to HiDA.
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new FileSystemResource(sipPath.toFile()));
        parts.add("rtpId", this.rtpId);
        ResponseEntity<String> response = restTemplate
            .postForEntity(this.sipUploaderServletURL, parts, 
                    String.class);
        
        log.debug("Exiting upload(): {}", response);
        return response;
    }
    
    /**
     * Creates an accession metadata sip tag that will be contained with
     * the SIP bag. This accession metadata sip tag is created by the 
     * serialization of an Accession Model Object in JSON format.
     * 
     * @param destinationDirectory The directory that the created 
     *                             accession metadata sip tag will reside in.
     * 
     * @return The path to the accession metadata sip tag. 
     */
    private Path createAccessionMetadata(Path destinationDirectory) {
        log.debug("Entering createAccessionMetadata(destinationDirectory = {})", 
                destinationDirectory);
        UserInformation userInformation = Lookup.getDefault().lookup(UserInformation.class);
        String department = userInformation.getDepartment();
        String division = userInformation.getDivision();
        ProducerInfo producerInfo = new ProducerInfo(department, division, 
                userInformation.getBranch());
        MachineInfoExtractor machineInfo = Lookup.getDefault().lookup(MachineInfoExtractor.class);
        Accession accession = new Accession();
        accession.setRtpId(this.rtpId);
        accession.setMachineInfo(machineInfo.getMachineInfoPair());
        accession.setProducerInfo(producerInfo);
        accession.setPreserver(new Agent("Hawaii State Archives", "Accessioning of Records"));
        accession.setTransfererName(userInformation.getFullName());
        accession.setAccessionCreationDate(new Date());
        accession.setTransferMethod("Kukini HTTPS");
        accession.setCreator(new Agent(department + ", " + division, "Records submitted to HIDA"));
        
        try {
            Path sipTagPath = destinationDirectory.resolve("accession.json");
            mapper.writeValue(sipTagPath.toFile(), accession);
            log.debug("Exiting createAccessionMetadata(): {}", sipTagPath);
            return sipTagPath;
            
        } catch (IOException e) {
            String errorMessage = "Failed to serialize accession model object";
            log.error(errorMessage, e);
            throw new HidaIOException(errorMessage, e);
        }
    }
    
    
    
    /**
     * Copies the files that the user has selected to a directory.
     * 
     * @param selectedContext A list containing the selected files.
     * @param destinationDirectory The directory to copy the selected files to. 
     */
    private void copySelectedFilesToDirectory(List<DataObject> selectedContext,
            FileObject destinationDirectory) {
        log.debug("Entering copySelectedFilesToDirectory(selectedContext={}, "
                + "destinationDirectory={})", selectedContext, destinationDirectory);
        for (DataObject dataObject : selectedContext) {
            FileObject selectedFile = dataObject.getPrimaryFile();        
            try {
                // Create the parent folder of the selected file within the
                // directory. The parent folders of the parent
                // folder is also created if they don't exist.
                FileObject parentOfFile = FileUtil.createFolder(new File(destinationDirectory.getPath() + 
                        FileUtil.normalizePath(selectedFile.getParent().getPath())
                                .replaceFirst("([a-zA-Z]):","/$1_Hida_Volume")));
           
                // Copy the file within the temporaryb ag directory.
                FileUtil.copyFile(selectedFile, parentOfFile, selectedFile.getName());
            } catch (IOException e) {
                String errorMessage = "Failed to copy the file " + selectedFile.getPath()
                        + " to " + destinationDirectory.getPath();
                log.error(errorMessage, e);
                throw new HidaIOException(errorMessage, e);
            }
            log.debug("Exiting copySelectedFilesToDirectory()");
        }
    }

    @Override
    public String getSipUploaderServletURL() {
        log.debug("Entering getSipUploaderServletURL()");
        log.debug("Exiting getSipUploaderServletURL(): {}", sipUploaderServletURL);
        return sipUploaderServletURL;
    }
}