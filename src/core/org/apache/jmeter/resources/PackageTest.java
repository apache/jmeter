// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Created on Nov 29, 2003
 *
 * Test the composition of the properties files
 * - properties files exist (default, DE, NO, JA)
 * - properties files don't have duplicate keys
 * - non-default properties files don't have any extra keys.
 * 
 * N.B. If there is a default resource, ResourceBundle does not detect missing resources,
 * i.e. the presence of messages.properties means that the ResourceBundle for Locale "XYZ"
 * would still be found, and have the same keys as the default. This makes it not very
 * useful for checking properties files.
 * 
 * This is why the tests use Class.getResourceAsStream() etc
 * 
 * The tests don't quite follow the normal JUnit test strategy of one test
 * per possible failure. This was done in order to make it easier to report
 * exactly why the tests failed.
 */

/**
 * @version $Revision$ $Date$
 */
public class PackageTest extends TestCase
{

    //private static List defaultList = null;
    private static PropertyResourceBundle defaultPRB;
    
    // Read resource into ResourceBundle and store in List
    private PropertyResourceBundle getRAS(String res) throws Exception{
    	InputStream ras = this.getClass().getResourceAsStream(res);
    	return new PropertyResourceBundle(ras);
    }

    //	Read resource file saving the keys
    private void readRF(String res, List l) throws Exception
    {
		InputStream ras = this.getClass().getResourceAsStream(res);
		BufferedReader fileReader =
		new BufferedReader(new InputStreamReader(ras));
       	String s;
        while((s=fileReader.readLine())!=null)
        {
           	if (s.length() > 0 && !s.startsWith("#"))  {
           		l.add(s.substring(0,s.indexOf('=')));
           	}
       	} 
    }
    
    // Helper method to construct resource name
    private static String getResName(String lang){
    	if (lang.length()==0){
			return "messages.properties";
    	} else {
			return "messages_"+lang.toLowerCase()+".properties";
    	}
    }
	
	/*
	 * perform the checks on the resources
	 * 
	 */
	private void check(String resname) throws Exception
	{
		ArrayList alf = new ArrayList(500);// holds keys from file
		String res = getResName(resname);
		readRF(res,alf);
		Collections.sort(alf);
		
		// Look for duplicate keys in the file
		String last="";
		for (int i=0;i<alf.size();i++){
			String curr = (String) alf.get(i);
			if (curr.equals(last)){
				subTestFailures++;
				System.out.println("\nDuplicate key ="+curr+" in "+res);
			}
			last=curr;
		}
		
		if (resname.length()==0) // Must be the default resource file
		{
			defaultPRB = getRAS(res);
		}
		else
		{
			// Check all the keys are in the default props file
			Enumeration enum = getRAS(res).getKeys();
			while(enum.hasMoreElements())
			{
				String key = null;
				try
				{
				    key = (String)enum.nextElement();
			        defaultPRB.getString(key);
				}
				catch (MissingResourceException e)
				{
					subTestFailures++;
					System.out.println("Locale: "+resname+" has unexpected key: "+ key);
				}
			}
		}

		if (subTestFailures > 0) {
			fail("One or more subtests failed");
		}
	}
	
	/*
	 * Use a suite to ensure that the default is done first
	 */
	public static Test suite(){
		TestSuite ts=new TestSuite();
		ts.addTest(new PackageTest("atestDefault"));
		ts.addTest(new PackageTest("atestDE"));
		ts.addTest(new PackageTest("atestNO"));
		ts.addTest(new PackageTest("atestJA"));
		return ts;
	}

    private int subTestFailures;

    public PackageTest(String string)
    {
        super(string);
        subTestFailures=0;
    }

    public void atestDE() throws Exception
	{
		check("DE");
	}

    public void atestJA() throws Exception
	{
		check("JA");
	}
	public void atestNO() throws Exception
	{
		check("NO");
	}
	public void atestDefault() throws Exception
	{
		check("");
	}
}
