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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

/**
 * Combo box for the HTTP version of a sampler, offering only the versions the selected
 * implementation supports. The items are the values stored in the {@code HTTPSampler.httpVersion}
 * property, the rendering spells out how HTTP/2 is applied.
 *
 * @since 5.7
 */
public class HttpVersionComboBox extends JComboBox<String> {

    private static final long serialVersionUID = 1L;

    public HttpVersionComboBox() {
        super(HTTPSamplerFactory.getHttpVersions(""));
        setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, getLabel(value), index, isSelected, cellHasFocus);
            }
        });
    }

    private static Object getLabel(Object value) {
        if (HTTPConstants.HTTP_VERSION_2.equals(value)) {
            // Spelled out, as HTTP/2 falls back to HTTP/1.1 when the server does not support it
            return "HTTP/2 Negotiate";
        }
        return value;
    }

    /**
     * Keeps the offered HTTP versions in sync with the implementation selected in the given combo
     * box, so combinations an implementation would ignore cannot be selected.
     *
     * @param httpImplementation combo box holding the implementation of the sampler
     */
    public void bindToImplementation(JComboBox<String> httpImplementation) {
        httpImplementation.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                setImplementation(Objects.toString(event.getItem(), ""));
            }
        });
        setImplementation(Objects.toString(httpImplementation.getSelectedItem(), ""));
    }

    /**
     * Restricts the offered HTTP versions to the ones the given implementation supports.
     *
     * @param implementation implementation name, an empty value refers to the default implementation
     */
    public void setImplementation(String implementation) {
        String[] versions = HTTPSamplerFactory.getHttpVersions(implementation);
        String selected = (String) getSelectedItem();
        if (Arrays.equals(versions, getItems())) {
            return;
        }
        setModel(new DefaultComboBoxModel<>(versions));
        setSelectedItem(Arrays.asList(versions).contains(selected) ? selected : "");
    }

    private String[] getItems() {
        String[] items = new String[getItemCount()];
        Arrays.setAll(items, this::getItemAt);
        return items;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A version which the selected implementation does not offer, e.g. one read from a test plan
     * saved with an earlier version of JMeter, is added to the model instead of being dropped
     * silently.
     */
    @Override
    public void setSelectedItem(Object item) {
        if (item instanceof String version && !Arrays.asList(getItems()).contains(version)
                && getModel() instanceof DefaultComboBoxModel) {
            ((DefaultComboBoxModel<String>) getModel()).addElement(version);
        }
        super.setSelectedItem(item);
    }
}
