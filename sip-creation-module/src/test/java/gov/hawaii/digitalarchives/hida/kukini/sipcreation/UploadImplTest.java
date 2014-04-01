//package gov.hawaii.digitalarchives.hida.kukini.sipcreation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import gov.hawaii.digitalarchives.hida.core.model.accession.Accession;
//import gov.hawaii.digitalarchives.hida.core.util.ZipUtil;
//import gov.hawaii.digitalarchives.hida.kukini.provenance.SigarNativeLibraryUtil;
//import gov.hawaii.digitalarchives.hida.kukini.provenance.UserInformation;
//import gov.hawaii.digitalarchives.hida.kukini.springservice.SpringServiceProvider;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URISyntaxException;
//import java.nio.file.FileSystems;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//import org.apache.commons.io.FileUtils;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.LocalFileSystem;
//import org.openide.loaders.DataObject;
//import org.openide.util.Lookup;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.client.MockRestServiceServer;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
//import org.testng.Assert;
//import org.springframework.web.client.RestTemplate;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
///**
// * Tests the {@link UploadImpl} class.
// * author Keone Hiraide
// */
//public class UploadImplTest {
//    
//     // Interface of class that we would like to test. 
//    private SipUploader upload = (SipUploader)Lookup.getDefault()
//            .lookup(SpringServiceProvider.class).getBean("upload");
//     // Used to represent the selected context.
//    private final List<DataObject> selectedContext = new ArrayList<>();
//    
//     // The URL to the mock servlet which will accept POST requests.
//    private String testSipUploaderServletURL = "http://localhost:8080/sipuploader/accessions";
//    
//    private Path sipPath;
//    
//    @BeforeTest
//    public void setup () throws URISyntaxException, IOException, Exception
//    {
//        
//        loadNativeLibraryFromProvenanceModule();
//        
//        // User information is normally extracted at login time. But in this
//        // case, we sill manually set the user information so that a SIP tag
//        // can properly be created. The reason is becauseAccessions contain ProducerInfo 
//        // objects, and ProducerInfo objects cannot have a null division, department, and branch.
//        UserInformation userInformation = Lookup.getDefault().lookup(UserInformation.class);
//        userInformation.setDivision("division");
//        userInformation.setDepartment("department");
//        userInformation.setBranch("branch");
//        userInformation.setFullName("John Doe");
//        
//        // Getting a FileObject which represents the directory which
//        // holds the resources used for testing.
//        FileObject testResourcesDirectory = new LocalFileSystem().getRoot()
//                .getFileObject("src/test/resources/testfiles");
//        
//        // Getting the file objects within the test resources directory
//        // and turning them into DataObjects. These DataObjects will then be
//        // stored in a List to be used to test our methods.
//        for (FileObject testResource : testResourcesDirectory.getChildren()) {
//            DataObject testResourceDataObject = DataObject.find(testResource);
//            Assert.assertNotNull(testResourceDataObject);
//            selectedContext.add(testResourceDataObject);
//        }
//    }
//    
//    @AfterMethod
//    public void deleteTemporaryDirectory() throws IOException, InterruptedException {
//        File sipFile = sipPath.toFile();
//        if (sipFile != null && sipFile.exists()) {
//            FileUtils.forceDelete(sipFile);
//        }
//    }
//    
//    /**
//     * Testing the bagSelectedContext method's ability to properly
//     * bag a collection of DataObjects which represents the
//     * selected context. The selected context represents the files
//     * that are currently selected within Kukini's GUI.
//     */
//    @Test
//    public void bagSelectedContextTest() throws InterruptedException{
//            
//        // If the bagging was successful, a path to the SIP
//        // should be returned.
//        sipPath = upload.createSipFromContext(selectedContext);
//        Assert.assertNotNull(sipPath);
//        try (ZipFile sipZipFile = new ZipFile(sipPath.toString())) {
//            for (DataObject selectedFile : selectedContext) {
//                boolean found = false;
//                Enumeration sipFiles = sipZipFile.entries();
//                while (sipFiles.hasMoreElements()) {
//                    ZipEntry sipFile = (ZipEntry)sipFiles.nextElement();
//                    if (sipFile.getName().contains(selectedFile.getPrimaryFile()
//                            .getPath()
//                            .replaceFirst("([a-zA-Z]):","/$1"))) {
//                        found = true;
//                        break;
//                    }
//                }
//                Assert.assertTrue(found, "The sip did not contain the selected file: " + 
//                        selectedFile.getName());
//            }
//        }
//        catch (IOException e) {
//            Assert.fail("Failed to create ZipFile instance.", e);
//        }
//    }
//    
//    /**
//     * Tests the bagSelectedContext method which takes in a destination
//     * parameter.
//     */
//    @Test 
//    public void bagSelectedContextTestWithDestination() {
//        try {
//            Path destinationDirectory = Files.createTempDirectory(null);
//            destinationDirectory.toFile().deleteOnExit();
//            sipPath = upload.createSipFromContext(selectedContext, destinationDirectory);
//        } catch (IOException e) {
//            Assert.fail("Failed to create destination directory.", e);
//        } 
//    }
//    
//    /**
//     * Tests a POST request to a mock server. This method tests the 
//     * {@link UploadImpl} class' uploadSip(Path, RestTemplate) method.
//     */
//    @Test
//    public void uploadSipPathTest() throws URISyntaxException, IOException {
//        RestTemplate restTemplate = new RestTemplate();
//        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
//        mockServer.expect(requestTo(testSipUploaderServletURL))
//                .andExpect(method(HttpMethod.POST))
//                .andRespond(withSuccess("uploadSuccessful", MediaType.APPLICATION_JSON));
//        
//        Path sipPath = Paths.get(this.getClass().getClassLoader().getResource("testBag.zip")
//                .toURI());
//        upload.uploadSip(sipPath, restTemplate);
//        mockServer.verify();
//    }
//    
//    /**
//     * Tests whether the SIP Tag within a bag can successfully be 
//     * deserialized into an Accession object instance.
//     */
//    @Test
//    public void sipTagTest() throws IOException, InterruptedException, URISyntaxException {
//        Path sipPath = Paths.get(this.getClass().getClassLoader().getResource("testBag.zip")
//                .toURI());
//        Path sipTagParent = sipPath.getParent();
//        Path sipTagPath = sipTagParent.resolve("accession.json");
//        try (java.nio.file.FileSystem fs = FileSystems.newFileSystem(sipPath, null)) {
//             ZipUtil.copyFile(fs.getPath("/accession/accession.json"), sipTagParent);
//            
//            // Deserializing the Accession sip tag to create an Accession.
//            ObjectMapper mapper = new ObjectMapper();
//            Accession accession = mapper.readValue(sipTagPath.toFile(), Accession.class);
//            Assert.assertEquals(accession.getRtpId(), "ark:/0000/Stub");
//            Assert.assertNotNull(accession.getMachineInfo());
//            Assert.assertNotNull(accession.getProducerInfo());
//            Assert.assertNotNull(accession.getPreserver());
//            Assert.assertNotNull(accession.getTransfererName());
//            Assert.assertNotNull(accession.getAccessionCreationDate());
//            Assert.assertNotNull(accession.getTransferMethod());
//            Assert.assertNotNull(accession.getCreator());
//        } finally {
//            if (sipTagPath != null) {
//                FileUtils.forceDelete(sipTagPath.toFile());
//            }
//        }
//    }
//    
//    /**
//     * Will load the appropriate sigar native library from the provenance
//     * module based on the architecture and OS of the user's machine.
//     */
//    private void loadNativeLibraryFromProvenanceModule() throws IOException {
//        ClassLoader syscl = Lookup.getDefault().lookup(ClassLoader.class);
//        String nativeLibrary = SigarNativeLibraryUtil.getAppropriateNativeLibrary();
//	File nativeLibraryFile = new File(nativeLibrary);
//        nativeLibraryFile.deleteOnExit();
// 
//        // Write the inputStream to a FileOutputStream
//        try (
//            InputStream in = syscl.getResourceAsStream(nativeLibrary);
//            OutputStream out = new FileOutputStream(nativeLibraryFile);
//        ) {
//            int read = 0;
//            byte[] bytes = new byte[1024];
//            while ((read = in.read(bytes)) != -1) {
//                out.write(bytes, 0, read);
//            }
//        }
//        System.load(nativeLibraryFile
//                .getAbsolutePath());
//    }
//}