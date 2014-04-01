package gov.hawaii.digitalarchives.hida.kukini.provenance;

import java.util.HashMap;
import java.util.Map;

/**
 * Detects the architecture and operating system using 
 * java.lang.System and returns the name of the
 * native library that should be loaded for the sigar
 * API to work properly. 
 * 
 * 
 * @author Keone Hiraide
 */
public class SigarNativeLibraryUtil {
    
    public static String getAppropriateNativeLibrary() {
        Map<String, String> nativeLibaries = new HashMap<>();
        nativeLibaries.put("Windows64", "sigar-amd64-winnt.dll");
        nativeLibaries.put("Windows32", "sigar-x86-winnt.dll");
        nativeLibaries.put("Linux64", "libsigar-amd64-linux.so");
        nativeLibaries.put("Linux32", "libsigar-x86-linux.so");
        
        // Detecting ths operating system and archiecture of the user's machine.
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        
        if (osName.contains("Windows")) {
            osName = "Windows";
            
            // If the user is running a 32-bit jvm within a 64-bit OS,
            // the System.getProperty("os.arch") method will detect that
            // the user has a 32-bit architecture. Thus, System.getenv("ProgramFiles(x86)") 
            // will properly detect the archecture of the user's machine in this
            // case.
            if (osArch.contains("64")) {  
                osArch = "64";
            } else {
                osArch = "32";
            }
        }
        else {
            osName = "Linux";
            if (osArch.contains("64")) {
                osArch = "64";
            } else {
                osArch = "32";
            }
        }
        return nativeLibaries.get(osName + osArch);
    }
}
