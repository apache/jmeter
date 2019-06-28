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

package org.apache.jmeter.protocol.http.util;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.testelement.property.PropertyIterator;
import org.junit.Test;

public class TestHTTPFileArgs {

    @Test
    public void testConstructors() throws Exception {
        HTTPFileArgs files = new HTTPFileArgs();
        assertEquals(0, files.getHTTPFileArgCount());
    }

    @Test
    public void testAdding() throws Exception {
        HTTPFileArgs files = new HTTPFileArgs();
        assertEquals(0, files.getHTTPFileArgCount());
        files.addHTTPFileArg("hede");
        assertEquals(1, files.getHTTPFileArgCount());
        assertEquals("hede", ((HTTPFileArg) files.iterator().next().getObjectValue()).getPath());
        HTTPFileArg file = new HTTPFileArg("hodo");
        files.addHTTPFileArg(file);
        assertEquals(2, files.getHTTPFileArgCount());
        PropertyIterator iter = files.iterator();
        assertEquals("hede", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("hodo", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        files.addEmptyHTTPFileArg();
        assertEquals(3, files.getHTTPFileArgCount());
        iter = files.iterator();
        assertEquals("hede", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("hodo", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
    }

    @Test
    public void testSetHTTPFileArgs() throws Exception {
        List<HTTPFileArg> newHTTPFileArgs = new LinkedList<>();
        newHTTPFileArgs.add(new HTTPFileArg("hede"));
        HTTPFileArgs files = new HTTPFileArgs();
        files.setHTTPFileArgs(newHTTPFileArgs);
        assertEquals(1, files.getHTTPFileArgCount());
        assertEquals("hede", ((HTTPFileArg) files.iterator().next().getObjectValue()).getPath());
    }

    @Test
    public void testRemoving() throws Exception {
        HTTPFileArgs files = new HTTPFileArgs();
        assertEquals(0, files.getHTTPFileArgCount());
        files.addHTTPFileArg("hede");
        assertEquals(1, files.getHTTPFileArgCount());
        files.clear();
        assertEquals(0, files.getHTTPFileArgCount());
        files.addHTTPFileArg("file1");
        files.addHTTPFileArg("file2");
        files.addHTTPFileArg("file3");
        HTTPFileArg file = new HTTPFileArg("file4");
        files.addHTTPFileArg(file);
        files.addHTTPFileArg("file5");
        files.addHTTPFileArg("file6");
        assertEquals(6, files.getHTTPFileArgCount());
        files.removeHTTPFileArg("file3");
        assertEquals(5, files.getHTTPFileArgCount());
        PropertyIterator iter = files.iterator();
        assertEquals("file1", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file2", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file4", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file5", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file6", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        files.removeHTTPFileArg(file);
        assertEquals(4, files.getHTTPFileArgCount());
        iter = files.iterator();
        assertEquals("file1", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file2", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file5", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file6", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        files.removeHTTPFileArg(new HTTPFileArg("file5"));
        assertEquals(3, files.getHTTPFileArgCount());
        iter = files.iterator();
        assertEquals("file1", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file2", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file6", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        files.removeHTTPFileArg(1);
        assertEquals(2, files.getHTTPFileArgCount());
        iter = files.iterator();
        assertEquals("file1", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        assertEquals("file6", ((HTTPFileArg) iter.next().getObjectValue()).getPath());
        files.removeAllHTTPFileArgs();
        assertEquals(0, files.getHTTPFileArgCount());
    }

    @Test
    public void testToString() throws Exception {
        HTTPFileArgs files = new HTTPFileArgs();
        files.addHTTPFileArg("file1");
        files.addHTTPFileArg("file2");
        files.addHTTPFileArg("file3");
        assertEquals("path:'file1'|param:''|mimetype:''\n"
                    +"path:'file2'|param:''|mimetype:''\n"
                    +"path:'file3'|param:''|mimetype:''",
                    files.toString());
    }
}
