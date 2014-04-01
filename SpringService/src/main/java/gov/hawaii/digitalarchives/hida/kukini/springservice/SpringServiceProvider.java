package gov.hawaii.digitalarchives.hida.kukini.springservice;

/**
 * Service which allows the modules within Kukini
 * the ability to retrieve beans from a Spring Context.
 * 
 * @author Keone Hiraide
 */
public interface SpringServiceProvider {
    /**
     * Retrieves a bean from a spring context by name.
     * 
     * @param beanName The name of the bean that you'd like to retrieve
     *                 from a spring context.
     * @return The bean declared within a spring context.
     */
    public Object getBean(String beanName);
}
