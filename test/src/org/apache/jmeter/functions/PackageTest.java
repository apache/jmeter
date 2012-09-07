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

/*
 * Package to test functions
 * 
 * Functions are created and parameters set up in one thread.
 * 
 * They are then tested in another thread, or two threads running in parallel
 * 
 */
package org.apache.jmeter.functions;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.log.Logger;

/**
 * Test cases for Functions
 */
public class PackageTest extends JMeterTestCase {

    private static final Logger log = LoggingManager.getLoggerForClass();

//  static {
//       LoggingManager.setPriority("DEBUG","jmeter");
//       LoggingManager.setTarget(new java.io.PrintWriter(System.out));
//  }

    public PackageTest(String arg0) {
        super(arg0);
    }

    // Create the CSVRead function and set its parameters.
    private static CSVRead setCSVReadParams(String p1, String p2) throws Exception {
        CSVRead cr = new CSVRead();
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
        if (p1 != null) {
            parms.add(new CompoundVariable(p1));
        }
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        cr.setParameters(parms);
        return cr;
    }

    // Create the StringFromFile function and set its parameters.
    private static StringFromFile SFFParams(String p1, String p2, String p3, String p4) throws Exception {
        StringFromFile sff = new StringFromFile();
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
        if (p1 != null) {
            parms.add(new CompoundVariable(p1));
        }
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        if (p3 != null) {
            parms.add(new CompoundVariable(p3));
        }
        if (p4 != null) {
            parms.add(new CompoundVariable(p4));
        }
        sff.setParameters(parms);
        return sff;
    }

    // Create the SplitFile function and set its parameters.
    private static SplitFunction splitParams(String p1, String p2, String p3) throws Exception {
        SplitFunction split = new SplitFunction();
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
        parms.add(new CompoundVariable(p1));
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        if (p3 != null) {
            parms.add(new CompoundVariable(p3));
        }
        split.setParameters(parms);
        return split;
    }

    // Create the BeanShell function and set its parameters.
    private static BeanShell BSHFParams(String p1, String p2, String p3) throws Exception {
        BeanShell bsh = new BeanShell();
        bsh.setParameters(makeParams(p1, p2, p3));
        return bsh;
    }

    private static Collection<CompoundVariable> makeParams(String p1, String p2, String p3) {
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
        if (p1 != null) {
            parms.add(new CompoundVariable(p1));
        }
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        if (p3 != null) {
            parms.add(new CompoundVariable(p3));
        }
        return parms;
    }

