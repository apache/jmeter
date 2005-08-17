package org.apache.jorphan.util;

import junit.framework.TestCase;

public class TestJorphanUtils extends TestCase {

    public TestJorphanUtils() {
        super();
        // TODO Auto-generated constructor stub
    }

    public TestJorphanUtils(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }
    
        public void testReplace1() {
            assertEquals("xyzdef", JOrphanUtils.replaceFirst("abcdef", "abc", "xyz"));
        }

        public void testReplace2() {
            assertEquals("axyzdef", JOrphanUtils.replaceFirst("abcdef", "bc", "xyz"));
        }

        public void testReplace3() {
            assertEquals("abcxyz", JOrphanUtils.replaceFirst("abcdef", "def", "xyz"));
        }

        public void testReplace4() {
            assertEquals("abcdef", JOrphanUtils.replaceFirst("abcdef", "bce", "xyz"));
        }

        public void testReplace5() {
            assertEquals("abcdef", JOrphanUtils.replaceFirst("abcdef", "alt=\"\" ", ""));
        }

        public void testReplace6() {
            assertEquals("abcdef", JOrphanUtils.replaceFirst("abcdef", "alt=\"\" ", ""));
        }

        public void testReplace7() {
            assertEquals("alt=\"\"", JOrphanUtils.replaceFirst("alt=\"\"", "alt=\"\" ", ""));
        }

        public void testReplace8() {
            assertEquals("img src=xyz ", JOrphanUtils.replaceFirst("img src=xyz alt=\"\" ", "alt=\"\" ", ""));
        }

        public void testSplit1() {
            String in = "a,bc,,"; // Test ignore trailing split characters
            String out[] = JOrphanUtils.split(in, ",",true);
            assertEquals(2, out.length);
            assertEquals("a", out[0]);
            assertEquals("bc", out[1]);
        }

        public void testSplit2() {
            String in = ",,a,bc"; // Test leading split characters
            String out[] = JOrphanUtils.split(in, ",",true);
            assertEquals(2, out.length);
            assertEquals("a", out[0]);
            assertEquals("bc", out[1]);
            out = JOrphanUtils.split(in, ",",false);
            assertEquals("Should detect the leading split chars; ", 4, out.length);
            assertEquals("", out[0]);
            assertEquals("", out[1]);
            assertEquals("a", out[2]);
            assertEquals("bc", out[3]);
        }
        
        public void testTruncate() throws Exception
        {
            String in = "a,b,,,d,e,,f";
            String[] out = JOrphanUtils.split(in,",",true);
            assertEquals("d",out[2]);
            out = JOrphanUtils.split(in,",",false);
            assertEquals("",out[2]);
            
        }

}
