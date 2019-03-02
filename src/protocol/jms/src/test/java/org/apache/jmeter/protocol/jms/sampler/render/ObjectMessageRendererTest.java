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
 */

package org.apache.jmeter.protocol.jms.sampler.render;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.Serializable;

import org.apache.jmeter.protocol.jms.sampler.PublisherSampler;
import org.junit.Test;

public class ObjectMessageRendererTest extends MessageRendererTest<Serializable> {

    private ObjectMessageRenderer render = RendererFactory.getInstance().getObject();
    @Override
    public ObjectMessageRenderer getRenderer() {
        return render;
    }

    @Test
    public void getValueFromText() {
        Serializable object = render.getValueFromText(getDoeContent());
        assertObject(object, "Doe");
    }

   private void assertObject(Serializable object, String name) {
        assertNotNull("object", object);
        assertEquals("object.class", Person.class, object.getClass());
        Person p = (Person) object;
        assertEquals("object.name", name, p.getName());
    }

    @Test
    public void getValueFromFile_inRawMode() {
        assertValueFromFile(object -> {
            assertObject(object, "Doe");
            Person p = (Person) object;
            assertSame("cache", p, getFirstCachedValue());
        }, "object_doe.xml", false);
    }

    @Test
    public void getValueFromFile_withForcedEncoding() {
        String filename = getResourceFile("object_cp1252.xml");
        Serializable object = getRenderer().getValueFromFile(filename, "Cp1252", true, cache);
        assertObject(object, "eéè€");
        assertEquals("cache", getUnicodeContent(), getFirstCachedValue());

    }

    @Test
    public void getValueFromFile_withDefaultEncodingAndNoProlog() {
        String filename = getResourceFile("object_utf8.xml");
        Serializable object = getRenderer().getValueFromFile(filename, PublisherSampler.DEFAULT_ENCODING, true, cache);
        assertObject(object, "eéè€");
        assertEquals("cache", getUnicodeContent(), getFirstCachedValue());

    }

    @Test
    public void getValueFromFile_withDefaultEncodingAndProlog() {
        String filename = getResourceFile("object_prolog_cp1252.xml");
        Serializable object = getRenderer().getValueFromFile(filename, PublisherSampler.DEFAULT_ENCODING, true, cache);
        assertObject(object, "eéè€");
        Person p = (Person) object;
        assertEquals("object.name", "eéè€", p.getName());
        assertEquals("cache", format("<?xml version=\"1.0\" encoding=\"Windows-1252\"?>%n%s", getUnicodeContent()), getFirstCachedValue());

    }

    private String getUnicodeContent() {
        return "<org.apache.jmeter.protocol.jms.sampler.render.Person><name>eéè€</name></org.apache.jmeter.protocol.jms.sampler.render.Person>";
    }

    private String getDoeContent() {
        return "<org.apache.jmeter.protocol.jms.sampler.render.Person><name>Doe</name></org.apache.jmeter.protocol.jms.sampler.render.Person>";
    }
}
