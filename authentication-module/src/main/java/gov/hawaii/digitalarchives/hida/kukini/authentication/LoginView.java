package gov.hawaii.digitalarchives.hida.kukini.authentication;

import org.openide.modules.ModuleInstall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays the login dialog to the user at startup.
 * 
 * @author Keone Hiraide
 */

public class LoginView extends ModuleInstall {

    private final Logger log = LoggerFactory.getLogger(LoginView.class);
    
    @Override
    public void restored() {
        log.debug("Entering restored()");
        LoginHandler.getDefault().showLoginDialog();
        log.debug("Exiting restored()");
    }
}
