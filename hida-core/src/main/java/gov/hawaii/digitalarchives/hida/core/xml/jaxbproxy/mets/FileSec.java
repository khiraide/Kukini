package gov.hawaii.digitalarchives.hida.core.xml.jaxbproxy.mets;

import gov.hawaii.digitalarchives.hida.core.xml.namespaces.METS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class FileSec {
    @XmlElement(name = "fileGrp", namespace = METS.NS)
    private List<FileGrp> fileGrps = new ArrayList<FileGrp>();

    @XmlElement(name = "ID")
    private String localID;

    public void addGroup(String use, METSFile... files) {
        if (use == null) {
            throw new IllegalArgumentException("\"use\" cannot be null.");
        }
        fileGrps.add(new FileGrp(use, files));
    }

    public void addGroup(String use, METSID ADMID, METSFile... files) {
        throw new UnsupportedOperationException("not yet supported brah");
    }
}