    public static Test suite() throws Exception {
        TestSuite allsuites = new TestSuite("Function PackageTest");

        if (!BeanShellInterpreter.isInterpreterPresent()){
            final String msg = "BeanShell jar not present, tests ignored";
            log.warn(msg);
        } else {
            TestSuite bsh = new TestSuite("BeanShell");
            bsh.addTest(new PackageTest("BSH1"));
            allsuites.addTest(bsh);
        }

        TestSuite suite = new TestSuite("SingleThreaded");
        suite.addTest(new PackageTest("CSVParams"));
        suite.addTest(new PackageTest("CSVNoFile"));
        suite.addTest(new PackageTest("CSVSetup"));
        suite.addTest(new PackageTest("CSVRun"));

        suite.addTest(new PackageTest("CSValias"));
        suite.addTest(new PackageTest("CSVBlankLine"));
        allsuites.addTest(suite);

        // Reset files
        suite.addTest(new PackageTest("CSVSetup"));
        TestSuite par = new ActiveTestSuite("Parallel");
        par.addTest(new PackageTest("CSVThread1"));
        par.addTest(new PackageTest("CSVThread2"));
        allsuites.addTest(par);

        TestSuite sff = new TestSuite("StringFromFile");
        sff.addTest(new PackageTest("SFFTest1"));
        sff.addTest(new PackageTest("SFFTest2"));
        sff.addTest(new PackageTest("SFFTest3"));
        sff.addTest(new PackageTest("SFFTest4"));
        sff.addTest(new PackageTest("SFFTest5"));
        allsuites.addTest(sff);

        TestSuite split = new TestSuite("SplitFunction");
        split.addTest(new PackageTest("splitTest1"));
        allsuites.addTest(split);

        TestSuite xpath = new TestSuite("XPath");
        xpath.addTest(new PackageTest("XPathtestColumns"));
        xpath.addTest(new PackageTest("XPathtestDefault"));
        xpath.addTest(new PackageTest("XPathtestNull"));
        xpath.addTest(new PackageTest("XPathtestrowNum"));
        xpath.addTest(new PackageTest("XPathEmpty"));
        xpath.addTest(new PackageTest("XPathFile1"));
        xpath.addTest(new PackageTest("XPathFile2"));
        xpath.addTest(new PackageTest("XPathNoFile"));

        allsuites.addTest(xpath);
        
        TestSuite random = new TestSuite("Random");
        random.addTest(new PackageTest("randomTest1"));
        allsuites.addTest(random);

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

        TestSuite variable = new TestSuite("Variable");
        variable.addTest(new PackageTest("variableTest1"));
        allsuites.addTest(variable);
        
        TestSuite eval = new TestSuite("Eval");
        eval.addTest(new PackageTest("evalTest1"));
        eval.addTest(new PackageTest("evalTest2"));
        allsuites.addTest(eval);

        TestSuite intSum = new TestSuite("Sums");
        intSum.addTest(new PackageTest("sumTest"));
        allsuites.addTest(intSum);

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

    public void splitTest1() throws Exception {
        String src = "";

        try {
            splitParams("a,b,c", null, null);
            fail("Expected InvalidVariableException (wrong number of parameters)");
        } catch (InvalidVariableException e) {
            // OK
        }
        src = "a,b,c";
        SplitFunction split;
        split = splitParams(src, "VAR1", null);
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR1"));
        assertEquals("3", vars.get("VAR1_n"));
        assertEquals("a", vars.get("VAR1_1"));
        assertEquals("b", vars.get("VAR1_2"));
        assertEquals("c", vars.get("VAR1_3"));
        assertNull(vars.get("VAR1_4"));

        split = splitParams(src, "VAR2", ",");
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR2"));
        assertEquals("3", vars.get("VAR2_n"));
        assertEquals("a", vars.get("VAR2_1"));
        assertEquals("b", vars.get("VAR2_2"));
        assertEquals("c", vars.get("VAR2_3"));
        assertNull(vars.get("VAR2_4"));

        src = "a|b|c";
        split = splitParams(src, "VAR3", "|");
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR3"));
        assertEquals("3", vars.get("VAR3_n"));
        assertEquals("a", vars.get("VAR3_1"));
        assertEquals("b", vars.get("VAR3_2"));
        assertEquals("c", vars.get("VAR3_3"));
        assertNull(vars.get("VAR3_4"));

        src = "a|b||";
        split = splitParams(src, "VAR4", "|");
        assertEquals(src, split.execute());
        assertEquals(src, vars.get("VAR4"));
        assertEquals("4", vars.get("VAR4_n"));
        assertEquals("a", vars.get("VAR4_1"));
        assertEquals("b", vars.get("VAR4_2"));
        assertEquals("?", vars.get("VAR4_3"));
        assertNull(vars.get("VAR4_5"));

        src = "a,,c";
        vars.put("VAR", src);
        split = splitParams("${VAR}", "VAR", null);
        assertEquals(src, split.execute());
        assertEquals("3", vars.get("VAR_n"));
        assertEquals("a", vars.get("VAR_1"));
        assertEquals("?", vars.get("VAR_2"));
        assertEquals("c", vars.get("VAR_3"));
        assertNull(vars.get("VAR_4"));

        src = "a,b";
        vars.put("VAR", src);
        split = splitParams("${VAR}", "VAR", null);
        assertEquals(src, split.execute());
        assertEquals("2", vars.get("VAR_n"));
        assertEquals("a", vars.get("VAR_1"));
        assertEquals("b", vars.get("VAR_2"));
        assertNull(vars.get("VAR_3"));

        src = "a,,c,";
        vars.put("VAR", src);
        split = splitParams("${VAR}", "VAR5", null);
        assertEquals(src, split.execute());
        assertEquals("4", vars.get("VAR5_n"));
        assertEquals("a", vars.get("VAR5_1"));
        assertEquals("?", vars.get("VAR5_2"));
        assertEquals("c", vars.get("VAR5_3"));
        assertEquals("?", vars.get("VAR5_4"));
        assertNull(vars.get("VAR5_5"));

}

    public void SFFTest1() throws Exception {
        StringFromFile sff1 = SFFParams("testfiles/SFFTest#'.'txt", "", "1", "3");
        assertEquals("uno", sff1.execute());
        assertEquals("dos", sff1.execute());
        assertEquals("tres", sff1.execute());
        assertEquals("cuatro", sff1.execute());
        assertEquals("cinco", sff1.execute());
        assertEquals("one", sff1.execute());
        assertEquals("two", sff1.execute());
        sff1.execute();
        sff1.execute();
        assertEquals("five", sff1.execute());
        assertEquals("eins", sff1.execute());
        sff1.execute();
        sff1.execute();
        sff1.execute();
        assertEquals("fuenf", sff1.execute());
        try {
            sff1.execute();
            fail("Should have thrown JMeterStopThreadException");
        } catch (JMeterStopThreadException e) {
            // expected
        }
    }

    public void SFFTest2() throws Exception {
        StringFromFile sff = SFFParams("testfiles/SFFTest1.txt", "", null, null);
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        assertEquals("uno", sff.execute()); // Restarts
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
    }

    public void SFFTest3() throws Exception {
        StringFromFile sff = SFFParams("testfiles/SFFTest1.txt", "", "", "");
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        assertEquals("uno", sff.execute()); // Restarts
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
    }

    public void SFFTest4() throws Exception {
        StringFromFile sff = SFFParams("xxtestfiles/SFFTest1.txt", "", "", "");
        assertEquals(StringFromFile.ERR_IND, sff.execute());
        assertEquals(StringFromFile.ERR_IND, sff.execute());
    }

    // Test that only loops twice
    public void SFFTest5() throws Exception {
        StringFromFile sff = SFFParams("testfiles/SFFTest1.txt", "", "", "2");
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        assertEquals("uno", sff.execute());
        assertEquals("dos", sff.execute());
        assertEquals("tres", sff.execute());
        assertEquals("cuatro", sff.execute());
        assertEquals("cinco", sff.execute());
        try {
            sff.execute();
            fail("Should have thrown JMeterStopThreadException");
        } catch (JMeterStopThreadException e) {
            // expected
        }
    }

    // Function objects to be tested
    private static CSVRead cr1, cr2, cr3, cr4, cr5, cr6;

    // Helper class used to implement co-routine between two threads
    private static class Baton {
        void pass() {
            done();
            try {
                // System.out.println(">wait:"+Thread.currentThread().getName());
                wait(1000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            // System.out.println("<wait:"+Thread.currentThread().getName());

        }

        void done() {
            // System.out.println(">done:"+Thread.currentThread().getName());
            notifyAll();
        }

    }

    private static final Baton baton = new Baton();

    public void CSVThread1() throws Exception {
        Thread.currentThread().setName("One");
        synchronized (baton) {

            assertEquals("b1", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            assertEquals("b2", cr1.execute(null, null));

            baton.pass();

            assertEquals("", cr4.execute(null, null));

            assertEquals("b4", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            baton.pass();

            assertEquals("b3", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            baton.done();
        }
    }

    public void CSVThread2() throws Exception {
        Thread.currentThread().setName("Two");
        Thread.sleep(500);// Allow other thread to start
        synchronized (baton) {

            assertEquals("b3", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            baton.pass();

            assertEquals("b1", cr1.execute(null, null));

            assertEquals("", cr4.execute(null, null));

            assertEquals("b2", cr1.execute(null, null));

            baton.pass();

            assertEquals("", cr4.execute(null, null));

            assertEquals("b4", cr1.execute(null, null));

            baton.done();
        }
    }

    public void CSVRun() throws Exception {
        assertEquals("b1", cr1.execute(null, null));
        assertEquals("c1", cr2.execute(null, null));
        assertEquals("d1", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b2", cr1.execute(null, null));
        assertEquals("c2", cr2.execute(null, null));
        assertEquals("d2", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b3", cr1.execute(null, null));
        assertEquals("c3", cr2.execute(null, null));
        assertEquals("d3", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b4", cr1.execute(null, null));
        assertEquals("c4", cr2.execute(null, null));
        assertEquals("d4", cr3.execute(null, null));

        assertEquals("", cr4.execute(null, null));
        assertEquals("b1", cr1.execute(null, null));
        assertEquals("c1", cr2.execute(null, null));
        assertEquals("d1", cr3.execute(null, null));

        assertEquals("a1", cr5.execute(null, null));
        assertEquals("", cr6.execute(null, null));
        assertEquals("a2", cr5.execute(null, null));

    }

    public void CSVParams() throws Exception {
        try {
            setCSVReadParams(null, null);
            fail("Should have failed");
        } catch (InvalidVariableException e) {
        }
        try {
            setCSVReadParams(null, "");
            fail("Should have failed");
        } catch (InvalidVariableException e) {
        }
        try {
            setCSVReadParams("", null);
            fail("Should have failed");
        } catch (InvalidVariableException e) {
        }
    }

    public void CSVSetup() throws Exception {
        cr1 = setCSVReadParams("testfiles/test.csv", "1");
        cr2 = setCSVReadParams("testfiles/test.csv", "2");
        cr3 = setCSVReadParams("testfiles/test.csv", "3");
        cr4 = setCSVReadParams("testfiles/test.csv", "next");
        cr5 = setCSVReadParams("", "0");
        cr6 = setCSVReadParams("", "next");
    }

    public void CSValias() throws Exception {
        cr1 = setCSVReadParams("testfiles/test.csv", "*A");
        cr2 = setCSVReadParams("*A", "1");
        cr3 = setCSVReadParams("*A", "next");

        cr4 = setCSVReadParams("testfiles/test.csv", "*B");
        cr5 = setCSVReadParams("*B", "2");
        cr6 = setCSVReadParams("*B", "next");

        String s;

        s = cr1.execute(null, null); // open as *A
        assertEquals("", s);
        s = cr2.execute(null, null); // col 1, line 1, *A
        assertEquals("b1", s);

        s = cr4.execute(null, null);// open as *B
        assertEquals("", s);
        s = cr5.execute(null, null);// col2 line 1
        assertEquals("c1", s);

        s = cr3.execute(null, null);// *A next
        assertEquals("", s);
        s = cr2.execute(null, null);// col 1, line 2, *A
        assertEquals("b2", s);

        s = cr5.execute(null, null);// col2, line 1, *B
        assertEquals("c1", s);

        s = cr6.execute(null, null);// *B next
        assertEquals("", s);

        s = cr5.execute(null, null);// col2, line 2, *B
        assertEquals("c2", s);

    }

    public void CSVNoFile() throws Exception {
        String s;

        cr1 = setCSVReadParams("xtestfiles/test.csv", "1");
        log.info("Expecting file not found");
        s = cr1.execute(null, null);
        assertEquals("", s);

        cr2 = setCSVReadParams("xtestfiles/test.csv", "next");
        log.info("Expecting no entry for file");
        s = cr2.execute(null, null);
        assertEquals("", s);

        cr3 = setCSVReadParams("xtestfiles/test.csv", "*ABC");
        log.info("Expecting file not found");
        s = cr3.execute(null, null);
        assertEquals("", s);

        cr4 = setCSVReadParams("*ABC", "1");
        log.info("Expecting cannot open file");
        s = cr4.execute(null, null);
        assertEquals("", s);
    }

    // Check blank lines are treated as EOF
    public void CSVBlankLine() throws Exception {
        CSVRead csv1 = setCSVReadParams("testfiles/testblank.csv", "1");
        CSVRead csv2 = setCSVReadParams("testfiles/testblank.csv", "next");

        String s;

        for (int i = 1; i <= 2; i++) {
            s = csv1.execute(null, null);
            assertEquals("b1", s);

            s = csv2.execute(null, null);
            assertEquals("", s);

            s = csv1.execute(null, null);
            assertEquals("b2", s);

            s = csv2.execute(null, null);
            assertEquals("", s);
        }

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
        // assertEquals("Expected 4 lines",4,f.size());

        int myRow = f.nextRow();
        assertEquals(0, myRow);
        assertEquals(1, f.getNextRow());

        myRow = f.nextRow();
        assertEquals(1, myRow);
        assertEquals(2, f.getNextRow());

        myRow = f.nextRow();
        assertEquals(2, myRow);
        assertEquals(3, f.getNextRow());

        // myRow = f.nextRow();
        // assertEquals(3,myRow);
        // assertEquals(0,f.getNextRow());

        // myRow = f.nextRow();
        // assertEquals(0,myRow);
        // assertEquals(1,f.getNextRow());

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
        synchronized (baton) {
            assertEquals("u1",sxp1.execute());
            assertEquals("u2",sxp1.execute());
            baton.pass();
            assertEquals("u5",sxp1.execute());
            baton.pass();
            assertEquals("u2",sxp1.execute());
            baton.done();
        }
    }

    public void XPathThread2() throws Exception {
        Thread.currentThread().setName("XPathTwo");
        Thread.sleep(500);
        synchronized (baton) {
            assertEquals("u3",sxp2.execute());
            assertEquals("u4",sxp2.execute());
            baton.pass();
            assertEquals("u1",sxp2.execute());
            baton.pass();
            assertEquals("u3",sxp2.execute());
            baton.done();
        }
    }

    private XPath setupXPath(String file, String expr) throws Exception{
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
        parms.add(new CompoundVariable(file));
        parms.add(new CompoundVariable(expr));
        XPath xp = new XPath();
        xp.setParameters(parms);
        return xp;        
    }
    

    
    public void randomTest1() throws Exception {
        Random r = new Random();
        Collection<CompoundVariable> parms = makeParams("0","10000000000","VAR");
        r.setParameters(parms);
        //String s = 
            r.execute(null,null);
    }

    public void variableTest1() throws Exception {
        Variable r = new Variable();
        vars.put("A_1","a1");
        vars.put("A_2","a2");
        vars.put("one","1");
        vars.put("two","2");
        vars.put("V","A");
        Collection<CompoundVariable> parms;
        String s;
        
        parms = makeParams("V",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("A",s);
        
        parms = makeParams("X",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("X",s);
        
        parms = makeParams("A${X}",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("A${X}",s);
        
        parms = makeParams("A_1",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a1",s);
        
        parms = makeParams("A_2",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a2",s);
        
        parms = makeParams("A_${two}",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a2",s);
        
        parms = makeParams("${V}_${one}",null,null);
        r.setParameters(parms);
        s = r.execute(null,null);
        assertEquals("a1",s);
    }        

    public void evalTest1() throws Exception {
        EvalFunction eval = new EvalFunction();
        vars.put("query","select ${column} from ${table}");
        vars.put("column","name");
        vars.put("table","customers");
        Collection<CompoundVariable> parms;
        String s;
        
        parms = makeParams("${query}",null,null);
        eval.setParameters(parms);
        s = eval.execute(null,null);
        assertEquals("select name from customers",s);
        
    }

    public void evalTest2() throws Exception {
        EvalVarFunction evalVar = new EvalVarFunction();
        vars.put("query","select ${column} from ${table}");
        vars.put("column","name");
        vars.put("table","customers");
        Collection<CompoundVariable> parms;
        String s;
        
        parms = makeParams("query",null,null);
        evalVar.setParameters(parms);
        s = evalVar.execute(null,null);
        assertEquals("select name from customers",s);
    }
    
    public void sumTest() throws Exception {
        String maxIntVal = Integer.toString(Integer.MAX_VALUE);
        String minIntVal = Integer.toString(Integer.MIN_VALUE);

        { // prevent accidental use of is below
        IntSum is = new IntSum();
        checkInvalidParameterCounts(is,2);
        checkSum(is,"3", new String[]{"1","2"});
        checkSumNoVar(is,"3", new String[]{"1","2"});
        checkSum(is,"1", new String[]{"-1","1","1","1","-2","1"});
        checkSumNoVar(is,"1", new String[]{"-1","1","1","1","-2","1"});
        checkSumNoVar(is,"-1", new String[]{"-1","1","1","1","-2","-1"});
        checkSum(is,maxIntVal, new String[]{maxIntVal,"0"});
        checkSum(is,minIntVal, new String[]{maxIntVal,"1"}); // wrap-round check
        }

        LongSum ls = new LongSum();
        checkInvalidParameterCounts(ls,2);
        checkSum(ls,"3", new String[]{"1","2"});
        checkSum(ls,"1", new String[]{"-1","1","1","1","-1","0"});
        checkSumNoVar(ls,"3", new String[]{"1","2"});
        checkSumNoVar(ls,"1", new String[]{"-1","1","1","1","-1","0"});
        checkSumNoVar(ls,"0", new String[]{"-1","1","1","1","-1","-1"});
        String maxIntVal_1 = Long.toString(1+(long)Integer.MAX_VALUE);
        checkSum(ls,maxIntVal, new String[]{maxIntVal,"0"});
        checkSum(ls,maxIntVal_1, new String[]{maxIntVal,"1"}); // no wrap-round check
        String maxLongVal = Long.toString(Long.MAX_VALUE);
        String minLongVal = Long.toString(Long.MIN_VALUE);
        checkSum(ls,maxLongVal, new String[]{maxLongVal,"0"});
        checkSum(ls,minLongVal, new String[]{maxLongVal,"1"}); // wrap-round check
    }
    
    // Perform a sum and check the results
    private void checkSum(AbstractFunction func, String value, String [] addends)  throws Exception {
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
        for (int i=0; i< addends.length; i++){
            parms.add(new CompoundVariable(addends[i]));
        }
        parms.add(new CompoundVariable("Result"));
        func.setParameters(parms);
        assertEquals(value,func.execute(null,null));
        assertEquals(value,vars.getObject("Result"));       
    }
    // Perform a sum and check the results
    private void checkSumNoVar(AbstractFunction func, String value, String [] addends)  throws Exception {
        Collection<CompoundVariable> parms = new LinkedList<CompoundVariable>();
        for (int i=0; i< addends.length; i++){
            parms.add(new CompoundVariable(addends[i]));
        }
        func.setParameters(parms);
        assertEquals(value,func.execute(null,null));
    }
}
