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

package org.apache.jorphan.gui.ui;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

/**
 * Enables to create Swing UI components for a given UI name.
 * <p>By default, Swing uses {@link JComponent#getUIClassID()}, however,
 * this class enables to create UI for components with arbitrary {@code UIClassID}.</p>
 * <p>Note: the assumption is that actual {@code ...UI#createUI(JComponent c)} do not use
 * {@code c} argument.</p>
 */
class UIFactory {
    private static class JCustomComponent extends JComponent {
        private final String uiClassId;

        private JCustomComponent(String uiClassId) {
            this.uiClassId = uiClassId;
        }

        @Override
        public String getUIClassID() {
            return uiClassId;
        }
    }

    /**
     * Creates a {@code Swing UI} for a given {@code uiClassId}
     * @param uiClassId the id of the UI
     * @return Swing UI
     */
    static ComponentUI create(String uiClassId) {
        return UIManager.getUI(new JCustomComponent(uiClassId));
    }
}
