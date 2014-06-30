/*
 * $Id$
 *
 * Copyright (c) 2002-2006 UC Regents
 * 
 * Permission to use, copy, modify, distribute, and sell this software and
 * its documentation for any purpose is hereby granted without fee, provided
 * that (i) the above copyright notices and this permission notice appear in
 * all copies of the software and related documentation, and (ii) the names
 * of the UC Regents and the University of California are not used in any
 * advertising or publicity relating to the software without the specific,
 * prior written permission of the University of California.
 * 
 * THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY 
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE FOR ANY
 * SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY
 * THEORY OF LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE
 * OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.cdl.noid;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springbyexample.util.log.AutowiredLogger;

/** 
 * 
 * 
 * @author Rasan Rasch (rasan@nyu.edu)
 * @version $Revision$
 */

public class NoidDB 
{
    private String id = "";
    private String msg = "";
    private String envHome = null;
    private Database db = null;
    private DatabaseConfig dbConfig = null;
    private boolean alreadyLocked;
    Timer timer = null;
    private PrintStream log = null;
    private static String R = ":";

    @AutowiredLogger
    Logger logger = LoggerFactory.getLogger(Noid.class);

    
    public NoidDB(String envHome) 
    {
        this.envHome = envHome;
    }
    
    public Database getDB() 
    {
        return db;
    }

    public void setDB(Database database) 
    {
        db = database;
    }

    public String getMsg() 
    {
        return msg;
    }

    public String getMsg(boolean reset)
    {
        String buf = msg;
        if (reset) 
        {
            msg = "";
        }
        return buf;
    }
    
    public void setMsg(String buf) 
    {
        msg = buf;
    }

    
    /**
     * Add message to error message buffer.
     * 
     * @param buf  Error message.
     */
    public void addMsg(String buf) 
    {
    	this.logger.error(buf);
        msg += buf + "\n";
    }

    public void setLog(PrintStream out) 
    {
        log = out;
    }
    
    public void writeLog(String buf) 
    {
        //log.println(buf);
    }

