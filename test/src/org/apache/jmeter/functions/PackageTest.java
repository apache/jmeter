/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import static org.apache.jmeter.functions.FunctionTestHelper.makeParams;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for Functions
 */
public class PackageTest extends JMeterTestCaseJUnit {

    private static final Logger log = LoggerFactory.getLogger(PackageTest.class);

    public PackageTest(String arg0) {
        super(arg0);
    }

    // Create the CSVRead function and set its parameters.
    private static CSVRead setCSVReadParams(String p1, String p2) throws Exception {
        CSVRead cr = new CSVRead();
        Collection<CompoundVariable> parms = new LinkedList<>();
        if (p1 != null) {
            parms.add(new CompoundVariable(p1));
        }
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        cr.setParameters(parms);
        return cr;
    }


    // Create the BeanShell function and set its parameters.
    private static BeanShell BSHFParams(String p1, String p2, String p3) throws Exception {
        BeanShell bsh = new BeanShell();
        bsh.setParameters(makeParams(p1, p2, p3));
        return bsh;
    }

    public static Test suite() throws Exception {
        TestSuite allsuites = new TestSuite("Function PackageTest");

        if (!BeanShellInterpreter.isInterpreterPresent()) {
            final String msg = "BeanShell jar not present, tests ignored";
            log.warn(msg);
        } else {
            TestSuite bsh = new TestSuite("BeanShell");
            bsh.addTest(new PackageTest("BSH1"));
            allsuites.addTest(bsh);
        }


        // Reset files
        allsuites.addTest(new PackageTest("CSVSetup"));
        TestSuite par = new ActiveTestSuite("Parallel");
        par.addTest(new PackageTest("CSVThread1"));
        par.addTest(new PackageTest("CSVThread2"));
        allsuites.addTest(par);

        TestSuite xpath = new TestSuite("XPath");
        xpath.addTest(new PackageTest("XPathtestColumns"));
        xpath.addTest(new PackageTest("XPathtestDefault"));
        xpath.addTest(new PackageTest("XPathtestNull"));
        xpath.addTest(new PackageTest("XPathtestrowNum"));
        xpath.addTest(new PackageTest("XPathEmpty"));
        xpath.addTest(new PackageTest("XPathFile"));
        xpath.addTest(new PackageTest("XPathFile1"));
        xpath.addTest(new PackageTest("XPathFile2"));
        xpath.addTest(new PackageTest("XPathNoFile"));

        allsuites.addTest(xpath);
        
        allsuites.addTest(new PackageTest("XPathSetup1"));
        TestSuite par2 = new ActiveTestSuite("ParallelXPath1");
        par2.addTest(new PackageTest("XPathThread1"));
        par2.addTest(new PackageTest("XPathThread2"));
        allsuites.addTest(par2);

        allsuites.addTest(new PackageTest("XPathSetup2"));
        TestSuite par3 = new ActiveTestSuite("ParallelXPath2");
        par3.addTest(new PackageTest("XPathThread1"));
        par3.addTest(new PackageTest("XPathThread2"));
        allsuites.addTest(par3);

        return allsuites;
    }

    private JMeterContext jmctx = null;

    private JMeterVariables vars = null;

