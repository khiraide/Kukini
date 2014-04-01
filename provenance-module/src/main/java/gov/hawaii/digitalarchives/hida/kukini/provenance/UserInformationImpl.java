package gov.hawaii.digitalarchives.hida.kukini.provenance;

import org.openide.util.lookup.ServiceProvider;

/**
 * Model object represents user information including: 
 * Department, division, branch, and full name.
 * 
 * @author Keone Hiraide
 */
@ServiceProvider(service = UserInformation.class)
public class UserInformationImpl implements UserInformation {

    private String fullName;
    private String department;
    private String division;
    private String branch;

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getDepartment() {
        return department;
    }
    
    @Override
    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getDivision() {
        return division;
    }

    @Override
    public void setDivision(String division) {
        this.division = division;
    }

    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public void setBranch(String branch) {
        this.branch = branch;
    }
}
