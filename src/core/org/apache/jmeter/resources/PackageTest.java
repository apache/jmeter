package org.apache.jmeter.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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
 * - non-default properties files have same keys as the default.
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
 * @author sebb AT apache DOT org
 * @version $revision$ $date$
 */
public class PackageTest extends TestCase
{

    private static int defaultListSize;

    private static List defaultList = null;
    
    private int countKeys(Enumeration e, List l){
    	int i=0;
    	while (e.hasMoreElements()){
    		i++;
    		l.add(e.nextElement());
    	}
    	return i;
    }
    
    // Read resource into ResourceBundle and store in List
    private int getRAS(String res, List l) throws Exception{
    	InputStream ras = this.getClass().getResourceAsStream(res);
    	PropertyResourceBundle prb = new PropertyResourceBundle(ras);
    	return countKeys(prb.getKeys(),l);
    }

    private int readRF(String res, List l) throws Exception
    {//Read resource file and return # of lines; saving the keys
		int i=0;// no of lines
		InputStream ras = this.getClass().getResourceAsStream(res);
		BufferedReader fileReader =
		new BufferedReader(new InputStreamReader(ras));
       	String s;
        while((s=fileReader.readLine())!=null)
        {
           	if (s.length() > 0)  {
           		l.add(s.substring(0,s.indexOf('=')));
          		i++;
           	}
       	} 
		return i;
    }
    
    // Helper method to construct resource name
    private static String getResName(String lang){
    	if (lang.length()==0){
			return "messages.properties";
    	} else {
			return "messages_"+lang.toLowerCase()+".properties";
    	}
    }
	
	private void check(String resname) throws Exception
	{
		ArrayList alf = new ArrayList(500);// holds keys from file
		ArrayList alr = new ArrayList(500);// holds keys from resource
		String res = getResName(resname);
		readRF(res,alf);
		getRAS(res,alr);
		Collections.sort(alf);
		Collections.sort(alr);
		
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
		
		if (defaultList != null){
			if (defaultListSize != alr.size()){
				subTestFailures++;
				System.out.println("\nKey counts differ: "
				+getResName("")+"="+defaultListSize+" "+res+"="+alr.size());
			}
			if (!defaultList.equals(alr)){
				subTestFailures++;
				System.out.println("\nKeys in "
				+res
				+" do not match keys in "
				+getResName("")
				);
				for (int i=0;i<alr.size();i++){
					String d=(String) defaultList.get(i);
					String a=(String) alr.get(i);
					if (!d.equals(a)){
						System.out.println("First difference: "+ a + "!=" + d);
						break;
					}
				}
			}
		} else { // must be the default file
			defaultList = alr;
			defaultListSize = alr.size();
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
// Disabled test altogether while we think what to do about it
// TODO: reinstate
/*		ts.addTest(new PackageTest("atestDefault"));
		ts.addTest(new PackageTest("atestDE"));
		ts.addTest(new PackageTest("atestNO"));
		ts.addTest(new PackageTest("atestJA"));*/
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
