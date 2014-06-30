package gov.hawaii.digitalarchives.hida.persistentid;

import java.util.ArrayList;
import java.util.List;

import org.cdl.noid.Noid;

public class NoidGUIMethods
{
    private Noid noid;
    private String NoidTemplate;
    public void setNewNoid(String directory)
    {
        //log.debug("setNewNoid: Noid directory set to {}", directory);
        noid = new Noid(directory);
    }
    public String getReport(String template, String naan)
    {
        NoidTemplate = template;
        String db = noid.dbCreate(template, naan);
        return db;
    }
    public String mintNewId(boolean pepper)
    {
        return noid.mint(pepper);
    }
    public List<String> mintNewIds(int nIds)
    {
        List<String> nIdList = new ArrayList<String> ();
        for(int i = 0;i < nIds; i++) 
        {
            nIdList.add(noid.mint(true));
        }
        return nIdList;
    }
    public String getTemplate()
    {
        //log.debug("getTemplate: Noid template set to {}", NoidTemplate);
        return NoidTemplate;
    }
    public String getMsgLog()
    {
        String tempLog = noid.getMsg();
        //log.debug("getMsgLog: {}", tempLog);
        return tempLog;
    }
    public String getErc()
    {
        String tempLog = noid.getErc();
        //log.debug("getErc: {}", tempLog);
        return tempLog;
    }
}
