
package gov.hawaii.digitalarchives.hida.kukini.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 * Runs the auto-update process as soon as the GUI is displayed to
 * the user. Once the auto-update process has been complete, this module
 * will be closed until the next start-up.
 * 
 * @author Keone Hiraide
 */
public class AutoInstaller extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(AutoInstaller.class.getName());
    
    /**
     *  Invoke the autoupdate process when the UI of the window system is
     *  ready. It is guaranteed that only one runnable runs at a given time.
     *  
     */
    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new AutoInstallerImpl());
    }
    
    /**
     * Searches for any new updates from an update center, adds those
     * new updates/modules to a container, then installs those modules.
     */
    private static final class AutoInstallerImpl implements Runnable {
        
        /**
         * Holds the new modules that will be installed. Please see
         * {@link UpdateElement} for more information. UpdateElement represents
         * the modules from an UpdateCenter. It allows you to retrieve info
         * such as the name of the module, the category, the icon of the module,
         * etc. 
         */
        private List<UpdateElement> install = new ArrayList<UpdateElement>();
        
        /**
         * Holds the installed modules that will be updated. Please see
         * {@link UpdateElement} for more information. UpdateElement represents
         * the modules from an UpdateCenter. It allows you to retrieve info
         * such as the name of the module, the category, the icon of the module,
         * etc. 
         */
        private List<UpdateElement> update = new ArrayList<UpdateElement>();
        
        @Override
        public void run() {
            
            // Searches the update centers and loads this information about the 
            // provided modules.
            searchNewAndUpdatedModules();
            
            // Install the new modules.
            OperationContainer<InstallSupport> installContainer = 
                    addToContainer(OperationContainer.createForInstall(), install);
            installModules(installContainer);
            
            // Update the modules that are already installed.
            OperationContainer<InstallSupport> updateContainer = 
                    addToContainer(OperationContainer.createForUpdate(), update);
            installModules(updateContainer);
        }
        
        /**
         * Search the update centers and loads this information about the 
         * provided modules.
         */
        public void searchNewAndUpdatedModules () {
            
            // Getting all the update centers.
            for (UpdateUnitProvider provider : UpdateUnitProviderFactory
                    .getDefault().getUpdateUnitProviders(false)) {
                try {
                    // Just something you call in order to refresh
                    // the update center.
                    provider.refresh(null, true);
                    
                } catch (IOException ex) {
                    LOG.severe(ex.getMessage());
                }
            }

            // Getting the UpdateUnits, which represents the modules
            // contained with the update centers.
            for (UpdateUnit unit : UpdateManager.getDefault().getUpdateUnits()) {
                // Check if there are any new modules to be updated
                // or installed.
                if (!unit.getAvailableUpdates().isEmpty()) {
                    // This module has not been installed yet so add it to our
                    // container that will be used to install these new modules.
                    if (unit.getInstalled() == null) {
                        install.add(unit.getAvailableUpdates().get(0));
                        
                    // We're updating a module that has already been installed.
                    } else {
                        update.add(unit.getAvailableUpdates().get(0));
                    }
                }
            }
        }

        /**
         * Add the modules to an {@link OperationContainer}. Operation 
         * containers are used in order to manage installing, un-installing,
         * disabling, enabling, and updating of our application's modules. 
         * 
         * See {@link OperationContainer}'s API online at:
         * 
         * http://bits.netbeans.org/dev/javadoc/org-netbeans-modules-autoupdate-services/org/netbeans/api/autoupdate/OperationContainer.html
         * 
         * @param container The container that you will use in order to either 
         *                   install, update, un-install, disable, or enable 
         *                   modules. You can create this container by using 
         *                   the OperationContainer's  static methods such as: 
         *                   OperationContainer.createForInstall() or 
         *                   OperationContainer.createForUpdate().
         * 
         * @param modules The list of modules to process.
         * 
         * @return A ready to use OperationContainer containing the modules 
         *         to install, update, disable, enable, etc.
         */
        public OperationContainer<InstallSupport> addToContainer
        (OperationContainer<InstallSupport> container, List<UpdateElement> modules) {
            for (UpdateElement updateElement : modules) {
                
                // Check if the module is compatible with the container.
                if (container.canBeAdded(updateElement.getUpdateUnit(), updateElement)) {
                    OperationContainer.OperationInfo<InstallSupport> operationInfo = 
                            container.add(updateElement);
                    // Does the module need any dependencies?
                    if (operationInfo != null) {
                        
                        // Do we have any broken dependencies? If so, 
                        // log this, and continue adding the modules that can be
                        // installed or updated. Note: An instance of OperationInfo has a method
                        //getBrokenDependencies(). Only plugins with no broken dependencies can be
                        // installed. Otherwise, plugins with such broken dependencies won't be 
                        // loaded by NetBeans Module System.
                        Collection<String> brokenDependencies = operationInfo.getBrokenDependencies();
                        if (!brokenDependencies.isEmpty()) {
                            String logMessage = "Plugin " + operationInfo + 
                                    " cannot be installed because some dependencies"
                                    + "cannot be satisfied: " + brokenDependencies;
                            LOG.severe(logMessage);
                            JOptionPane.showMessageDialog(null, logMessage);
                        }
                        else {
                            // Adding the dependencies required in order to install
                            // or update the module.
                            container.add(operationInfo.getRequiredElements());
                        }
                    }
                }
                else {
                    String logMessage = "The update element " 
                    + updateElement.getUpdateUnit() + " was not compatible with"
                    + " OperationContainer " + container;
                    LOG.severe(logMessage);
                    JOptionPane.showMessageDialog(null, logMessage);
                }
            }
            
            return container;
        }

        /**
         * Download, validate, and install the new modules and afterwards,
         * notify the user about it.
         * 
         * @param container The container holding the modules to be installed.
         *                  This container is created by the addToContainer
         *                  method.
         */
        public void installModules(OperationContainer<InstallSupport> container) {
            try {
                InstallSupport support = container.getSupport();

                if (support != null) {
                    // Download the modules from the update center(s).
                    InstallSupport.Validator validator = support.doDownload(null, true, true);
                    
                    // Validate them, get the installer.
                    InstallSupport.Installer installer = support.doValidate(validator, null);
                    
                    // Need to restart or not depending on what was read from
                    // a modules info.xml file contained with its nbm package.
                    OperationSupport.Restarter restarter = support.doInstall(installer, null);

                    // Looks like this module needs a restart in order for it 
                    // to be successfully be installed.
                    if (restarter != null) {
                        
                        // Setting the information contained within a dialog
                        // that will be displayed to the user.
                        NotifyDescriptor nd = new NotifyDescriptor
                                .Message("Updates were automatically detected and installed. "
                                + "Your Kukini application will now restart.", 
                                        NotifyDescriptor.INFORMATION_MESSAGE);
                        
                        // Display a notification dialog to the user.
                        DialogDisplayer.getDefault().notify(nd);

                        try {
                            // Restarting!!!
                            support.doRestart(restarter, null);
                        } catch (OperationException ex) {
                            LOG.severe(ex.getMessage());
                        }
                    }
                }
            } catch (OperationException ex) {
                LOG.severe(ex.getMessage());
            }
        }
    }
}