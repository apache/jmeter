package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;

public class TestXPathFunction extends JMeterTestCase {

    private XPath setupXPath(String file, String expr) throws Exception{
        Collection parms = new LinkedList();
        parms.add(new CompoundVariable(file));
        parms.add(new CompoundVariable(expr));
        XPath xp = new XPath();
        xp.setParameters(parms);
        return xp;        
    }
    
    public void testEmpty() throws Exception{
        XPath xp = setupXPath("","");
        String val=xp.execute();
        assertEquals("",val);
        val=xp.execute();
        assertEquals("",val);
        val=xp.execute();
        assertEquals("",val);
    }
    
    public void testNoFile() throws Exception{
        XPath xp = setupXPath("no-such-file","");
        String val=xp.execute();
        assertEquals("",val); // TODO - should check that error has been logged...
    }
    
    public void testFile1() throws Exception{
        XPath xp = setupXPath("testfiles/XPathTest.xml","//user/@username");
        assertEquals("u1",xp.execute());
        assertEquals("u2",xp.execute());
        assertEquals("u1",xp.execute());
    }
    
    public void testFile2() throws Exception{
        XPath xp1  = setupXPath("testfiles/XPathTest.xml","//user/@username");
        XPath xp1a = setupXPath("testfiles/XPathTest.xml","//user/@username");
        XPath xp2  = setupXPath("testfiles/XPathTest.xml","//user/@password");
        XPath xp2a = setupXPath("testfiles/XPathTest.xml","//user/@password");
        assertEquals("u1",xp1.execute());
        assertEquals("p1",xp2.execute());
        assertEquals("p2",xp2.execute());
        assertEquals("u2",xp1a.execute());
        assertEquals("u1",xp1.execute());
        assertEquals("u2",xp1.execute());
        assertEquals("p1",xp2a.execute());

    }
}
