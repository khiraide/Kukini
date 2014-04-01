package gov.hawaii.digitalarchives.hida.kukini.springservice;

import org.openide.awt.StatusDisplayer;
import org.openide.util.lookup.ServiceProvider;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Creates the main spring context to be used throughout all the
 * other modules of Kukini.
 * 
 * @author Keone Hiraide
 */
@ServiceProvider(service = SpringServiceProvider.class)
public class SpringServiceProviderImpl implements SpringServiceProvider {
    
    // The spring context.
    private final AbstractApplicationContext ctx;
    

    /**
     * Creates the spring context.
     */
    public SpringServiceProviderImpl() {
        ctx = new ClassPathXmlApplicationContext("/spring/kukiniApplicationContext.xml");
        ctx.registerShutdownHook();
    }

    
    
    @Override
    public Object getBean(String beanName) {
        return ctx.getBean(beanName);
    }
    
    
    public void closeContext() {
        ctx.close();
    }
    
}
