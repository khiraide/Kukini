//package gov.hawaii.digitalarchives.hida.kukini.authentication;
//
//import gov.hawaii.digitalarchives.hida.kukini.provenance.UserInformation;
//import org.openide.util.Lookup;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
///**
// * Tests whether user information of an authorized user
// * can be extracted at login time.
// *
// * @author Keone Hiraide
// */
//public class UserInformationTest {
//    private final String validUsername = "Keone";
//    private final String validUsername2 = "JohnDoe";
//    private final String validPassword = "aloha";
//    
//    @Test
//    public void testUserInformationExtraction() {
//        // Login with the crendentials of an authorized user.
//        SecurityManager.getDefault().login(validUsername, validPassword);
//        
//        // Check whether the user information that is extracted at login time
//        // is correct.
//        UserInformation userInformation = Lookup.getDefault().lookup(UserInformation.class);
//        Assert.assertEquals(userInformation.getFullName(), "Keone Hiraide");
//        Assert.assertEquals(userInformation.getDepartment(), "Department of Accounting and "
//                + "General Services");
//        Assert.assertEquals(userInformation.getDivision(), "Hawaii State Archives");
//        Assert.assertEquals(userInformation.getBranch(), "Digital Archives");
//       
////        // Login again with a different user.
////        SecurityManager.getDefault().login(validUsername2, validPassword);
////        
////        // The user information should change to match the currently logged in
////        // user.
////        userInformation = Lookup.getDefault().lookup(UserInformation.class);
////        Assert.assertEquals(userInformation.getFullName(), "John Doe");
////        Assert.assertEquals(userInformation.getDepartment(), "Department of the Doe");
////        Assert.assertEquals(userInformation.getDivision(), "Doe Division");
////        Assert.assertEquals(userInformation.getBranch(), "Doe Branch");
//    }
//}
