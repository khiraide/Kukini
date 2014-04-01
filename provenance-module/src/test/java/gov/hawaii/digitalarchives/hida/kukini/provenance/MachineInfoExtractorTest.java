package gov.hawaii.digitalarchives.hida.kukini.provenance;

import java.util.Map;
import org.openide.util.Lookup;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests whether we can successfully extract the machine information
 * using the Sigar API.
 * 
 * @author Keone Hiraide
 */
public class MachineInfoExtractorTest {
    
    @Test
    public void testMachineExtractor() {
        MachineInfoExtractor machineInfoExtractor = Lookup.getDefault().lookup(MachineInfoExtractor.class);
        Map<String, String> machineInfo = machineInfoExtractor.getMachineInfoPair();
        Assert.assertNotNull(machineInfo.get("interface"));
        Assert.assertNotNull(machineInfo.get("ipAddress"));
        Assert.assertNotNull(machineInfo.get("macAddress"));
        Assert.assertNotNull(machineInfo.get("netMask"));
        Assert.assertNotNull(machineInfo.get("hostName"));
        Assert.assertNotNull(machineInfo.get("domainName"));
        Assert.assertNotNull(machineInfo.get("defaultGateway"));
        Assert.assertNotNull(machineInfo.get("primaryDns"));
        Assert.assertNotNull(machineInfo.get("secondaryDns"));
        
    }
}
