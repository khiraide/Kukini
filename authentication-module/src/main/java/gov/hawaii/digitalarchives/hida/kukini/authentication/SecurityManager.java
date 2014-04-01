package gov.hawaii.digitalarchives.hida.kukini.authentication;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import gov.hawaii.digitalarchives.hida.kukini.provenance.UserInformation;
import java.io.IOException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import org.openide.util.Exceptions;
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
    
    private SecurityManager() { 
    }
    
    public static SecurityManager getDefault() {
        log.debug("Entering getDefault()");
        log.debug("Exiting getDefault(): {}", securityManager);
        return securityManager;
    }
    
    /**
     * Method which will attempt to establish a connection
     * with the details given. If for example, the wrong
     * username or password is given, a NamingException is thrown.
     * 
     * @param user The username to authenticate against.
     * @param password The password to authenticate against.
     * @return true if the login was successful, false otherwise.
     */
    public boolean login(String user, String password) {
        log.debug("Entering login(user={}, password={})", user, password);
        
        Properties properties = new Properties();
        System.setProperty("javax.net.ssl.trustStore", 
                this.getClass().getResource("/trusted2.ks").getPath());
        
        properties.put(DirContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // Defines the URL for the directory server. As you can see the LDAP protocol
        // is defined before the server name, port, and DN.
        properties.put(DirContext.PROVIDER_URL, "ldap://localhost:10389/ou=system");
        
        try {
            // Authenticating against the LDAP using the properties passed in.
            LdapContext initialDirContext = new InitialLdapContext(properties, null);

            // Start TLS
            StartTlsResponse tls =
                (StartTlsResponse) initialDirContext.extendedOperation(new StartTlsRequest());
            try {
                
                // Handshake with the LDAP server.
                tls.negotiate();
                
                // Indicates the authentication mechanism of the Simple authentication
                // and the security layer (SASL) frameworks. 
                initialDirContext.addToEnvironment(DirContext.SECURITY_AUTHENTICATION, "simple");

                // Indicates the username as a complete path in the directory.
                // The path is given, and the username is dynamically grabbed from
                // the login dialog.
                initialDirContext.addToEnvironment(DirContext.SECURITY_PRINCIPAL, 
                        "uid=" + user + ",ou=users,ou=system");
                
                //Transfers the password which belongs to the authenticated user.
                initialDirContext.addToEnvironment(DirContext.SECURITY_CREDENTIALS, password);
                
                Attributes attributes = initialDirContext.getAttributes("uid=" + user + ", ou=users");
                Attribute fullName = attributes.get("cn");
                Attribute branch = attributes.get("description");
                Attribute department = attributes.get("o");
                Attribute division = attributes.get("ou");
                if (fullName != null && branch != null && department != null && division != null) {
                    UserInformation userInformation = Lookup.getDefault().lookup(UserInformation.class);
                    userInformation.setFullName((String)fullName.get());
                    userInformation.setBranch((String)branch.get());
                    userInformation.setDepartment((String)department.get());
                    userInformation.setDivision((String)division.get());
                } else {
                    String errorMessage = "Failed to extract user information. "
                            + "The '" + user + "' may not have his/her attributes: "
                            + "o, ou, cn, and desciption set.";
                    log.error(errorMessage);
                    throw new HidaException(errorMessage);
                }
                tls.close();

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            log.debug("Exiting login(): true");
            return true;

         // Naming exception can happen if the user has entered the wrong
         // credentials, we couldn't establish a connection to the LDAP server,
         // etc.
        } catch (NamingException e) {
            log.debug("Exiting login(): false", e);
            return false;
        }  
    }
}