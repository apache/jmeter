/*
 * Created on May 13, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.jorphan.math;

import junit.framework.TestCase;

/**
 * @author mstover
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestStatCalculator extends TestCase {

    StatCalculator calc;
    
    /**
     * 
     */
    public TestStatCalculator() {
        super();
    }
    /**
     * @param arg0
     */
    public TestStatCalculator(String arg0) {
        super(arg0);
    }
    
    public void setUp()
    {
        calc = new StatCalculator();
    }
    
    public void testPercentagePoint() throws Exception
    {
        System.out.println("calc test going on");
        calc.addValue(10);
        calc.addValue(9);
        calc.addValue(5);
        calc.addValue(6);
        calc.addValue(1);
        calc.addValue(3);
        calc.addValue(8);
        calc.addValue(2);
        calc.addValue(7);
        calc.addValue(4);
        assertEquals(10,calc.getCount());
        assertEquals(9,calc.getPercentPoint(0.8999999).intValue());
    }
}
