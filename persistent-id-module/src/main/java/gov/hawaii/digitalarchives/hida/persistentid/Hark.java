package gov.hawaii.digitalarchives.hida.persistentid;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIOException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.cdl.noid.Noid;
import org.slf4j.Logger;
import org.springbyexample.util.log.AutowiredLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/** Implementation of the {@link Ark} interface that allows for easy access to
 * the different parts of an Ark URI. For more information see
 * https://wiki.ucop.edu/display/Curation/ARK
 * 
 * @author Micah Takabayashi */
public class Hark extends URI implements Ark
{
    /** Pattern that the ARK URI must follow (the ARK scheme identifier, a series
     * of digits for the NAAN, one or more characters for the name, and
     * optionally one or more characters for the qualifier, all separated by
     * forward slashes). */
    public static final Pattern ARK_FORMAT = Pattern.compile(Ark.SCHEME + ":/\\d+/.+(/.*)?");
    
    /** Pattern that the NAAN portion of the ARK must follow.*/
    public static final Pattern NAAN_FORMAT = Pattern.compile("\\d+");
    
    static final long serialVersionUID = 1L;
    
    private String arkName;
    private String naan;
    private String qualifier;
    
    /** Constructor. Takes required fields as separate parameters.
     * 
     * @param naan Name Assigning Authority Number.
     * @param arkName Unique name component of the ARK.
     * @param qualifier An optional qualifier used for hierarchy and variations.
     *            Can generally be left empty.
     * @throws URIException
     * @throws NullPointerException */
    private Hark (String naan, String arkName, String qualifier) throws URIException {
        super(qualifier.isEmpty() ?
                SCHEME + "/" + naan + "/" + arkName :
                SCHEME + "/" + naan + "/" + arkName + "/" + qualifier, false);
        
        this.naan = naan;
        this.arkName = arkName;
        this.qualifier = qualifier;
    }
    
    /** Constructor. Takes a single URI-formatted string containing all
     * parameters. Helpful for creating a new Hark instance from a string
     * previously returned by a PID factory.
     * 
     * @param uri The full ID URI containing all required fields. 
     * @throws NullPointerException 
     * @throws URIException */
    private Hark(String uri) throws URIException, NullPointerException {
        super(uri, false);
    }

    @Override
    public String getNaan ()
    {
        return this.naan;
    }

    @Override
    public String getArkName ()
    {
        return this.arkName;
    }
    
    @Override
    public String getQualifier ()
    {
        return this.qualifier;
    }
    
    /** {@link PersistentIdFactory} class for {@link Hark}. */
    @Component(Ark.SCHEME)
    @Scope("prototype")
    public static class HarkFactory implements PersistentIdFactory
    {
        @Autowired
        private ArkFactory factory;
        
        /** Private utility class used to mint new PIDs from NOID, used in
         * creating new {@link Hark}s. */
        public static class ArkFactory
        {
            /** Pattern that the template must follow. */
            private static Pattern TEMPLATE_FORMAT = Pattern.compile("\\.[zse][ed]");
            
            @AutowiredLogger
            private Logger log = null;
            /** Noid instance */
            private Noid noid;

            /** Constructor.
             * 
             * @param template String template to output Noid IDs. Correct format is
             *            .(z|s|e)(e|d).
             * @param naan Number assigned to the entity using this factory, as a
             *            String.
             * @param dbhome Path directory where Noid folders will be created or found. */
            public ArkFactory (String template, String naan, Path dbhome)
            {
                // Validate input.
                Assert.notNull(template, "template must not be null.");
                Assert.notNull(naan, "naan must not be null.");
                Assert.notNull(dbhome, "dbhome must not be null.");
                Assert.isTrue(NAAN_FORMAT.matcher(naan).matches(),
                        "naan does not match expected format.");
                Assert.isTrue(TEMPLATE_FORMAT.matcher(template.trim()).matches(),
                        "template does not match expected format." + template);
                
                // Attempt to open NOID database.
                try
                {
                    noid = new Noid(Files.createDirectories(dbhome).toAbsolutePath().toString());
                    if (Files.isExecutable(dbhome) && Files.isReadable(dbhome) && Files.isWritable(dbhome))
                    {
                        noid.dbCreate(template, naan);
                    }
                    else
                    {
                        String msg = "Noid does not have permission to read, write, and execute at "
                                + dbhome.toAbsolutePath();
                        throw new HidaIOException(msg);
                    }
                }
                catch (IOException e)
                {
                    String msg = "An error occurred while creating the directory at " + dbhome;
                    throw new HidaIOException(msg, e);
                }
            }

            private Hark createPersistentId ()
            {
                log.debug("Entering createPersistentId()");
                // Generate the NOID ID
                String id = noid.mint(true);
                if (id == null)
                {
                    String msg = "Noid has reached the limit for generated IDs.";
                    log.error(msg);
                    throw new HidaException(msg);
                }
                try
                {
                    Hark hark = new Hark(Ark.SCHEME + ":/" + id);
                    log.debug("Exiting createPersistentId(): {}", hark);
                    return hark;
                }
                catch (URIException e)
                {
                    String msg = "Exception was thrown while creating the URI for the HARK.";
                    log.error(msg, e);
                    throw new HidaException(msg, e);
                }
                catch (NullPointerException e)
                {
                    String msg = "There was a null pointer exception. id = " + id;
                    log.error(msg, e);
                    throw new HidaException(msg, e);
                }
            }
        }
        
        @Override
        public PersistentId createPersistentId ()
        {
            return this.factory.createPersistentId();
        }

        @Override
        public PersistentId createPersistentId (String existingUri)
        {
            String naan;
            String arkName;
            String qualifier = "";
            
            // Validate input.
            Assert.notNull(existingUri, "Input URI must not be null.)");
            String[] components = existingUri.split("/");
            Assert.isTrue(components.length == 3 || components.length == 4,
                    "existingUri does not contain the expected number of components " +
                    "(expected 3 or 4, found " + components.length + ".");
            Assert.isTrue(components[0].equals(Ark.SCHEME + ":"),
                    "Scheme identifier of input URI does not match expected type.");
            Assert.isTrue(Hark.NAAN_FORMAT.matcher(components[1]).matches(),
                    "NAAN of input does not match expected format.");
            naan = components[1];
            Assert.hasLength(components[2], "ARK name must not be empty.");
            arkName = components[2];
            if (components.length == 4) {
                Assert.hasLength(components[3], "Qualifier must not be empty.");
                qualifier = components[3];
            }
            
            try
            {
                Hark result =  new Hark(existingUri);
                result.naan = naan;
                result.arkName = arkName;
                result.qualifier = qualifier;
                return result;
            }
            catch (URIException e)
            {
                throw new HidaException("Issue encountered creating Hark.", e);
            }
        }
    }
}
