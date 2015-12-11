package org.apache.jmeter.report.core;

import junit.framework.TestCase;

public class SampleMetadataTest extends TestCase {

    public void testToString() {
        assertEquals("a,b", new SampleMetadata(',', "a", "b").toString());
    }

}
