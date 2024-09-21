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

package org.apache.jmeter.gui.menu;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JPopupMenu;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.timers.gui.AbstractTimerGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apiguardian.api.API;

/**
 * Internal class to speedup the startup time.
 * <p>JMeter needs component names to create menus, however, default GUIComponent implementations
 * create UI elements in their constructors. This class implements just the minimal subset of the
 * methods to make menu factory happy.</p>
 */
@API(since = "5.3", status = API.Status.INTERNAL)
public class StaticJMeterGUIComponent implements JMeterGUIComponent {
    private final String labelResource;
    private final ResourceBundle resourceBundle;
    private final Collection<String> groups;

    public StaticJMeterGUIComponent(Class<?> c, TestElementMetadata metadata) {
        this.labelResource = metadata.labelResource();
        String resourceBundle = metadata.resourceBundle();
        if (!resourceBundle.isEmpty()) {
            this.resourceBundle = ResourceBundle.getBundle(c.getName() + "Resources");
        } else if (labelResource.equals("displayName")) {
            this.resourceBundle = ResourceBundle.getBundle(c.getName() + "Resources");
        } else {
            this.resourceBundle = null;
        }
        this.groups = getGroups(c, metadata);
    }

    private static List<String> getGroups(Class<?> c, TestElementMetadata metadata) {
        String[] groups = metadata.actionGroups();
        if (groups.length == 1 && groups[0].equals("")) {
            // Annotations can't hold null values, so we use empty string instead
            return null;
        }
        if (groups.length != 0) {
            return Collections.unmodifiableList(Arrays.asList(groups));
        }
        String group;
        if (Assertion.class.isAssignableFrom(c) || AbstractAssertionGui.class.isAssignableFrom(c)) {
            group = MenuFactory.ASSERTIONS;
        } else if (ConfigElement.class.isAssignableFrom(c) || AbstractConfigGui.class.isAssignableFrom(c)) {
            group = MenuFactory.CONFIG_ELEMENTS;
        } else if (Controller.class.isAssignableFrom(c) || AbstractControllerGui.class.isAssignableFrom(c)) {
            group = MenuFactory.CONTROLLERS;
        } else if (Visualizer.class.isAssignableFrom(c) || AbstractListenerGui.class.isAssignableFrom(c)) {
            group = MenuFactory.LISTENERS;
        } else if (PostProcessor.class.isAssignableFrom(c) || AbstractPostProcessorGui.class.isAssignableFrom(c)) {
            group = MenuFactory.POST_PROCESSORS;
        } else if (PreProcessor.class.isAssignableFrom(c) || AbstractPreProcessorGui.class.isAssignableFrom(c)) {
            group = MenuFactory.PRE_PROCESSORS;
        } else if (Sampler.class.isAssignableFrom(c) || AbstractSamplerGui.class.isAssignableFrom(c)) {
            group = MenuFactory.SAMPLERS;
        } else if (Timer.class.isAssignableFrom(c) || AbstractTimerGui.class.isAssignableFrom(c)) {
            group = MenuFactory.TIMERS;
        } else if (ThreadGroup.class.isAssignableFrom(c) || AbstractThreadGroupGui.class.isAssignableFrom(c)) {
            group = MenuFactory.THREADS;
        } else {
            throw new IllegalArgumentException("Unknown group for class " + c);
        }
        return Collections.singletonList(group);
    }

    @Override
    public String getLabelResource() {
        return labelResource;
    }

    @Override
    public String getStaticLabel() {
        String labelResource = getLabelResource();
        if (resourceBundle == null) {
            return JMeterUtils.getResString(labelResource);
        }
        return resourceBundle.getString(labelResource);
    }

    @Override
    public Collection<String> getMenuCategories() {
        return groups;
    }

    // The rest throws UnsupportedOperationException since the methods are not intended to be used

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDocAnchor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestElement createTestElement() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void modifyTestElement(TestElement element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JPopupMenu createPopupMenu() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void configure(TestElement element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearGui() {
        throw new UnsupportedOperationException();
    }
}
