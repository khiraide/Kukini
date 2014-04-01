package gov.hawaii.digitalarchives.hida.kukini.authentication;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.slf4j.LoggerFactory;

/**
 * Singleton class which uses HiDA's active 
 * directory system for authentication. Provides the logic behind the 
 * authentication process for authenticating users with 
 * the HiDA active directory server.
 * 
 * @author Keone Hiraide
 */
public class LoginHandler implements ActionListener {
    private static final LoginHandler loginHandler = new LoginHandler();
    private LoginPanel loginPanel = new LoginPanel();
    private DialogDescriptor dialogDescriptor = null;
    private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);
    
    private LoginHandler() {
    }
    
    /**
     * Gets the singleton instance of loginHandler.
     * 
     * @return The singleton instance of login handler.
     */
    public static LoginHandler getDefault() {
        log.debug("Entering getDefault()");
        log.debug("Exiting getDefault(): {}", loginHandler);
        return loginHandler;
    }
    
    /**
     * Displays the login dialog to the user and implement the behavior of its
     * buttons.
     */
    public void showLoginDialog() {
        log.debug("Entering showLoginDialog()");
        dialogDescriptor = new DialogDescriptor(loginPanel, "Login", true, this);
        dialogDescriptor.setClosingOptions(new Object[]{});
        dialogDescriptor.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    // Our login dialog's "x" button is clicked.
                    if (e.getPropertyName().equals(DialogDescriptor.PROP_VALUE)
                    && e.getNewValue() == DialogDescriptor.CLOSED_OPTION) {
                        
                        //Exit the application.
                        LifecycleManager.getDefault().exit();
                    }
                }
        });
        
        // Login dialog will be displayed right after the splash screen is displayed.
        DialogDisplayer.getDefault().notifyLater(dialogDescriptor);
        log.debug("Exiting showLoginDialog()");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        log.debug("Entering actionPerformed(event={})", event);
        // Dialog's cancel button is clicked.
        if (event.getSource() == DialogDescriptor.CANCEL_OPTION) {
            // Exit the application.
            LifecycleManager.getDefault().exit();
        }
        else {
            //login();
            dialogDescriptor.setClosingOptions(null);
        }
        log.debug("Entering actionPerformed()");
    }
    
    /**
     * Pass to the security manager the username and password that the user
     * entered within the login dialogue. 
     */
    private void login () {
        log.debug("Entering login()");
        if (!SecurityManager.getDefault().login(loginPanel.getUsername(), 
                loginPanel.getPasswordField())) {
                JOptionPane.showMessageDialog(null, "Wrong username or password");   
        } else {
            dialogDescriptor.setClosingOptions(null);
        }
        log.debug("Exiting login()");
    }
}
