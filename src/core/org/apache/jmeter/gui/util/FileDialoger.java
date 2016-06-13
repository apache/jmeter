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

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterFileFilter;

/**
 * Class implementing a file open dialogue
 */
public final class FileDialoger {
    /**
     * The last directory visited by the user while choosing Files.
     */
    private static String lastJFCDirectory = null;

    private static JFileChooser jfc = new JFileChooser();

    /**
     * Prevent instantiation of utility class.
     */
    private FileDialoger() {
    }

    /**
     * Prompts the user to choose a file or a directory from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it - null if no file was chosen
     */
    public static JFileChooser promptToOpenFile() {
        return promptToOpenFile((String)null);
    }

    /**
     * Prompts the user to choose a file or a directory from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     * @param existingFileName The name of a file with path. If the filename points
     *             to an existing file, the directory in which it lies, will be used
     *             as the starting point for the returned JFileChooser.
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it - null if no file was chosen
     */
    public static JFileChooser promptToOpenFile(String existingFileName) {
        return promptToOpenFile(new String[0], existingFileName);
    }
    
    /**
     * Prompts the user to choose a file or a directory from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     * @param existingFileName The name of a file with path. If the filename points
     *             to an existing file, the directory in which it lies, will be used
     *             as the starting point for the returned JFileChooser.
     * @param onlyDirectories If true, only directories are displayed in the FileChooser
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it - null if no file was chosen
     */
    public static JFileChooser promptToOpenFile(String existingFileName, boolean onlyDirectories) {
        return promptToOpenFile(new String[0], existingFileName, onlyDirectories);
    }
    
    
    /**
     * Prompts the user to choose a file or a directory from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     * @param exts The list of allowed file extensions. If empty, any
     *             file extension is allowed
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it - null if no file was chosen
     */
    public static JFileChooser promptToOpenFile(String[] exts) {
        return promptToOpenFile(exts, null);
    }
    
    /**
     * Prompts the user to choose a file or a directory from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     * @param exts The list of allowed file extensions. If empty, any
     *             file extension is allowed
     * @param existingFileName The name of a file with path. If the filename points
     *             to an existing file, the directory in which it lies, will be used
     *             as the starting point for the returned JFileChooser.
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it - null if no file was chosen
     */
    public static JFileChooser promptToOpenFile(String[] exts, String existingFileName) {
        return promptToOpenFile(exts, null, false);
    }
  
    /**
    * Prompts the user to choose a file or a directory from their filesystems for our own
    * devious uses. This method maintains the last directory the user visited
    * before dismissing the dialog. This does NOT imply they actually chose a
    * file from that directory, only that they closed the dialog there. It is
    * the caller's responsibility to check to see if the selected file is
    * non-null.
    * @param exts The list of allowed file extensions. If empty, any
    *             file extension is allowed
    * @param existingFileName The name of a file with path. If the filename points
    *             to an existing file, the directory in which it lies, will be used
    *             as the starting point for the returned JFileChooser.
     * @param onlyDirectories If true, only directories are displayed in the FileChooser
    *
    * @return the JFileChooser that interacted with the user, after they are
    *         finished using it - null if no file was chosen
    */
   public static JFileChooser promptToOpenFile(String[] exts, String existingFileName, boolean onlyDirectories) {
       if (onlyDirectories) {
           jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
       } else {
           jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
       }
       if(!StringUtils.isEmpty(existingFileName)) {
           File existingFileStart = new File(existingFileName);
           if(existingFileStart.exists() && existingFileStart.canRead()) {
               jfc.setCurrentDirectory(new File(existingFileName));
           }
       }
       else if (lastJFCDirectory == null) {
           String start = System.getProperty("user.dir", ""); //$NON-NLS-1$//$NON-NLS-2$

           if (start.length() > 0) {
               jfc.setCurrentDirectory(new File(start));
           }
       }
       clearFileFilters();
       if(exts != null && exts.length > 0) {
           JMeterFileFilter currentFilter = new JMeterFileFilter(exts);
           jfc.addChoosableFileFilter(currentFilter);
           jfc.setAcceptAllFileFilterUsed(true);
           jfc.setFileFilter(currentFilter);
       }
       if(lastJFCDirectory==null) {
           lastJFCDirectory = System.getProperty("user.dir", ""); //$NON-NLS-1$//$NON-NLS-2$
       }
       jfc.setCurrentDirectory(new File(lastJFCDirectory));
       int retVal = jfc.showOpenDialog(GuiPackage.getInstance().getMainFrame());
       lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();

       if (retVal == JFileChooser.APPROVE_OPTION) {
           return jfc;
       }
       return null;
   }

    private static void clearFileFilters() {
        FileFilter[] filters = jfc.getChoosableFileFilters();
        for (FileFilter filter : filters) {
            jfc.removeChoosableFileFilter(filter);
        }
    }

    /**
     * Prompts the user to choose a file from their filesystems for our own
     * devious uses. This method maintains the last directory the user visited
     * before dismissing the dialog. This does NOT imply they actually chose a
     * file from that directory, only that they closed the dialog there. It is
     * the caller's responsibility to check to see if the selected file is
     * non-null.
     * @param filename  The name of a file with path. If the filename points
     *             to an existing file, the directory in which it lies, will be used
     *             as the starting point for the returned JFileChooser.
     *
     * @return the JFileChooser that interacted with the user, after they are
     *         finished using it - null if no file was chosen
     * @see #promptToOpenFile()
     */
    public static JFileChooser promptToSaveFile(String filename) {
        return promptToSaveFile(filename, null);
    }

    /**
     * Get a JFileChooser with a new FileFilter.
     *
     * @param filename file name
     * @param extensions list of extensions
     * @return the FileChooser - null if no file was chosen
     */
    public static JFileChooser promptToSaveFile(String filename, String[] extensions) {
        if (lastJFCDirectory == null) {
            String start = System.getProperty("user.dir", "");//$NON-NLS-1$//$NON-NLS-2$
            if (start.length() > 0) {
                jfc = new JFileChooser(new File(start));
            }
            lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();
        }
        String ext = ".jmx";//$NON-NLS-1$
        if (filename != null) {
            jfc.setDialogTitle(filename);
            jfc.setSelectedFile(filename.lastIndexOf(File.separator) > 0 ?
                    new File(filename) :
                    new File(lastJFCDirectory, filename));
            int i = -1;
            if ((i = filename.lastIndexOf('.')) > -1) {//$NON-NLS-1$
                ext = filename.substring(i);
            }
        }
        clearFileFilters();
        if (extensions != null) {
            jfc.addChoosableFileFilter(new JMeterFileFilter(extensions));
        } else {
            jfc.addChoosableFileFilter(new JMeterFileFilter(new String[] { ext }));
        }

        int retVal = jfc.showSaveDialog(GuiPackage.getInstance().getMainFrame());
        jfc.setDialogTitle(null);
        lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();
        if (retVal == JFileChooser.APPROVE_OPTION) {
            return jfc;
        }
        return null;
    }
    
    /**
     * 
     * @return The last directory visited by the user while choosing Files
     */
    public static String getLastJFCDirectory() {
        return lastJFCDirectory;
    }
    
    /**
     * 
     * @param lastJFCDirectory The last directory visited by the user while choosing Files
     */
    public static void setLastJFCDirectory(String lastJFCDirectory) {
        FileDialoger.lastJFCDirectory = lastJFCDirectory;
    }
}
