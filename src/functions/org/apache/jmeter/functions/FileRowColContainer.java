/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.functions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * File data container for CSV (and similar delimited) files
 * Data is accessible via row and column number
 *  
 * @author sebb AT apache DOT org (multiple file version)
 * @version $Revision$
 */
public class FileRowColContainer
{
    
    transient private static Logger log = LoggingManager.getLoggerForClass();
    

    private ArrayList fileData; // Lines in the file, split into columns

    private String fileName; // name of the file
    
    public static final String DELIMITER = ","; // Default delimiter
    
    /** Keeping track of which row is next to be read. */
    private int nextRow;

    /** Delimiter for this file */
	private String delimiter;

    private FileRowColContainer()// Not intended to be called directly
    {
    }

	public FileRowColContainer(String file,String delim)
	throws IOException,FileNotFoundException
	{
		log.debug("FDC("+file+","+delim+")");
		fileName = file;
		delimiter = delim;
		nextRow = 0;
		load();
	}

	public FileRowColContainer(String file)
	throws IOException,FileNotFoundException
	{
		log.debug("FDC("+file+")");
		fileName = file;
		delimiter = DELIMITER;
		nextRow = 0;
		load();
	}


	private void load() 
	throws IOException,FileNotFoundException
	{
		fileData = new ArrayList();

		BufferedReader myBread=null;
		try
		{
			FileReader fis = new FileReader(fileName);
			myBread = new BufferedReader(fis);
			String line = myBread.readLine();
			while (line != null)
			{
				fileData.add(splitLine(line,delimiter));
				line = myBread.readLine();
			}
		} 
		catch (FileNotFoundException e)
        {
			fileData = null;
        	log.warn(e.toString());
        	throw e;
        } 
        catch (IOException e)
        {
        	fileData = null;
			log.warn(e.toString());
			myBread.close();
            throw e;
        }
	}

    /**
     * Get the string for the column from the current row
     * 
     * @param row row number (from 0)
     * @param col column number (from 0)
     * @return the string (empty if out of bounds)
     * @throws IndexOutOfBoundsException if the column number is out of bounds
     */
    public String getColumn(int row,int col) throws IndexOutOfBoundsException
    {
    	String colData;
		colData = (String) ((ArrayList) fileData.get(row)).get(col);
    	log.debug(fileName+"("+row+","+col+"): "+colData);
    	return colData;
    }
    
    /**
     * Returns the next row to the caller, and updates it,
     * allowing for wrap round
     * 
     * @return the first free (unread) row
     * 
     */
    public int nextRow()
    {
    	int row = nextRow;
        nextRow++;
        if (nextRow >= fileData.size())// 0-based
        {
            nextRow = 0;
        }
		log.debug ("Row: "+ row);
		return row;
    }


    /**
     * Splits the line according to the specified delimiter
     * 
     * @return an ArrayList of Strings containing one element for each
     *          value in the line
     */
    private static ArrayList splitLine(String theLine,String delim)
    {
        ArrayList result = new ArrayList();
        StringTokenizer tokener = new StringTokenizer(theLine,delim);
        while(tokener.hasMoreTokens())
        {
            String token = tokener.nextToken();
            result.add(token);
        }
        return result;
    }
    public static class Test extends JMeterTestCase
    {

		static{
//			LoggingManager.setPriority("DEBUG","jmeter");
//			LoggingManager.setTarget(new PrintWriter(System.out));
		}


    	public Test(String a)
    	{
    		super(a);
    	}
    	
    	public void testNull() throws Exception
    	{
    		try
    		{
    			FileRowColContainer f = new FileRowColContainer("testfiles/xyzxyz");
    			fail("Should not find the file");
    		}
    		catch (FileNotFoundException e)
    		{
    		}
    	}
    	
		public void testrowNum() throws Exception
		{
			FileRowColContainer f = new FileRowColContainer("testfiles/test.csv");
			assertNotNull(f);
			assertEquals("Expected 4 lines",4,f.fileData.size());

			int myRow=f.nextRow();
			assertEquals(0,myRow);
			assertEquals(1,f.nextRow);

			myRow = f.nextRow();
			assertEquals(1,myRow);
			assertEquals(2,f.nextRow);

			myRow = f.nextRow();
			assertEquals(2,myRow);
			assertEquals(3,f.nextRow);

			myRow = f.nextRow();
			assertEquals(3,myRow);
			assertEquals(0,f.nextRow);
			
			myRow = f.nextRow();
			assertEquals(0,myRow);
			assertEquals(1,f.nextRow);

		}
		
		public void testColumns() throws Exception
		{
			FileRowColContainer f = new FileRowColContainer("testfiles/test.csv");
			assertNotNull(f);
			assertTrue("Not empty",f.fileData.size() > 0);

			int myRow=f.nextRow();
			assertEquals(0,myRow);
			assertEquals("a1",f.getColumn(myRow,0));
			assertEquals("d1",f.getColumn(myRow,3));

			try {
				f.getColumn(myRow,4);
				fail("Expected out of bounds");
			}
			catch (IndexOutOfBoundsException e)
			{
			}
			myRow=f.nextRow();
			assertEquals(1,myRow);
			assertEquals("b2",f.getColumn(myRow,1));
			assertEquals("c2",f.getColumn(myRow,2));
		}
    }
    /**
     * @return the file name for this class
     */
    public String getFileName()
    {
        return fileName;
    }

}