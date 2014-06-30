package gov.hawaii.digitalarchives.hida.core.util;

/** Utility class containing static methods pertaining to the file system.
 * 
 * @author Micah Takabayashi */
public class FileSystem
{
    /** Convert a URI (as a String) into a form usable by the OS as a file name
     * by escaping special characters.
     * 
     * Very minimal. For the moment, only escapes colons and forward slashes.
     * Should not be treated as a general-purpose solution.
     * 
     * @param name Base String to escape.
     * @return A copy of the input String with special file system characters
     *         escaped, making the string suitable for use as a file name. */
    public static String uriToFileName(String name) {
        // TODO, this should be sufficient for escaping Hark PIDs on Linux. A
        // more general solution may come later if needed.
        name = name.replace(':', '_');
        name = name.replace('/', '-');
        
        return name;
    }
}
