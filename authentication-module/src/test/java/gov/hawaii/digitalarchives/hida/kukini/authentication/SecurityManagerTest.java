//package gov.hawaii.digitalarchives.hida.kukini.authentication;
//
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
///**
// * Test whether we can successfully authenticate against the HiDA
// * development LDAP server.
// * 
// * @author Keone Hiraide
// */
//public class SecurityManagerTest {
//    
//    private final String validUsername = "Keone";
//    private final String validPassword = "aloha";
//    
//    private final String invalidUsername = "John";
//    private final String invalidPassword = "doe";
//    
//    /**
//     * Test the authentication against the HiDA LDAP server with valid 
//     * credentials.
//     */
//    @Test
//    public void testValidLogin() {
//        Assert.assertTrue(SecurityManager.getDefault().login(validUsername, validPassword));
//    }
//    
//    /**
//     * Test the authentication against the HiDA LDAP server with a valid 
//     * username and an invalid password.
//     */
//    @Test
//    public void testValidUsernameInvalidPasswordLogin() {
//        Assert.assertFalse(SecurityManager.getDefault().login(validUsername, invalidPassword));
//    }
//    
//    /**
//     * Test the authentication against the HiDA LDAP server with an invalid 
//     * username and an valid password.
//     */
//    @Test
//    public void testInvalidUsernameValidPasswordLogin() {
//        Assert.assertFalse(SecurityManager.getDefault().login(invalidUsername, validPassword));
//    }
//    
//    /**
//     * Test the authentication against the HiDA LDAP server with an invalid
//     * username and password.
//     */
//    @Test
//    public void testInvalidLogin() {
//        Assert.assertFalse(SecurityManager.getDefault().login(invalidUsername, invalidPassword));
//    }
//}
