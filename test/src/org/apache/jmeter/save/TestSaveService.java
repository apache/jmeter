/*
 * Created on Jun 19, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jmeter.save;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.collections.HashTree;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestSaveService extends JMeterTestCase
{
       private static final String[] FILES =
           new String[] {
               "AssertionTestPlan.jmx",
               "AuthManagerTestPlan.jmx",
               "HeaderManagerTestPlan.jmx",
               "InterleaveTestPlan2.jmx",
               "InterleaveTestPlan.jmx",
               "LoopTestPlan.jmx",
               "Modification Manager.jmx",
               "OnceOnlyTestPlan.jmx",
               "proxy.jmx",
               "ProxyServerTestPlan.jmx",
               "SimpleTestPlan.jmx",
               "GuiTest.jmx",
               };

       private static boolean saveOut = 
    	   System.getProperty("testsaveservice.saveout","false").equalsIgnoreCase("true");
       
       public TestSaveService(String name)
       {
           super(name);
       }

       public void setUp()
       {}

       public void testLoadAndSave() throws Exception
       {
           byte[] original = new byte[1000000];

           boolean failed = false; // Did a test fail?
           
            for (int i = 0; i < FILES.length; i++)
              {
                  InputStream in =
                      new FileInputStream(new File("testfiles/" + FILES[i]));
                  int len = in.read(original);

                  in.close();

                  in = new ByteArrayInputStream(original, 0, len);
                  HashTree tree = SaveService.loadTree(in);

                  in.close();

                  ByteArrayOutputStream out = new ByteArrayOutputStream(1000000);

                  SaveService.saveTree(tree, new OutputStreamWriter(out));
                  out.close();

                  // We only check the length of the result. Comparing the
                  // actual result (out.toByteArray==original) will usually
                  // fail, because the order of the properties within each
                  // test element may change. Comparing the lengths should be
                  // enough to detect most problem cases...
                  if (len != out.size())
                  {
                  	failed=true;
                  	System.out.println();
                      System.out.println(
                          "Loading file testfiles/"
                              + FILES[i]
                              + " and "
                              + "saving it back changes its size from "+len+" to "+out.size()+".");
                      if (saveOut) {
                    	  String outfile="testfiles/"+FILES[i]+".out";
                    	  System.out.println("Write "+outfile);
                    	  FileOutputStream outf = new FileOutputStream(
                    			  new File(outfile));
                    	  outf.write(out.toByteArray());
                    	  outf.close();
                    	  System.out.println("Wrote "+outfile);
                      }
                  }

                  // Note this test will fail if a property is added or
                  // removed to any of the components used in the test
                  // files. The way to solve this is to appropriately change
                  // the test file.
              }
           if (failed) //TODO make these separate tests?
           {
           	fail("One or more failures detected");
           }
       }
}
