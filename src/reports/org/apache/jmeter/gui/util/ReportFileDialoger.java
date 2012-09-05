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
import javax.swing.filechooser.FileFilter;

import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.JMeterFileFilter;

public final class ReportFileDialoger {
    /**
     * The last directory visited by the user while choosing Files.
     */
    private static String lastJFCDirectory = null;

    private static JFileChooser jfc = new JFileChooser();

    /**
     * Prevent instantiation of utility class.
     */
    private ReportFileDialoger() {
    }

    /**
     * Prompts the user to choose a file from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it (accept or otherwise).
     */
    public static JFileChooser promptToOpenFile(String[] exts) {
        // JFileChooser jfc = null;

        if (lastJFCDirectory == null) {
            String start = System.getProperty("user.dir", "");

            if (!start.equals("")) {
                jfc.setCurrentDirectory(new File(start));
            }
        }
        clearFileFilters();
        jfc.addChoosableFileFilter(new JMeterFileFilter(exts));
        int retVal = jfc.showOpenDialog(ReportGuiPackage.getInstance().getMainFrame());
        lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();

        if (retVal == JFileChooser.APPROVE_OPTION) {
            return jfc;
        } else {
            return null;
        }
    }

    private static void clearFileFilters() {
        FileFilter[] filters = jfc.getChoosableFileFilters();
        for (int x = 0; x < filters.length; x++) {
            jfc.removeChoosableFileFilter(filters[x]);
        }
    }

    public static JFileChooser promptToOpenFile() {
        return promptToOpenFile(new String[0]);
    }

    /**
     * Prompts the user to choose a file from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it (accept or otherwise).
     * @see #promptToOpenFile()
     */
    public static JFileChooser promptToSaveFile(String filename) {
        return promptToSaveFile(filename, null);
    }

    /**
     * Get a JFileChooser with a new FileFilter.
     *
     * @param filename
     * @param extensions
     * @return JFileChooser
     */
    public static JFileChooser promptToSaveFile(String filename, String[] extensions) {
        if (lastJFCDirectory == null) {
            String start = System.getProperty("user.dir", "");
            if (!start.equals("")) {
                jfc = new JFileChooser(new File(start));
            }
            lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();
        }
        String ext = ".jmx";
        if (filename != null) {
            jfc.setSelectedFile(new File(lastJFCDirectory, filename));
            int i = -1;
            if ((i = filename.lastIndexOf('.')) > -1) {
                ext = filename.substring(i);
            }
        }
        clearFileFilters();
        if (extensions != null) {
            jfc.addChoosableFileFilter(new JMeterFileFilter(extensions));
        } else {
            jfc.addChoosableFileFilter(new JMeterFileFilter(new String[] { ext }));
        }

        int retVal = jfc.showSaveDialog(ReportGuiPackage.getInstance().getMainFrame());
        lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();
        if (retVal == JFileChooser.APPROVE_OPTION) {
            return jfc;
        } else {
            return null;
        }
    }
}
