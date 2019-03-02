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

package org.apache.jmeter.processor.gui;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jmeter.gui.AbstractScopedJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;

/**
 * This is the base class for JMeter GUI components which manage PostProcessors.
 * 
 * PostProcessors which can be applied to different scopes (parent, children or both)
 * need to use the createScopePanel() to add the panel to the GUI, and they also
 * need to use saveScopeSettings() and showScopeSettings() to keep the test element
 * and GUI in synch.
 *
 */
public abstract class AbstractPostProcessorGui extends AbstractScopedJMeterGuiComponent {

    private static final long serialVersionUID = 240L;

    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.POST_PROCESSORS);
    }

}