    @Override
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        jmctx.setVariables(new JMeterVariables());
        vars = jmctx.getVariables();
    }

    public void BSH1() throws Exception {
        String fn = "testfiles/BeanShellTest.bsh";
        try {
            BSHFParams(null, null, null);
            fail("Expected InvalidVariableException");
        } catch (InvalidVariableException e) {
        }

        try {
            BSHFParams("", "", "");
            fail("Expected InvalidVariableException");
        } catch (InvalidVariableException e) {
        }

        BeanShell bsh;
        try {
            bsh = BSHFParams("", "", null);
            assertEquals("", bsh.execute());
        } catch (InvalidVariableException e) {
            fail("BeanShell not present");
        }

        bsh = BSHFParams("1", null, null);
        assertEquals("1", bsh.execute());

        bsh = BSHFParams("1+1", "VAR", null);
        assertEquals("2", bsh.execute());
        assertEquals("2", vars.get("VAR"));

        // Check some initial variables
        bsh = BSHFParams("return threadName", null, null);
        assertEquals(Thread.currentThread().getName(), bsh.execute());
        bsh = BSHFParams("return log.getClass().getName()", null, null);
        assertEquals(log.getClass().getName(), bsh.execute());

        // Check source works
        bsh = BSHFParams("source (\"testfiles/BeanShellTest.bsh\")", null, null);
        assertEquals("9876", bsh.execute());

        // Check persistence
        bsh = BSHFParams("${SCR1}", null, null);

        vars.put("SCR1", "var1=11");
        assertEquals("11", bsh.execute());

        vars.put("SCR1", "var2=22");
        assertEquals("22", bsh.execute());

        vars.put("SCR1", "x=var1");
        assertEquals("11", bsh.execute());

        vars.put("SCR1", "++x");
        assertEquals("12", bsh.execute());

        vars.put("VAR1", "test");
        vars.put("SCR1", "vars.get(\"VAR1\")");
        assertEquals("test", bsh.execute());

        // Check init file functioning
        JMeterUtils.getJMeterProperties().setProperty(BeanShell.INIT_FILE, fn);
        bsh = BSHFParams("${SCR2}", null, null);
        vars.put("SCR2", "getprop(\"" + BeanShell.INIT_FILE + "\")");
        assertEquals(fn, bsh.execute());// Check that bsh has read the file
        vars.put("SCR2", "getprop(\"avavaav\",\"default\")");
        assertEquals("default", bsh.execute());
        vars.put("SCR2", "++i");
        assertEquals("1", bsh.execute());
        vars.put("SCR2", "++i");
        assertEquals("2", bsh.execute());

    }

    // Function objects to be tested
    private static CSVRead cr1, cr4;

    // Helper class used to implement co-routine between two threads
    private static class Baton {
        void pass() {
            done();
            try {
                wait(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        void done() {
            notifyAll();
        }

    }

    private static final Baton BATON = new Baton();

    public void CSVThread1() throws Exception {
        Thread.currentThread().setName("One");
        synchronized (BATON) {

            assertEquals("b1", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            assertEquals("b2", cr1.execute(null, null));

            BATON.pass();

            assertEquals("", cr4.execute(null, null));

            assertEquals("b4", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            BATON.pass();

            assertEquals("b3", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            BATON.done();
        }
    }

    public void CSVThread2() throws Exception {
        Thread.currentThread().setName("Two");
        Thread.sleep(500);// Allow other thread to start
        synchronized (BATON) {

            assertEquals("b3", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            BATON.pass();

            assertEquals("b1", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            assertEquals("b2", cr1.execute(null, null));

            BATON.pass();

            assertEquals("", cr4.execute(null, null));

            assertEquals("b4", cr1.execute(null, null));

            BATON.done();
        }
    }


    public void CSVSetup() throws Exception {
        cr1 = setCSVReadParams("testfiles/test.csv", "1");
        cr4 = setCSVReadParams("testfiles/test.csv", "next");
    }


    // XPathFileContainer tests
    
    public void XPathtestNull() throws Exception {
        try {
            new XPathFileContainer("nosuch.xml", "/");
            fail("Should not find the file");
        } catch (FileNotFoundException e) {
        }
    }

    public void XPathtestrowNum() throws Exception {
        XPathFileContainer f = new XPathFileContainer("../build.xml", "/project/target/@name");
        assertNotNull(f);

        int myRow = f.nextRow();
        assertEquals(0, myRow);
        assertEquals(1, f.getNextRow());

        myRow = f.nextRow();
        assertEquals(1, myRow);
        assertEquals(2, f.getNextRow());

        myRow = f.nextRow();
        assertEquals(2, myRow);
        assertEquals(3, f.getNextRow());
    }

    public void XPathtestColumns() throws Exception {
        XPathFileContainer f = new XPathFileContainer("../build.xml", "/project/target/@name");
        assertNotNull(f);
        assertTrue("Not empty", f.size() > 0);
        int last = 0;
        for (int i = 0; i < f.size(); i++) {
            last = f.nextRow();
            log.debug("found [" + i + "]" + f.getXPathString(last));
        }
        assertEquals(last + 1, f.size());

    }

    public void XPathtestDefault() throws Exception {
        XPathFileContainer f = new XPathFileContainer("../build.xml", "/project/@default");
        assertNotNull(f);
        assertTrue("Not empty", f.size() > 0);
        assertEquals("install", f.getXPathString(0));

    }
    
    public void XPathEmpty() throws Exception{
        XPath xp = setupXPath("","");
        String val=xp.execute();
        assertEquals("",val);
        val=xp.execute();
        assertEquals("",val);
        val=xp.execute();
        assertEquals("",val);
    }
    
    public void XPathNoFile() throws Exception{
        XPath xp = setupXPath("no-such-file","");
        String val=xp.execute();
        assertEquals("",val); // TODO - should check that error has been logged...
    }

    public void XPathFile() throws Exception{
        XPath xp = setupXPath("testfiles/XPathTest2.xml","note/body");
        assertEquals("Don't forget me this weekend!",xp.execute());
        
        xp = setupXPath("testfiles/XPathTest2.xml","//note2");
        assertEquals("", xp.execute());
        
        xp = setupXPath("testfiles/XPathTest2.xml","//note/to");
        assertEquals("Tove", xp.execute());
    }
    
    public void XPathFile1() throws Exception{
        XPath xp = setupXPath("testfiles/XPathTest.xml","//user/@username");
        assertEquals("u1",xp.execute());
        assertEquals("u2",xp.execute());
        assertEquals("u3",xp.execute());
        assertEquals("u4",xp.execute());
        assertEquals("u5",xp.execute());
        assertEquals("u1",xp.execute());
    }
    
    public void XPathFile2() throws Exception{
        XPath xp1  = setupXPath("testfiles/XPathTest.xml","//user/@username");
        XPath xp1a = setupXPath("testfiles/XPathTest.xml","//user/@username");
        XPath xp2  = setupXPath("testfiles/XPathTest.xml","//user/@password");
        XPath xp2a = setupXPath("testfiles/XPathTest.xml","//user/@password");
        assertEquals("u1",xp1.execute());
        assertEquals("p1",xp2.execute());
        assertEquals("p2",xp2.execute());
        assertEquals("u2",xp1a.execute());
        assertEquals("u3",xp1.execute());
        assertEquals("u4",xp1.execute());
        assertEquals("p3",xp2a.execute());

    }
    
    private static XPath sxp1,sxp2;
    // Use same XPath for both threads
    public void XPathSetup1() throws Exception{
        sxp1  = setupXPath("testfiles/XPathTest.xml","//user/@username");
        sxp2=sxp1;
    }

    // Use different XPath for both threads
    public void XPathSetup2() throws Exception{
        sxp1  = setupXPath("testfiles/XPathTest.xml","//user/@username");
        sxp2  = setupXPath("testfiles/XPathTest.xml","//user/@username");
    }

    public void XPathThread1() throws Exception {
        Thread.currentThread().setName("XPathOne");
        synchronized (BATON) {
            assertEquals("u1",sxp1.execute());
            assertEquals("u2",sxp1.execute());
            BATON.pass();
            assertEquals("u5",sxp1.execute());
            BATON.pass();
            assertEquals("u2",sxp1.execute());
            BATON.done();
        }
    }

    public void XPathThread2() throws Exception {
        Thread.currentThread().setName("XPathTwo");
        Thread.sleep(500);
        synchronized (BATON) {
            assertEquals("u3",sxp2.execute());
            assertEquals("u4",sxp2.execute());
            BATON.pass();
            assertEquals("u1",sxp2.execute());
            BATON.pass();
            assertEquals("u3",sxp2.execute());
            BATON.done();
        }
    }

    private XPath setupXPath(String file, String expr) throws Exception{
        Collection<CompoundVariable> parms = new LinkedList<>();
        parms.add(new CompoundVariable(file));
        parms.add(new CompoundVariable(expr));
        XPath xp = new XPath();
        xp.setParameters(parms);
        return xp;        
    }

}
