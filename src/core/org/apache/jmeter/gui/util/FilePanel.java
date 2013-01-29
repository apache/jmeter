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

package org.apache.jmeter.gui.util;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.util.JMeterUtils;

public class FilePanel extends FilePanelEntry {
    private static final long serialVersionUID = 240L;

    private final String title;

    public FilePanel() {
        this("", (String) null);
    }

    public FilePanel(String title) {
        this(title, (String) null);
    }

    public FilePanel(String title, String filetype) {
        super(JMeterUtils.getResString("file_visualizer_filename"), filetype); // $NON-NLS-1$
        this.title = title;
        init();
    }

    public FilePanel(ChangeListener l, String title) {
        super(JMeterUtils.getResString("file_visualizer_filename"), l); // $NON-NLS-1$
        this.title = title;
        init();        
    }

    public FilePanel(String resString, String[] exts) {
        super(JMeterUtils.getResString("file_visualizer_filename"), exts); // $NON-NLS-1$
        title = resString;
        init();
    }

    private void init() {
        setBorder(BorderFactory.createTitledBorder(title));
    }

}
