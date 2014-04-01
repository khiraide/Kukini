
package gov.hawaii.digitalarchives.hida.kukini.provenance;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import java.io.File;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to dynamically detect and install the appropriate SIGAR API native 
 * library which is used to extract machine information from the user's 
 * machine.
 * 
 * @author Keone Hiraide
 */
public class SigarNativeLibraryInstaller extends ModuleInstall {
    
    private final Logger log = LoggerFactory.getLogger(SigarNativeLibraryInstaller.class);
    
    @Override
    public void restored() {
        log.debug("Entering restored()");
       
        // Load the appropriate native library according to the user's system.
        File sigarDistFolder = InstalledFileLocator.getDefault().locate("modules/lib/" +
                SigarNativeLibraryUtil.getAppropriateNativeLibrary(), 
                "gov-hawaii-digtialarchives-hida-kukini-provenance", false);
        
        if (sigarDistFolder != null) {
            System.load(sigarDistFolder.getPath());
        }
        else {
            String errorMessage = "Failed to load the sigar native library.";
            log.error(errorMessage);
            throw new HidaException(errorMessage);
        }
        log.debug("Exiting restored()");
    }
}
