package gov.hawaii.digitalarchives.hida.kukini.provenance;

/**
 * Model object used to hold user information. If the user logins
 * in successfully, this module will query the HIDA LDAP server
 * to retrieve the user information.
 * 
 * @author Keone Hiraide
 */
public interface UserInformation {
    
    /**
     * @return The full name of the user.
     */
    public String getFullName();
    
    /**
     * @param fullName The full name of the user to set.
     */
    public void setFullName(String fullName);
    
    /**
     * @return The department of the user. E.g: The Department of 
     *         Accounting and General Services.
     */
    public String getDepartment();
    
    /**
     * @param department The user's department to set. E.g: The Department of
     *                   Accounting and General Services.
     */
    public void setDepartment(String department);
    
    /**
     * @return The user's division. E.g: Hawaii State Archives.
     */
    public String getDivision();
    
    /**
     * @param division The user's division to set. E.g: The Department of
     *                 Accounting and General Services.
     */
    public void setDivision(String division);
    
    /**
     * @return The user's branch. E.g: Digital Archives.
     */
    public String getBranch();
    
    /**
     * @param branch The user's branch to set. E.g: Digital Archives.
     */
    public void setBranch(String branch);
    
    
}
