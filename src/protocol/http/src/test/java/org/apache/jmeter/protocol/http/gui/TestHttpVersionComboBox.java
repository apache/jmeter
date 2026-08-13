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

package org.apache.jmeter.protocol.http.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.junit.jupiter.api.Test;

public class TestHttpVersionComboBox {

    private static JComboBox<String> implementationComboBox() {
        JComboBox<String> implementation = new JComboBox<>(HTTPSamplerFactory.getImplementations());
        implementation.addItem("");
        return implementation;
    }

    private static List<String> items(JComboBox<String> comboBox) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            items.add(comboBox.getItemAt(i));
        }
        return items;
    }

    @Test
    void offersTheVersionsOfTheSelectedImplementation() {
        JComboBox<String> implementation = implementationComboBox();
        HttpVersionComboBox httpVersion = new HttpVersionComboBox();
        httpVersion.bindToImplementation(implementation);

        implementation.setSelectedItem("HttpClient5");
        assertEquals(Arrays.asList("", "HTTP/1.1", "HTTP/2", "HTTP/2 Strict"), items(httpVersion));

        implementation.setSelectedItem("Java");
        assertEquals(Arrays.asList("", "HTTP/1.1", "HTTP/2"), items(httpVersion));

        implementation.setSelectedItem("HttpClient4");
        assertEquals(Arrays.asList("", "HTTP/1.1"), items(httpVersion));
    }

    @Test
    void keepsTheSelectionWhenTheNewImplementationSupportsIt() {
        JComboBox<String> implementation = implementationComboBox();
        HttpVersionComboBox httpVersion = new HttpVersionComboBox();
        httpVersion.bindToImplementation(implementation);

        implementation.setSelectedItem("HttpClient5");
        httpVersion.setSelectedItem("HTTP/2");
        implementation.setSelectedItem("Java");

        assertEquals("HTTP/2", httpVersion.getSelectedItem());
    }

    @Test
    void resetsTheSelectionWhenTheNewImplementationDoesNotSupportIt() {
        JComboBox<String> implementation = implementationComboBox();
        HttpVersionComboBox httpVersion = new HttpVersionComboBox();
        httpVersion.bindToImplementation(implementation);

        implementation.setSelectedItem("HttpClient5");
        httpVersion.setSelectedItem("HTTP/2 Strict");
        implementation.setSelectedItem("Java");

        assertEquals("", httpVersion.getSelectedItem());
    }

    @Test
    void keepsAValueWhichIsNotOffered() {
        JComboBox<String> implementation = implementationComboBox();
        HttpVersionComboBox httpVersion = new HttpVersionComboBox();
        httpVersion.bindToImplementation(implementation);

        implementation.setSelectedItem("HttpClient4");
        httpVersion.setSelectedItem("HTTP/2");

        assertEquals("HTTP/2", httpVersion.getSelectedItem());
    }

    @Test
    void dropsAValueWhichIsNotOfferedWhenTheImplementationIsSetAgain() {
        HttpVersionComboBox httpVersion = new HttpVersionComboBox();

        httpVersion.setImplementation("HttpClient4");
        httpVersion.setSelectedItem("HTTP/2");
        assertEquals(Arrays.asList("", "HTTP/1.1", "HTTP/2"), items(httpVersion));

        httpVersion.setImplementation("HttpClient4");
        assertEquals(Arrays.asList("", "HTTP/1.1"), items(httpVersion));
        assertEquals("", httpVersion.getSelectedItem());
    }
}
