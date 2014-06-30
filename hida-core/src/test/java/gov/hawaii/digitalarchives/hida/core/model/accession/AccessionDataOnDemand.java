package gov.hawaii.digitalarchives.hida.core.model.accession;
import gov.hawaii.digitalarchives.hida.core.exception.HidaIdSyntaxException;
import gov.hawaii.digitalarchives.hida.core.model.ModelTestHelper;
import gov.hawaii.digitalarchives.hida.core.model.record.Agent;
import gov.hawaii.digitalarchives.hida.core.model.record.ProducerInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * Helper class for the integration testing of an Accession. 
 * 
 * @author Keone Hiraide
 */
@Component
@Configurable
public class AccessionDataOnDemand {

    /**
     * Used for testing purposes.
     */
    private Random rnd = new SecureRandom();

    /**
     * Collection of Accessions to be used for testing purposes.
     */
    private List<Accession> data;
    
    /**
     * Used when setting a field of {@link Accession}. 
     * whose field is of type {@link java.nio.file.Path}.
     */
    public static final String TEST_RECORD = "/testrecord.txt";

    /** 
     * Builds a transient {@link Accession} to be used for testing.
     * 
     * @return A properly formed {@link Accession} to be used for testing.
     * @param  index Used so that each field set by this {@link Accession}
     *         is unique.
     */
    public Accession getNewTransientAccession(int index) {
        
        Accession accession = new Accession("primaryId_" + index); 
        setAccessionCreationDate(accession);
        setAccessionNumber(accession, index);
        setAccessionPath(accession, index);
        setAccessionReceiveDate(accession);
        setCreator(accession, index);
        setIndexParserId(accession, index);
        setPreserver(accession, index);
        setProducerInfo(accession, index);
        setRtpId(accession, index);
        setTransferMethod(accession, index);
        setTransfererName(accession, index);
        setMachineInfoPair(accession, index);
        return accession;
    }
    
   
    
