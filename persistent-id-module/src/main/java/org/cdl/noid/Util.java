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

import gov.hawaii.digitalarchives.hida.core.exception.HidaException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Util class contains miscellaneous utility functions for manipulating text
 * and file IO.
 *
 * @author Rasan Rasch
 * @
 * @version $Revision$
 */
public class Util 
{
    public static String unNullify(String s) 
    {
        return Util.isEmpty(s) ? "" : s;
    }

    /**
     * Tests whether a string is empty or null.
     *
     * @param s string to be tested
     * @return  <code>true</code> if string is empty or null; <code>false</code>
     * otherwise
     */
    public static boolean isEmpty(String s) 
    {
        return s == null || s.length() == 0;
    }

    /**
     * Tests whether an array is empty or null.
     *
     * @param array array to be tested
     * @return      <code>true</code> if array is empty or null; <code>false</code>
     * otherwise
     */
    public static boolean isEmpty(Object[] array) 
    {
        return array == null || array.length == 0;
    }
    
    public static String getHostName() 
    {
        String hostName = null;
        try 
        {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostName = localMachine.getHostName();
        }catch (java.net.UnknownHostException uhe) 
        {
            // handle exception
            uhe.printStackTrace();
        }
        return hostName;
    }

    /**
     * Returns current working directory
     *
     * @return path of current working directory; if there was an IO error, it
     * will return <code>null</code>
     */
    public static String getCwd() 
    {
        String cwd = null;
        try 
        {
            cwd = new File(".").getCanonicalPath();
        }catch (IOException e) 
        {
            // handle exception
            e.printStackTrace();
        }
        return cwd;
    }
    
    /** 
      * Returns elements of an array matching specfied regular expression.
      *
      * @param regexp pattern to test each array member
      * @param args   array of strings to evaluate the regexp
      * @return       array of elements that matched <code>regexp</code>
      */
    public static String[] grep(String regexp, String[] args) 
    {
        ArrayList<String> list = new ArrayList<String>();
        Pattern patt = Pattern.compile(regexp);
        Matcher matcher = patt.matcher("");
        for (int i = 0; i < args.length; i++) 
        {
            matcher.reset(args[i]);
            if (matcher.find()) 
            {
                list.add(args[i]);
            }
        }
        String[] matches = (String[])list.toArray(new String[0]);
        return matches;
    }

    /**
     * Returns the contents of a file as string.
     *
     * @param fileName file to be read
     * @return string containing contents of file
     */
    public static String getFile(String fileName) 
    {
        StringBuffer sb = new StringBuffer();
        try 
        {
            BufferedReader in =
                    new BufferedReader(new FileReader(fileName));
            String str;
            while ((str = in.readLine()) != null) 
            {
                sb.append(str);
            }
            in.close();
        }catch (IOException e) 
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Tests whether an input string matches a pattern.
     *
     * @param pattern regular expression used to test string input
     * @param input input string
     * @return        <code>true</code> if pattern matches; <code>false</code>
     * otherwise
     */
    public static boolean matches(String pattern, String input) 
    {
        return Pattern.compile(pattern).matcher(input).find();
    }

    /**
     * Pad integer with leading zeros.
     *
     * @param value integer value to be padded
     * @param length minimum length of padded string
     * @return zero-padded string
     */
    public static String zeroPad(int value, int length) 
    {
        // DecimalFormat df = new DecimalFormat("00");
        DecimalFormat df = new DecimalFormat();
        df.setMinimumIntegerDigits(length);
        // df.setGroupingUsed(false);
        return df.format(value);
    }

    /**
     * Sets string buffer to
     * <code>newValue</code> string.
     *
     * @param sb string buffer to be set
     * @param newValue new value of string buffer
     */
    public static void setBuffer(StringBuffer sb, String newValue) 
    {
        sb.setLength(0);
        sb.append(newValue);
    }
    /** 
     * Removes first element of an <code>ArrayList</code> and returns
     * that element.
     * 
     * @param list array list to be shifted
     * @return     first element of array list if it's size is at
     *             least 1; <code>null</code> otherwise
     */
    public static Object shift(ArrayList list) 
    {
        return list.size() > 0 ? list.remove(0) : null;
    }

    /**
     * Store the contents of
     * <code>buf</code> in a new file.
     *
     * @param fileName path of file to create
     * @param buf contents of file
     * @return <code>true</code> if file was successfully create;
     * <code>false</code> otherwise
     */
    public static boolean storeFile(String fileName, String buf) 
    {
        //First open the file you want to write into
        try 
        {
            FileOutputStream fout = new FileOutputStream(fileName);

            // now convert the FileOutputStream into a PrintStream
            PrintStream myOutput = new PrintStream(fout);

            // Now you're able to use println statements just 
            // as if you were using System.out.println
            // to write to the terminal
            myOutput.println(buf);
            fout.close();
            myOutput.close();
        }catch (IOException e) 
        {
            String msg = "Error opening file: " + e.getMessage();
            System.out.println(msg);
            throw new HidaException(msg,e);
        }
        //sys.chmod(fileName);
        return true;
    }
}
