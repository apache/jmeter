package org.apache.jorphan.reflect;

import junit.framework.TestCase;

import org.apache.jorphan.util.JMeterException;
import org.junit.Test;

/**
 * Test various aspects of the {@link ClassTools} class
 */
public class TestClassTools extends TestCase {

    /**
     * Test that a class can be constructed using the default constructor
     * 
     * @throws JMeterException
     *             when something fails during object construction
     */
    @Test
    public void testConstructString() throws JMeterException {
        String dummy = (String) ClassTools.construct("java.lang.String");
        assertNotNull(dummy);
        assertEquals("", dummy);
    }

    /**
     * Test that a class can be constructed using an constructor with an integer
     * parameter
     * 
     * @throws JMeterException
     *             when something fails during object construction
     */
    @Test
    public void testConstructStringInt() throws JMeterException {
        Integer dummy = (Integer) ClassTools.construct("java.lang.Integer", 23);
        assertNotNull(dummy);
        assertEquals(Integer.valueOf(23), dummy);
    }

    /**
     * Test that a class can be constructed using an constructor with an string
     * parameter
     * 
     * @throws JMeterException
     *             when something fails during object construction
     */
    @Test
    public void testConstructStringString() throws JMeterException {
        String dummy = (String) ClassTools.construct("java.lang.String",
                "hello");
        assertNotNull(dummy);
        assertEquals("hello", dummy);
    }

    /**
     * Test that a simple method can be invoked on an object
     * 
     * @throws SecurityException
     *             when the method can not be used because of security concerns
     * @throws IllegalArgumentException
     *             when the method parameters does not match the given ones
     * @throws JMeterException
     *             when something fails while invoking the method
     */
    @Test
    public void testInvoke() throws SecurityException,
            IllegalArgumentException, JMeterException {
        Dummy dummy = new Dummy();
        ClassTools.invoke(dummy, "callMe");
        assertEquals(dummy.wasCalled(), true);
    }

    /**
     * Dummy class to be used for construction and invocation tests
     *
     */
    public static class Dummy {
        private boolean called = false;

        /**
         * @return <code>true</code> if {@link Dummy#callMe()} was called on
         *         this instance
         */
        public boolean wasCalled() {
            return this.called;
        }

        /**
         * Simple method to be called on void invocation
         */
        public void callMe() {
            this.called = true;
        }
    }

}