    /**
     * Sets a creation {@link Date} into this {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     */
    public void setAccessionCreationDate(Accession accession) {
        Date accessionCreationDate = new GregorianCalendar(Calendar.getInstance()
                .get(Calendar.YEAR), Calendar.getInstance()
                .get(Calendar.MONTH), Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH), Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY), Calendar.getInstance()
                .get(Calendar.MINUTE), Calendar.getInstance()
                .get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue())
                .getTime();
        accession.setAccessionCreationDate(accessionCreationDate);
    }

    
    
    /**
     * Sets a unique accession number into this {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setAccessionNumber(Accession accession, int index) {
        URI accessionNumber = null;
        try {
            accessionNumber = new URI("http://www.example.com/" + index);
        } catch (URISyntaxException e) {
            throw new HidaIdSyntaxException("Badly formatted URI " +
                    "when accessionNumber was constructed for the setAccessionNumber() method " +
                    "in the AccessionDataOnDemand class.", e);
        }
        accession.setAccessionNumber(accessionNumber);
    }

    
    
    /**
     * Sets a {@link Path} into this {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setAccessionPath(Accession accession, int index) {
        accession.setAccessionPath(getClass().getResource(TEST_RECORD).getPath());
    }

    
    
    /**
     * Sets a unique receive {@link Date} into this 
     * {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     */
    public void setAccessionReceiveDate(Accession accession) {
        Date accessionReceiveDate = new GregorianCalendar(Calendar.getInstance()
                .get(Calendar.YEAR), Calendar.getInstance()
                .get(Calendar.MONTH), Calendar.getInstance()
                .get(Calendar.DAY_OF_MONTH), Calendar.getInstance()
                .get(Calendar.HOUR_OF_DAY), Calendar.getInstance()
                .get(Calendar.MINUTE), Calendar.getInstance()
                .get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue())
                 .getTime();
        accession.setAccessionReceiveDate(accessionReceiveDate);
    }
    
    /**
     * Sets a unique creator of type {@link Agent} into this {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setCreator(Accession accession, int index) {
        Agent creator = null; // No concrete Agent yet.
        accession.setCreator(creator);
    }
    
    
    
    /**
     * Sets a unique {@link MachineInfoPair} into this {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setMachineInfoPair(Accession accession, int index) {
        Map<String, String> machineInfo = new HashMap<>();
        String indexString = String.valueOf(index);
        machineInfo.put(indexString, indexString);
        accession.setMachineInfo(machineInfo);
    }

    
    
    /**
     * Sets a unique index parser id of type {@link URI} into this 
     * {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setIndexParserId(Accession obj, int index) {
        String indexParserId = "indexParserId_" + index;
        obj.setIndexParserId(indexParserId);
    }

    
    
    /**
     * Sets a unique preserver of type {@link Agent} into this 
     * {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setPreserver(Accession accession, int index) {
        Agent preserver = null; // No concrete Agent yet.
        accession.setPreserver(preserver);
    }

    
    
    /**
     * Sets a unique {@link DefaultProducerInfo} into this {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setProducerInfo(Accession accession, int index) {
        ProducerInfo producerInfo = null;
        accession.setProducerInfo(producerInfo);
    }

    
    
    /**
     * Set a unique rtpId for this {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setRtpId(Accession accession, int index) {
        String rtpId = "http://www.example.com/" + index;
        accession.setRtpId(rtpId);
    }

    
    
    /**
     * Sets a unique transfer method of type {@link String} into this 
     * {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setTransferMethod(Accession accession, int index) {
        String transferMethod = "transferMethod_" + index;
        accession.setTransferMethod(transferMethod);
    }

    
    
    /**
     * Sets a unique transfer name of type {@link String} into this 
     * {@link Accession}.
     * 
     * @param accession    The {@link Accession} instance whose fields we want to 
     *               set for testing purposes.
     * @param index  Used so that each {@link Accession} field
     *               set by this {@link Accession} is unique.
     */
    public void setTransfererName(Accession accession, int index) {
        String transfererName = "transfererName_" + index;
        accession.setTransfererName(transfererName);
    }
    
    
    
    public void setSystemCreator(Accession obj, int index) {
        String systemCreator = "systemCreator_" + index;
        obj.setSystemCreator(systemCreator);
    }


    /**
     * Grab a specific Accession instance from the database 
     * according to its index in the collection.
     * 
     * @param index  Index of the Accession instance that you 
     *                  want to grab from the database.
     * @return       The Accession matching the index.
     */
    public Accession getSpecificAccession(int index) {
        // Ensuring that the index isn't negative or out of bounds.
        index = Math.min(data.size() - 1, Math.max(0, index));
        Accession accession = data.get(index);
        String id = accession.getPrimaryId().toString();
        return Accession.findAccession(id);
    }

    
    
    /**
     * Gets a random Accession from the database.
     * 
     * @return A random Accession from the database.
     */
    public Accession getRandomAccession() {
        Accession accession = data.get(rnd.nextInt(data.size()));
        String id = accession.getPrimaryId().toString();
        return Accession.findAccession(id);
    }
    
    
    /**
     * Used to test merge and flush methods.
     * 
     * @param  accession The accession in which you want check for
     *                   modifications.
     * @return true if the accession passed in has been modified, or false
     *         if it has not been modified.
     */
    //TODO Still need to do this... should be good enough for this story for now though.
    public boolean modifyAccession(Accession accession) {
        return false;
    }

    
    
    /**
     * Persists an initial amount of {@link Accession}s into the 
     * database. This database is then used for testing purposes within the
     * IntegrationTest classes. e.g.  {@link AccessionIntegrationTest}.
     * Note: Meant to be  called once; if  you want to add  additional accessions
     * to the database, use the  Accession classes' persist method.
     * 
     * @param initialDatabaseSize The initial amount of Accessions that
     *                            you want persisted into the database. 
     */
    public void init(int databaseSize) {
        data = Accession.findAccessionEntries(0, databaseSize);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'Accession'" +
                    " illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<Accession>();
        for (int i = 0; i < databaseSize; i++) {
            Accession accession = getNewTransientAccession(i);
            try {
                accession.persist();
            } catch (final ConstraintViolationException e) {
                String msg = ModelTestHelper.getConstraintViolationMessage(e);
                throw new IllegalStateException(msg, e);
            }
            accession.flush();
            data.add(accession);
        }
    }
}
