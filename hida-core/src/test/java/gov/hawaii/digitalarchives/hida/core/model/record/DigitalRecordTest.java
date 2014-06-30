package gov.hawaii.digitalarchives.hida.core.model.record;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests for {@link DefaultDigitalRecord}.
 *
 * @author Dongie Agnir
 */
@ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
public class DigitalRecordTest extends AbstractTestNGSpringContextTests {

    private final Logger log = LoggerFactory.getLogger(DigitalRecordTest.class);

    private String id;
    private Agent creator;
    private Agent preserver;
    private ProducerInfo producerInfo;
    private String title;
    private String agencyRecordId;
    private RetentionInfo retentionInfo;
    private Date createdDate;
    private RecordLanguage language = new RecordLanguage();
    private MetadataEntry[] metadataEntries;

    /**
     * Sets up our test Record.
     */
    @BeforeTest
    public void setUp() {

    }
//=================================================================================================
// Tests for methods that return "modifiable" classes.  Mostly collections.
//=================================================================================================

    /**
     * Test the copy returned by
     * {@link DefaultDigitalRecord#getMetadataEntries()} method.
     *
     * @throws URIException
     */
    @Test
    public void testMetadaEntriesCopy() throws URIException {
        DigitalRecord record = makeRecord();

        List<MetadataEntry> entries = record.getMetadataEntries();

        MetadataEntry newEntry = mock(MetadataEntry.class);
        when(newEntry.getName()).thenReturn("this_does_not_exist_in_record");

        entries.add(newEntry);

        for (MetadataEntry e : record.getMetadataEntries()) {
            if (newEntry.getName().equals(e.getName())) {
                fail("Our metadata entry made its way into record!");
            }
        }
    }
    /**
     * Test the copy returned by
     * {@link DefaultDigitalRecord#getLanguages()} method.
     *
     * @throws URIException
     */
    @Test
    public void testLanguagesCopy() throws URIException {
        DigitalRecord record = makeRecord();

        RecordLanguage newLanguage = new RecordLanguage("Japanese", record);
        List<RecordLanguage> recLanguages = record.getLanguages();

        recLanguages.add(newLanguage);

        for (RecordLanguage lang : record.getLanguages()) {
            if (lang.getLanguage().equalsIgnoreCase(newLanguage.getLanguage())) {
                fail("Language made it into record through the returned list!");
            }
        }
    }

    /**
     * Simple test to make sure all the properties we set are getting
     * returned.
     *
     * @throws URIException
     */
    @Test
    public void testPropertiesMatch() throws URIException {
        DigitalRecord record = makeRecord();

        assertEquals(record.getPrimaryId(), id);
        assertEquals(record.getCreator(), creator);
        assertEquals(record.getPreserver(), preserver);
        assertEquals(record.getProducerInfo(), producerInfo);
        assertEquals(record.getTitle(), title);
        assertEquals(record.getAgencyRecordId(), agencyRecordId);
        assertEquals(record.getRetentionInfo(), retentionInfo);
        assertEquals(record.getCreatedDate(), createdDate);
        assertEquals(record.getLanguages(), Arrays.asList(language));
    }

    @Test
    public void testAddMDEntriesSameKeyVal() throws URIException {
        DigitalRecord record = makeRecord();

        MetadataEntry newEntry1 = new MetadataEntry("new_entry_1", "aaaa", "ark://obj1");
        MetadataEntry newEntry2 = new MetadataEntry("new_entry_1", "aabb", "ark://obj1");
        //Note the order.  newEntry2 was added last, so it should overwrite
        //newEntry1's value, which shares the same key.
        record.addMetadataEntry(newEntry1);
        record.addMetadataEntry(newEntry2);

        for (MetadataEntry mdEntry : record.getMetadataEntries()) {
            if (mdEntry.getName().equals("new_entry_1")) {
                assertEquals(mdEntry.getValue(), "aabb");
            }
        }
    }

    /**
     * Helper method to create a test record.
     *
     * @return The test {@code DigitalRecord}.
     *
     * @throws URIException
     */
    private DigitalRecord makeRecord() throws URIException {
        DigitalRecord record = new DigitalRecord();

        id = "ark://record1";
        creator = new Organization("HiDA", "HiDANote");
        preserver = new Organization("HiDA", "HiDANote");
        producerInfo = new ProducerInfo("DAGS", "Archives", "Historical Records");
        title = "My Record";
        agencyRecordId = "my-record-1";
        retentionInfo = new RetentionInfo("schedule-1", "s1", "series-1");
        createdDate = new Date();
        language = new RecordLanguage("english", record);
        metadataEntries = new MetadataEntry[] {
                new MetadataEntry("entry_name_1", "value_1", "ark://obj1"),
                new MetadataEntry("entry_name_2", "value_2", "ark://obj1")
        };

        record.setPrimaryId(id);
        record.setCreator(creator);
        record.setPreserver(preserver);
        record.setProducerInfo(producerInfo);
        record.setTitle(title);
        record.setAgencyRecordId(agencyRecordId);
        record.setRetentionInfo(retentionInfo);
        record.setCreatedDate(createdDate);
        record.addLanguage(language);
        record.addMetadataEntry(metadataEntries[0]); record.addMetadataEntry(metadataEntries[1]);

        return record;
    }

    /**
     * DigitalRecords attempt to make defensive copies of Dates given to it.
     * Make sure that if we give it a null Date that it doesn't try deref it.
     */
    @Test
    public void testSetNullCreateDate() {
        DigitalRecord record = new DigitalRecord();

        try {
            record.setCreatedDate(null);
        } catch (NullPointerException npe) {
            fail("setCreatedDate(null) unexpectedly threw NullPointerException!");
        }
    }

    /**
     * DigitalRecords attempt to return defensive copies of its dates.  Make
     * sure that it doesn't accidentally try to deref the date and copy it if
     * it's null.
     */
    @Test
    public void testGetNullCreatedDate() {
        DigitalRecord record = new DigitalRecord();

        try {
            assertNull(record.getCreatedDate());
        } catch (NullPointerException npe) {
            fail("getCreatedDate() unexpectedly threw NullPointerException!");
        }
    }
}
