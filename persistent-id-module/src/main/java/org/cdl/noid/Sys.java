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

/**
 * Sys is an abstract class representing file system operations like chmod and
 * getuid. It has static factory methods that make concrete Sys instances of a
 * particular operating system specific implementation.
 *
 * @author Rasan Rasch (rasan@nyu.edu)
 * @version $Revision$
 */
abstract public class Sys 
{
    private static Class factoryClass = null;
    private static Sys theSys = null;
    private static String[] args = null;

    /**
     * Returns a concrete Sys implementation. If there was a prior call to this
     * method, it will return the same instance that was returned from that
     * call. Otherwise, it will return a new instance. A new instance is
     * necessary if this method was never called before, or after a call to
     * SetFactory().
     *
     * @see #SetFactory
     * @see #GetFactory
     * @throws IllegalArgumentException
     */
    public static synchronized Sys GetSys() throws IllegalArgumentException 
    {
        if (theSys != null) 
        {
            return (theSys);
        }

        String reason = "Unknown error";

        try 
        {
            return (theSys = (Sys) factoryClass.newInstance());
        }catch (NullPointerException why) 
        {
            reason = "Null factory: ";
        }catch (ClassCastException why)
        {
            reason = "Wrong class type: ";
        }catch (InstantiationException why) 
        {
            reason = "Instantiation exception: ";
        }catch (IllegalAccessException why) 
        {
            reason = "Illegal access: ";
        }

        theSys = null;

        throw new IllegalArgumentException(reason
                + String.valueOf(factoryClass));
    }

    /**
     * Set the factory to be the given fully-qualified class name, or null.
     *
     * @param className
     */
    public static synchronized void SetFactory(String className)
            throws IllegalArgumentException 
    {
        theSys = null;

        if (className == null) 
        {
            factoryClass = null;
            return;
        }

        String reason = "Unknown error";
        try 
        {
            factoryClass = Class.forName(className);
            GetSys();
            return;
        }catch (ClassNotFoundException why) 
        {
            reason = "Class not found: ";
        }catch (Throwable why) 
        {
            reason = why.getMessage();
        }

        factoryClass = null;
        throw new IllegalArgumentException(reason + String.valueOf(className));
    }

    /**
     * Return the current factory Class, or null. This will be the class of
     * instances returned by GetPosix().
     */
    public static synchronized Class GetFactory() 
    {
        return (factoryClass);
    }

    protected Sys() 
    {
        super();
    }

    /**
     * Return
     * <code>Options</code> object representing the parsed command line options
     * for
     * <code>NoidTest<code>.
     *
     * @param  args command line arguments
     * @return Options
     * @see Options
     */
    //abstract public Options getOptions(String[] args);
    /**
     * Return string representing user for current process.
     *
     * @param isWeb is current process in web context
     * @return user string
     */
    abstract public String getUser(boolean isWeb);

    /**
     * Set permissions for a file.
     *
     * @param fileName input file
     * @return          <code>true</code> if operation was successful; <code>false</code>
     * otherwise
     */
    abstract public boolean chmod(String fileName);

    /**
     * Print some system information.
     */
    public void printSysinfo() 
    {
        System.err.println("OS Name: " + System.getProperty("os.name"));
        System.err.println("OS Architecture: " + System.getProperty("os.arch"));
        System.err.println("OS Version: " + System.getProperty("os.version"));
    }
}
