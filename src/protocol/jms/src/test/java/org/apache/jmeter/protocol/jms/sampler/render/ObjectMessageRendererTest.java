/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.jms.sampler.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.jmeter.protocol.jms.sampler.PublisherSampler;
import org.junit.jupiter.api.Test;

public class ObjectMessageRendererTest extends MessageRendererTest<Serializable> {

    private static String CP1252_SAFE_XML =
            "<org.apache.jmeter.protocol.jms.sampler.render.Person><name>eéè€</name></org.apache.jmeter.protocol.jms.sampler.render.Person>";

    private ObjectMessageRenderer render = RendererFactory.getInstance().getObject();

    @Override
    public ObjectMessageRenderer getRenderer() {
        return render;
    }

    @Test
    public void getValueFromText() {
        String text = "<org.apache.jmeter.protocol.jms.sampler.render.Person><name>Doe</name></org.apache.jmeter.protocol.jms.sampler.render.Person>";
        Serializable object = render.getValueFromText(text);
        assertObject(object, "Doe");
    }

   private void assertObject(Serializable object, String name) {
       assertNotNull(object, "object");
       assertEquals(Person.class, object.getClass(), "object.class");
       Person p = (Person) object;
       assertEquals(name, p.getName(), "object.name");
   }

    @Test
    public void getValueFromFile_inRawMode() throws IOException {
        String fileName = writeFile(
                "object_doe.xml",
                "<org.apache.jmeter.protocol.jms.sampler.render.Person><name>Doe</name></org.apache.jmeter.protocol.jms.sampler.render.Person>"
        );
        assertValueFromFile(object -> {
            assertObject(object, "Doe");
            Person p = (Person) object;
            assertSame(p, getFirstCachedValue(), "cache");
        }, fileName, false);
    }

    @Test
    public void getValueFromFile_withForcedEncoding() throws IOException {
        String filename = writeFile("object_cp1252.xml", CP1252_SAFE_XML, Charset.forName("Cp1252"));
        Serializable object = getRenderer().getValueFromFile(filename, "Cp1252", true, cache);
        assertObject(object, "eéè€");
        assertEquals(CP1252_SAFE_XML, getFirstCachedValue(), "cache");
    }

    @Test
    public void getValueFromFile_withDefaultEncodingAndNoProlog() throws IOException {
        String content =
                "<org.apache.jmeter.protocol.jms.sampler.render.Person><name>eéè€</name></org.apache.jmeter.protocol.jms.sampler.render.Person>";
        String filename = writeFile("object_utf8.xml", content, StandardCharsets.UTF_8);
        Serializable object = getRenderer().getValueFromFile(filename, PublisherSampler.DEFAULT_ENCODING, true, cache);
        assertObject(object, "eéè€");
        assertEquals(CP1252_SAFE_XML, getFirstCachedValue(), "cache");

    }

    @Test
    public void getValueFromFile_withDefaultEncodingAndProlog() throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"Windows-1252\"?>\n" + CP1252_SAFE_XML;
        String filename = writeFile("object_prolog_cp1252.xml", content, Charset.forName("Cp1252"));
        Serializable object = getRenderer().getValueFromFile(filename, PublisherSampler.DEFAULT_ENCODING, true, cache);
        assertObject(object, "eéè€");
        Person p = (Person) object;
        assertEquals("eéè€", p.getName(), "object.name");
        Object firstCachedValue = getFirstCachedValue();
        assertEquals(content, convertLineEndingsToSystem(firstCachedValue), "cache");
    }

    private String convertLineEndingsToSystem(Object firstCachedValue) {
        return firstCachedValue.toString().replaceAll("[\r\n]+", "\n");
    }
}
