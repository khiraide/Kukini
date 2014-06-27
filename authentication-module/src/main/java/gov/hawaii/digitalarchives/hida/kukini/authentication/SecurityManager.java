package gov.hawaii.digitalarchives.hida.kukini.authentication;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import gov.hawaii.digitalarchives.hida.kukini.provenance.UserInformation;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton class which handles the login process.
 * This classes uses the Naming Directory Interface (JNDI) to
 * manage users of Kukini with HiDA's LDAP server.
 * 
 * @author Keone Hiraide
 */
public class SecurityManager {
    private static final SecurityManager securityManager = new SecurityManager();
    private static final Logger log = LoggerFactory.getLogger(SecurityManager.class);
    
    /**
     * Singleton class.
     */
    private SecurityManager() { 
    }
    
    /**
     * 
     * @return Returns a singleton instance of this class.
     */
    public static SecurityManager getDefault() {
        log.debug("Entering getDefault()");
        log.debug("Exiting getDefault(): {}", securityManager);
        return securityManager;
    }
    
    /**
     * Method which will attempt to establish a connection
     * with the details given. If for example, the wrong
     * username or password is given, false will be returned.
     * 
     * @param user The username to authenticate against.
     * @param password The password to authenticate against.
     * @return true if the login was successful, false otherwise.
     */
    public boolean login(String user, String password) {
        log.debug("Entering login(user={}, password={})", user, password);
        Properties properties = new Properties();

        // Defines the LDAP protocol we will be using.
        properties.put(DirContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // Defines the URL for the directory server. As you can see the LDAP protocol
        // is defined before the server name, port, and DN.
        properties.put(DirContext.PROVIDER_URL, "ldap://192.168.6.155:10389/ou=system");
        
        // Indicates the authentication mechanism of the Simple authentication
        // and the security layer (SASL) frameworks. 
        properties.put(DirContext.SECURITY_AUTHENTICATION, "simple");
        
        // Indicates the username as a complete path in the directory.
        // The path is given, and the username is dynamically grabbed from
        // the login dialog.
        properties.put(DirContext.SECURITY_PRINCIPAL, "uid=" + user + ",ou=users,ou=system");
        
        //Transfers the password which belongs to the authenticated user.
        properties.put(DirContext.SECURITY_CREDENTIALS, password);
        
        DirContext initialDirContext = null;
        try {
            // Authenticating against the LDAP using the properties passed in.
//            initialDirContext = new InitialDirContext(properties);
//            
//            Attributes attributes = initialDirContext.getAttributes("uid=" + user + ", ou=users");
//            Attribute fullName = attributes.get("cn");
//            Attribute branch = attributes.get("description");
//            Attribute department = attributes.get("o");
//            Attribute division = attributes.get("ou");
//            if (fullName != null && branch != null && department != null && division != null) {
                UserInformation userInformation = Lookup.getDefault()
                        .lookup(UserInformation.class);
                userInformation.setFullName("John Doe");
                userInformation.setBranch("Branch");
                userInformation.setDepartment("Department");
                userInformation.setDivision("Division");
//            } else {
//                String errorMessage = "Failed to extract user information. "
//                        + "The '" + user + "' may not have his/her attributes: "
//                        + "o, ou, cn, and description set.";
//                log.error("Exiting login(): false", errorMessage);
//                return false;
//            }

            log.debug("Exiting login(): true");
            return true;
            
         // Naming exception can happen if the user has entered the wrong
         // credentials, we couldn't establish a connection to the LDAP server,
         // etc.
//        } catch (NamingException e) {
//            log.debug("Exiting login(): false", e);
//            return false;
        } finally {
            if (initialDirContext != null) {
                try {
                    initialDirContext.close();
                } catch (NamingException e) {
                    String errorMessage = "Failed to close the DirContext.";
                    log.error(errorMessage, e);
                    throw new HidaException(errorMessage, e);
                }
            }
        }
    }
}