package gov.hawaii.digitalarchives.hida.kukini.provenance;

import java.util.Map;

/**
 * Interface that is used to extract machine information from the
 * user's machine.
 * 
 * @author Keone Hiraide
 */
public interface MachineInfoExtractor {
    
    public Map<String,String> getMachineInfoPair();
    
    public void setMachineInfoPair(Map<String,String> machineInfoPair);
}