    private void openLog() 
    {
        String logFile = envHome + ".log";
        FileOutputStream fout;
        PrintStream log;
        try 
        {
            fout = new FileOutputStream(logFile, true);
            log = new PrintStream(fout);
        }catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public void setReadOnly() 
    {
        dbConfig.setReadOnly(true);
    }

    /**
     * Report key/values in database according to level.
     *
     * @param level output level - <code>brief</code> for user vals and
     * interesting admin vals, <code>full</code> for user vals and all admin
     * vals, and <code>dump</code> for all vals, including all identifier
     * bindings
     */
    public int dbInfo(String level) 
    {
        Cursor cursor = null;

        try 
        {
            cursor = db.openCursor(null, null);

            String searchKey = R + "/";
            String searchData = "0";

            DatabaseEntry theKey =
                    new DatabaseEntry(searchKey.getBytes("UTF-8"));
            DatabaseEntry theData =
                    new DatabaseEntry(searchData.getBytes("UTF-8"));

            if (level.equals("dump")) 
            {
                while (cursor.getNext(theKey, theData, LockMode.DEFAULT)
                        == OperationStatus.SUCCESS) 
                {
                	this.logger.debug("{}:{}", theKey.getData(), theData.getData());
                }
                return 1;
            }

            // Perform the search
            OperationStatus retVal =
                    cursor.getSearchKeyRange(theKey, theData, LockMode.DEFAULT);

            // NOTFOUND is returned if a record cannot be found
            // whose key matches the search key AND whose data
            // begins with the search data.
            if (retVal == OperationStatus.NOTFOUND) 
            {
            	this.logger.debug("{}/{} not matched in database {}", 
            			searchKey, searchData, db.getDatabaseName());
                return 0;
            }

            // Upon completing a search, the key and data DatabaseEntry
            // parameters for getSearchKeyRange() are populated with the
            // key/data values of the found record.
            String foundKey = new String(theKey.getData());
            String foundData = new String(theData.getData());
            this.logger.debug("Found record {}/{} for search key/data: {}/{}", 
            		foundKey, foundData, searchKey, searchData);

            String patt = "^" + R + "/" + R + "/";
            Pattern p = Pattern.compile(patt);
            if (p.matcher(foundKey).find()) 
            {
            	this.logger.debug("User Assigned Values");
            	this.logger.debug("  {}:{}", foundKey, foundData);
                while (cursor.getNext(theKey, theData, LockMode.DEFAULT)
                        == OperationStatus.SUCCESS) 
                {
                    foundKey = new String(theKey.getData());
                    foundData = new String(theData.getData());
                    if (!p.matcher(foundKey).find()) 
                    {
                        break;
                    }
                    this.logger.debug("  {}:{}", foundKey, foundData);
                }
                this.logger.debug("");
            }

            this.logger.debug("Admin Values");
            this.logger.debug("  {}:{}", foundKey, foundData);
            while (cursor.getNext(theKey, theData, LockMode.DEFAULT)
                    == OperationStatus.SUCCESS) 
            {
                foundKey = new String(theKey.getData());
                foundData = new String(theData.getData());

                if (!Util.matches("^" + R + "/", foundKey)) 
                {
                    break;
                }
                if (level.equals("full")
                        || !Util.matches("^" + R + "/c\\d", foundKey)
                        && !Util.matches("^" + R + "/saclist", foundKey)
                        && !Util.matches("^" + R + "/recycle", foundKey)) 
                {
                	this.logger.debug("  {}:{}", foundKey, foundData);
                }
            }
            this.logger.debug("");
        }catch (Exception e) 
        {
            e.printStackTrace();
        }finally 
        {
            closeCursor(cursor);
        }
        return 1;
    }

    public boolean open() 
    {
        return open(false);
    }

    public boolean open(boolean createNew) 
    {
        File dir = new File(envHome);
        //System.out.println(dir);
        if (!dir.isDirectory()) 
        {
            addMsg(envHome + " not a directory");
        }

        Environment env = null;

        try 
        {
            if (dbConfig == null) 
            {
                dbConfig = new DatabaseConfig();
            }

            if (createNew) 
            {
                dbConfig.setAllowCreate(true);
            }

            //dbConfig.setAllowCreate(true);
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            env = new Environment(new File(envHome), envConfig);

            if (dbConfig.getAllowCreate()) 
            {
                envConfig.setAllowCreate(true);
            }

            String lockFile = envHome + "lock";
            //System.out.println("Lockfile: " + lockFile);
            int timeout = 5;
            RandomAccessFile lock = new RandomAccessFile(lockFile, "rw");
            FileChannel channel = lock.getChannel();
            boolean isSharedLock = dbConfig.getReadOnly();
            //System.out.println(isSharedLock);

            timer = new Timer();
            timer.schedule(new RemindTask(), timeout * 1000);
            //channel.lock(0, Long.MAX_VALUE, isSharedLock);

            timer.cancel();

            db = env.openDatabase(null, "noid", dbConfig);
            if (db == null) 
            {
            	this.logger.error("Can't open the database");
                throw new HidaException("Can't open the database");
            }

//          if (lockTest != 0) 
//          {
// 		System.out.println("locktest: holding lock for " +
//                                 lockTest + " seconds");
//              Thread.sleep(lockTest * 1000);
//          }

        }catch (Exception e) {}
        //System.out.println("Database created.");
        return true;
    }

    public void close() 
    {
        try 
        {
            if (db != null) 
            {
                db.close();
            }
        }catch (DatabaseException dbe) 
        {
            this.logger.error("Error closing db: {}", dbe.toString());
            throw new HidaException("Error closing db", dbe);
        }
    }

    public String get(String key) 
    {
        return searchKey(key).getData();
    }

    public SearchResult searchKey(String key) 
    {
        //System.err.println("entering searchKey(" + key + ")");
        OperationStatus status = null;
        String foundData = null;
        try 
        {
            // Create a pair of DatabaseEntry objects. theKey
            // is used to perform the search. theData is used
            // to store the data returned by the get() operation.
            DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            DatabaseEntry theData = new DatabaseEntry();

            // Perform the get.
            status = db.get(null, theKey, theData, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) 
            {
                // Recreate the data String.
                byte[] retData = theData.getData();
                foundData = new String(retData);
                //System.err.println("For key: '" + key + "' found data: '" + 
                //					foundData + "'.");
            } 
            else 
            {
                //System.err.println("No record found for key '" + key + "'.");
            }
        }catch (Exception e) 
        {
        	this.logger.error("Error: {}", e);
        }
        return new SearchResult(status, foundData);
    }

    public String rGet(String key) 
    {
        return get(R + "/" + key);
    }
    
    public int rGetInt(String key) 
    {
        return Integer.parseInt(rGet(key));
    }

    public boolean rGetBool(String key) 
    {
        //System.err.println("entering rGetBool()");
        String str = rGet(key);
        if (str == null) 
        {
            return false;
        }
        if (str.equals("0")) 
        {
            return false;
        }
        str = str.toLowerCase();
        if (str.equals("false")) 
        {
            //System.err.println("Return false for " + key);
            return false;
        }
        if (str.equals("no")) 
        {
            return false;
        }
        // if it's non false, it's true by definition
        //System.err.println("Return true for " + key);
        return true;
    }

    public boolean set(String key, String value) 
    {
        DatabaseEntry theKey, theValue;
        OperationStatus status = null;
        try 
        {
            theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            theValue = new DatabaseEntry(value.getBytes("UTF-8"));
            status = db.put(null, theKey, theValue);
        } catch (Exception e) {}
        
        if (status == OperationStatus.SUCCESS) 
        {
            //System.err.println("Successfully wrote " + key + " = " + value);
            return true;
        } else 
        {
        	this.logger.error("Failed to write {}", key);
            return false;
        }
    }

    public boolean set(String key, boolean value) 
    {
        String s = value ? "true" : "false";
        return set(key, s);
    }

    public boolean set(String key, int value) 
    {
        return set(key, Integer.toString(value));
    }

    public boolean rSet(String key, String value) 
    {
        return set(R + "/" + key, value);
    }

    public boolean rSet(String key, int value) 
    {
        return set(R + "/" + key, value);
    }

    public boolean rSet(String key, boolean value) 
    {
        return set(R + "/" + key, value);
    }

    public boolean append(String key, String buf) 
    {
        String value = get(key);
        if (value == null) 
        {        
            value = "";
        }
        value += buf;
        return set(key, value);
    }
    
    public boolean prepend(String key, String buf) 
    {
        String value = get(key);
        if (value == null) 
        {
            value = "";
        }
        value = buf + value;
        return set(key, value);
    }
    
    public boolean rInc(String key) 
    {
        return rSet(key, Integer.toString(rGetInt(key) + 1));
    }

    public boolean rDec(String key) 
    {
        return rSet(key, Integer.toString(rGetInt(key) - 1));
    }

    public boolean defined(String key) 
    {
        return get(key) != null;
    }

    public boolean exists(String key) 
    {
        return searchKey(key).foundKey();
    }

    public boolean isEmpty(String key) 
    {
        String value;
        if (!exists(key)) 
        {
            return true;
        }
        value = get(key);
        return value == null || value.length() == 0;
    }

    public boolean rIsEmpty(String key) 
    {
        return isEmpty(R + "/" + key);
    }

    /**
    public boolean rDefined(String key) {
        return defined(R + "/" + key);
    }*/

    public void remove(String key) 
    {
        try 
        {
            DatabaseEntry theKey = new DatabaseEntry(key.getBytes("UTF-8"));
            // Perform the deletion. All records that use this key are
            // deleted.
            db.delete(null, theKey);
        } catch (Exception e) {}
    }

    public String toString() 
    {
        Cursor cursor = null;
        StringBuffer sb = new StringBuffer();
        try 
        {
            // Open the cursor.
            cursor = db.openCursor(null, null);

            // Cursors need a pair of DatabaseEntry objects to
            // operate. These hold the key and data found at any
            // given position in the database.
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            // To iterate, just call getNext() until the last
            // database record has been read. All cursor operations
            // return an OperationStatus, so just read until we no
            // longer see OperationStatus.SUCCESS
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT)
                    == OperationStatus.SUCCESS) 
            {
                // getData() on the DatabaseEntry objects returns
                // the byte array held by that object. We use this
                // to get a String value. If the DatabaseEntry held
                // a byte array representation of some other data
                // type (such as a complex object) then this
                // operation would look considerably different.
                String keyString = new String(foundKey.getData(), "UTF-8");
                String dataString = new String(foundData.getData(), "UTF-8");
                sb.append("Key | Data : " + keyString + " | "
                        + dataString + "");
            }
        }catch (DatabaseException de) 
        {
        	this.logger.error("Error accessing database. {}", de);
        }catch (UnsupportedEncodingException uee) 
        {
        	this.logger.error("UTF-8 charset not supported. {}", uee);
        }
        finally 
        {
            // Cursors must be closed.
            closeCursor(cursor);
        }
        return sb.toString();
    }

    private void closeCursor(Cursor cursor) 
    {
        try 
        {
            if (cursor != null) 
            {
                cursor.close();
            }
        }catch (Exception e) {}
    }

    class RemindTask extends TimerTask 
    {
        public void run() 
        {
            System.err.format("Time's up!%n");
            timer.cancel(); //Terminate the timer thread
            throw new HidaException("Timed out"); 
        }
    }
}

class SearchResult 
{
    OperationStatus status;
    String data;

    SearchResult(OperationStatus status, String data)
    {
        this.status = status;
        this.data = data;
    }

    public boolean foundKey() {
        return status == OperationStatus.SUCCESS;
    }

    public String getData() {
        return data;
    }
}

/* vim: set ts=4 ai nowrap: */
