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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.reporters.FileReporter;
import org.apache.jmeter.util.JMeterUtils;

public class Analyze implements Command {
    private static final Set<String> commands = new HashSet<String>();

    static {
        commands.add(ActionNames.ANALYZE_FILE);
    }

    public Analyze() {
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        FileReporter analyzer = new FileReporter();
        final JFileChooser chooser = FileDialoger.promptToOpenFile(new String[] { ".jtl" }); //$NON-NLS-1$
        if (chooser != null) {
            try {
                analyzer.init(chooser.getSelectedFile().getPath());
            } catch (IOException err) {
                JMeterUtils.reportErrorToUser("The file you selected could not be analyzed");
            }
        }
    }
}
