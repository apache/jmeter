/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.functions;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * File data container for XML files
 * Data is accessible via XPath
 *  
 */
public class XPathFileContainer
{
    
    transient private static Logger log = LoggingManager.getLoggerForClass();
    

    private NodeList nodeList;
    
    private String fileName; // name of the file
    private String xpath;
     
    /** Keeping track of which row is next to be read. */
    private int nextRow;

    private XPathFileContainer()// Not intended to be called directly
    {
    }

	public XPathFileContainer(String file,String xpath)
	throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, TransformerException
	{
		log.debug("XPath("+file+") xpath "+xpath+"");
		fileName = file;
		this.xpath = xpath;
		nextRow = 0;
		load();
	}

	private void load() 
	throws IOException,FileNotFoundException, ParserConfigurationException, SAXException, TransformerException
	{
		InputStream fis=null;
		try
		{
			DocumentBuilder 
					builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			fis = new FileInputStream(fileName);
			nodeList = XPathAPI.selectNodeList(builder.parse(fis), xpath);
			log.debug("found "+nodeList.getLength());
		
		} 
		catch (FileNotFoundException e)
        {
			nodeList = null;
        	log.warn(e.toString());
        	throw e;
        } 
        catch (IOException e)
        {
        	nodeList = null;
			log.warn(e.toString());
            throw e;
        } catch (ParserConfigurationException e) {
        	nodeList = null;
        	log.warn(e.toString());
        	throw e;
		} catch (SAXException e) {
			nodeList = null;
        	log.warn(e.toString());
        	throw e;
		} catch (TransformerException e) {
			nodeList = null;
        	log.warn(e.toString());
        	throw e;
		}
        finally
        {
			if (fis != null) fis.close();
        }
	}

	public String getXPathString(int num) {
		return nodeList.item(num).getNodeValue();
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
        if (nextRow >= size())// 0-based
        {
            nextRow = 0;
        }
		log.debug (new StringBuffer("Row: ").append(row).toString());
		return row;
    }
    
    public int size() {
    	return (nodeList == null ) ? -1 : nodeList.getLength();
    }

  public static class Test extends JMeterTestCase
    {

		static{
			//LoggingManager.setPriority("DEBUG","jmeter");
			//LoggingManager.setTarget(new java.io.PrintWriter(System.out));
		}


    	public Test(String a)
    	{
    		super(a);
    	}
    	
    	public void testNull() throws Exception
    	{
    		try
    		{
    			new XPathFileContainer("nosuch.xml","/");
    			fail("Should not find the file");
    		}
    		catch (FileNotFoundException e)
    		{
    		}
    	}
    	
		public void testrowNum() throws Exception
		{
			XPathFileContainer f = new XPathFileContainer("../build.xml", "/project/target/@name");
			assertNotNull(f);
		//	assertEquals("Expected 4 lines",4,f.size());

			int myRow=f.nextRow();
			assertEquals(0,myRow);
			assertEquals(1,f.nextRow);

			myRow = f.nextRow();
			assertEquals(1,myRow);
			assertEquals(2,f.nextRow);
			
			myRow = f.nextRow();
			assertEquals(2,myRow);
			assertEquals(3,f.nextRow);
			
			
			//myRow = f.nextRow();
			//assertEquals(3,myRow);
			//assertEquals(0,f.nextRow);
			
			//myRow = f.nextRow();
			//assertEquals(0,myRow);
			//assertEquals(1,f.nextRow);

		}
		
		public void testColumns() throws Exception
		{
			XPathFileContainer f = new XPathFileContainer("../build.xml","/project/target/@name");
			assertNotNull(f);
			assertTrue("Not empty",f.size() > 0);
			int last = 0;
			for (int i=0; i< f.size(); i++) {
				last = f.nextRow();
				log.debug("found ["+i+"]"+f.getXPathString(last));
			}
			assertEquals(last+1, f.size());

		}
		public void testDefault() throws Exception
		{
			XPathFileContainer f = new XPathFileContainer("../build.xml","/project/@default");
			assertNotNull(f);
			assertTrue("Not empty",f.size() > 0);
			assertEquals("all", f.getXPathString(0));

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