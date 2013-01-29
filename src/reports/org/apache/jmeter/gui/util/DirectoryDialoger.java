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

import java.io.File;

import javax.swing.JFileChooser;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.util.JMeterUtils;

public final class DirectoryDialoger {
    /**
     * The last directory visited by the user while choosing Files.
     */
    private static String lastJFCDirectory = null;

    private static final JFileChooser jfc = new JFileChooser();

    /**
     * Prevent instantiation of utility class.
     */
    private DirectoryDialoger() {
    }

    public static JFileChooser promptToOpenFile() {

        if (lastJFCDirectory == null) {
            String start = System.getProperty("user.dir", ""); // $NON-NLS-1$  // $NON-NLS-2$

            if (!start.equals("")) { // $NON-NLS-1$
                jfc.setCurrentDirectory(new File(start));
            }
        }
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retVal = jfc.showDialog(ReportGuiPackage.getInstance().getMainFrame(),
                JMeterUtils.getResString("report_select")); // $NON-NLS-1$
        lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();

        if (retVal == JFileChooser.APPROVE_OPTION) {
            return jfc;
        } else {
            return null;
        }
    }

}
