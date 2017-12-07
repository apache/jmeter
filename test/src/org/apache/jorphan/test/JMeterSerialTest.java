package org.apache.jorphan.test;

/**
 * Used to tag tests which need to be run on their own (in serial) because
 * either, they cause other tests to fail, or they fail when run in parallel.
 */
public interface JMeterSerialTest {
}
