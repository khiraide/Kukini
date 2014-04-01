package gov.hawaii.digitalarchives.hida.kukini.provenance;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import java.util.HashMap;
import java.util.Map;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SysInfo;
import org.hyperic.sigar.Who;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts machine information from the user's machine. This information
 * includes the user's ip address, mac address, machine name, machine
 * architecture, etc.
 * 
 * @author Keone Hiraide
 */
@ServiceProvider(service = MachineInfoExtractor.class)
public class MachineInfoExtractorImpl implements MachineInfoExtractor {
    private Map<String, String> machineInfoPair = new HashMap<>();
    private final Sigar sigar = new Sigar();
    private final Logger log = LoggerFactory.getLogger(MachineInfoExtractorImpl.class);
    
    /**
     * Extract network related information, system information, and user
     * information.
     */
    public MachineInfoExtractorImpl() {
        extractNetworkInformation();
        extractSystemInformation();
        extractSystemUserInformation();
    }
    
    @Override
    public Map<String,String> getMachineInfoPair() {
        log.debug("Entering getMachineInfoPair()");
        log.debug("Exiting getMachineInfoPair(): {}", this.machineInfoPair);
        return this.machineInfoPair;
    }
    
    @Override
    public void setMachineInfoPair(Map<String,String> machineInfoPair) {
        this.machineInfoPair = machineInfoPair;
    }
    
    /**
     * Extracts network information such the user's ip address, mac address,
     * netMask, domain name, and host name.
     */
    private void extractNetworkInformation() {
        log.debug("Entering extractNetworkInformation()");
        NetInterfaceConfig netInterfaceConfig = null;
        NetInfo netInfo = null;
        try {
            netInterfaceConfig = sigar.getNetInterfaceConfig(null);
            this.machineInfoPair.put("interface", netInterfaceConfig.getName());
            this.machineInfoPair.put("ipAddress", netInterfaceConfig.getAddress());
            this.machineInfoPair.put("macAddress", netInterfaceConfig.getHwaddr());
            this.machineInfoPair.put("netMask", netInterfaceConfig.getNetmask());
       
            netInfo = sigar.getNetInfo();
            this.machineInfoPair.put("hostName", netInfo.getHostName());
            this.machineInfoPair.put("domainName", netInfo.getDomainName());
            this.machineInfoPair.put("defaultGateway", netInfo.getDefaultGateway());
            this.machineInfoPair.put("primaryDns", netInfo.getPrimaryDns());
            this.machineInfoPair.put("secondaryDns", netInfo.getSecondaryDns());
        } catch (SigarException e) {
            String errorMessage = "Failed to extract the user's network information";
            log.error(errorMessage, e);
            throw new HidaException(errorMessage, e);
            
        }
        log.debug("Exiting extractNetworkInformation()");
    }
    
    /**
     * Extracts system information such the user's machine name, 
     * machine architecture, vendor, etc.
     */
    private void extractSystemInformation() {
        log.debug("Entering extractSystemInformation()");
        try {
            SysInfo sysInfo = new SysInfo();
            sysInfo.gather(sigar);
            this.machineInfoPair.put("systemName", sysInfo.getName());
            this.machineInfoPair.put("systemMachine", sysInfo.getMachine());
            this.machineInfoPair.put("systemDescription", sysInfo.getDescription());
            this.machineInfoPair.put("systemArchitecture", sysInfo.getArch());
            this.machineInfoPair.put("systemPatchLevel", sysInfo.getPatchLevel());
            this.machineInfoPair.put("systemVendor", sysInfo.getVendor());
            this.machineInfoPair.put("systemVendorCodeName", sysInfo.getVendorCodeName());
            this.machineInfoPair.put("systemVendorVersion", sysInfo.getVendorVersion());
            this.machineInfoPair.put("systemVersion", sysInfo.getVersion());
            
        } catch (SigarException e) {
            String errorMessage = "Failed to extract the user's system information";
            log.error(errorMessage, e);
            throw new HidaException(errorMessage, e);
        }
        log.debug("Exiting extractSystemInformation()");
    }
    
    /**
     * Extracts user information such as the username of the user
     * that is logged into the machine. Note that this username is NOT
     * the username of the user logged in to Kukini.
     */
    private void extractSystemUserInformation() {
        log.debug("Entering extractSystemUserInformation()");
        try {
            Who[] whoList = sigar.getWhoList();
            for (int i=0; i < whoList.length; i++) {
                this.machineInfoPair.put("userDevice" + i, whoList[i].getDevice());
                this.machineInfoPair.put("host" + i, whoList[i].getHost());
                this.machineInfoPair.put("user" + i, whoList[i].getUser());
                this.machineInfoPair.put("machineTime" + i, String.valueOf(whoList[i].getTime()));
            }  
        } catch (SigarException e) {
            String errorMessage = "Failed to extract the user's user information";
            log.error(errorMessage, e);
            throw new HidaException(errorMessage, e);
        }
        log.debug("Exiting extractSystemUserInformation()");
    }
}