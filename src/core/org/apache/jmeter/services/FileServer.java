/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * Created on Oct 19, 2004
 */
package org.apache.jmeter.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 *
 * The point of this class is to provide thread-safe access to files, and to
 * provide some simplifying assumptions about where to find files and how to
 * name them. For instance, putting supporting files in the same directory as
 * the saved test plan file allows users to refer to the file with just it's
 * name - this FileServer class will find the file without a problem.
 * Eventually, I want all in-test file access to be done through here, with the
 * goal of packaging up entire test plans as a directory structure that can be
 * sent via rmi to remote servers (currently, one must make sure the remote
 * server has all support files in a relative-same location) and to package up
 * test plans to execute on unknown boxes that only have Java installed.
 */
public class FileServer {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String DEFAULT_BASE = System.getProperty("user.dir");// $NON-NLS-1$

    /** Default base prefix */
    private static final String BASE_PREFIX_DEFAULT = "~/"; // $NON-NLS-1$

    private static final String BASE_PREFIX = 
        JMeterUtils.getPropDefault("jmeter.save.saveservice.base_prefix", // $NON-NLS-1$
                BASE_PREFIX_DEFAULT);

    //@GuardedBy("this")
    private File base;

    //@GuardedBy("this")
    private final Map<String, FileEntry> files = new HashMap<String, FileEntry>();

    private static final FileServer server = new FileServer();

    private final Random random = new Random();

    private FileServer() {
        base = new File(DEFAULT_BASE);
        log.info("Default base="+DEFAULT_BASE);
    }

    public static FileServer getFileServer() {
        return server;
    }

    public void resetBase() throws IOException{
        setBasedir(DEFAULT_BASE);
    }

    public synchronized void setBasedir(String basedir) throws IOException {
        if (filesOpen()) {
            throw new IOException("Files are still open, cannot change base directory");
        }
        files.clear();
        if (basedir != null) {
            base = new File(basedir);
            if (!base.isDirectory()) {
                base = base.getParentFile();
            }
            log.info("Set new base="+base);
        }
    }

    public synchronized String getBaseDir() {
        return base.getAbsolutePath();
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename - relative (to base) or absolute file name (must not be null)
     */
    public synchronized void reserveFile(String filename) {
        reserveFile(filename,null);
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename - relative (to base) or absolute file name (must not be null)
     * @param charsetName - the character set encoding to use for the file (may be null)
     */
    public synchronized void reserveFile(String filename, String charsetName) {
        reserveFile(filename, charsetName, filename, false);
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename - relative (to base) or absolute file name (must not be null)
     * @param charsetName - the character set encoding to use for the file (may be null)
     * @param alias - the name to be used to access the object (must not be null)
     */
    public synchronized void reserveFile(String filename, String charsetName, String alias) {
        reserveFile(filename, charsetName, alias, false);
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename - relative (to base) or absolute file name (must not be null)
     * @param charsetName - the character set encoding to use for the file (may be null)
     * @param alias - the name to be used to access the object (must not be null)
     * @param hasHeader true if the file has a header line describing the contents
     */
    public synchronized String reserveFile(String filename, String charsetName, String alias, boolean hasHeader) {
        if (filename == null){
            throw new IllegalArgumentException("Filename must not be null");
        }
        if (alias == null){
            throw new IllegalArgumentException("Alias must not be null");
        }
        FileEntry fileEntry = files.get(alias);
        if (fileEntry == null) {
            File f = new File(filename);
            fileEntry =
                new FileEntry(f.isAbsolute() ? f : new File(base, filename),null,charsetName);
            if (filename.equals(alias)){
                log.info("Stored: "+filename);
            } else {
                log.info("Stored: "+filename+" Alias: "+alias);
            }
            files.put(alias, fileEntry);
            if (hasHeader){
                try {
                    fileEntry.headerLine=readLine(alias, false);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Could not read file header line",e);
                }
            }
        }
        return fileEntry.headerLine;
    }

   /**
     * Get the next line of the named file, recycle by default.
     *
     * @param filename
     * @return String containing the next line in the file
     * @throws IOException
     */
    public String readLine(String filename) throws IOException {
      return readLine(filename, true);
    }

    /**
     * Get the next line of the named file, first line is name to false
     *
     * @param filename
     * @param recycle - should file be restarted at EOF?
     * @return String containing the next line in the file (null if EOF reached and not recycle)
     * @throws IOException
     */
    public String readLine(String filename, boolean recycle) throws IOException {
        return readLine(filename, recycle, false);
    }
   /**
     * Get the next line of the named file.
     *
     * @param filename
     * @param recycle - should file be restarted at EOF?
     * @param firstLineIsNames - 1st line is fields names
     * @return String containing the next line in the file (null if EOF reached and not recycle)
     * @throws IOException
     */
    public synchronized String readLine(String filename, boolean recycle, 
            boolean firstLineIsNames) throws IOException {
        FileEntry fileEntry = files.get(filename);
        if (fileEntry != null) {
            if (fileEntry.inputOutputObject == null) {
                fileEntry.inputOutputObject = createBufferedReader(fileEntry, filename);
            } else if (!(fileEntry.inputOutputObject instanceof Reader)) {
                throw new IOException("File " + filename + " already in use");
            }
            BufferedReader reader = (BufferedReader) fileEntry.inputOutputObject;
            String line = reader.readLine();
            if (line == null && recycle) {
                reader.close();
                reader = createBufferedReader(fileEntry, filename);
                fileEntry.inputOutputObject = reader;
                if (firstLineIsNames) {
                    // read first line and forget
                    reader.readLine();
                }
                line = reader.readLine();
            }
            if (log.isDebugEnabled()) { log.debug("Read:"+line); }
            return line;
        }
        throw new IOException("File never reserved: "+filename);
    }

    private BufferedReader createBufferedReader(FileEntry fileEntry, String filename) throws IOException {
        FileInputStream fis = new FileInputStream(fileEntry.file);
        InputStreamReader isr = null;
        // If file encoding is specified, read using that encoding, otherwise use default platform encoding
        String charsetName = fileEntry.charSetEncoding;
        if(charsetName != null && charsetName.trim().length() > 0) {
            isr = new InputStreamReader(fis, charsetName);
        } else {
            isr = new InputStreamReader(fis);
        }
        return new BufferedReader(isr);
    }

    public synchronized void write(String filename, String value) throws IOException {
        FileEntry fileEntry = files.get(filename);
        if (fileEntry != null) {
            if (fileEntry.inputOutputObject == null) {
                fileEntry.inputOutputObject = createBufferedWriter(fileEntry, filename);
            } else if (!(fileEntry.inputOutputObject instanceof Writer)) {
                throw new IOException("File " + filename + " already in use");
            }
            BufferedWriter writer = (BufferedWriter) fileEntry.inputOutputObject;
            if (log.isDebugEnabled()) { log.debug("Write:"+value); }
            writer.write(value);
        } else {
            throw new IOException("File never reserved: "+filename);
        }
    }

    private BufferedWriter createBufferedWriter(FileEntry fileEntry, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileEntry.file);
        OutputStreamWriter osw = null;
        // If file encoding is specified, write using that encoding, otherwise use default platform encoding
        String charsetName = fileEntry.charSetEncoding;
        if(charsetName != null && charsetName.trim().length() > 0) {
            osw = new OutputStreamWriter(fos, charsetName);
        } else {
            osw = new OutputStreamWriter(fos);
        }
        return new BufferedWriter(osw);
    }

    public synchronized void closeFiles() throws IOException {
        Iterator<Map.Entry<String, FileEntry>>  iter = files.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, FileEntry> me = iter.next();
            closeFile(me.getKey(),me.getValue() );
        }
        files.clear();
    }

    /**
     * @param name
     * @throws IOException
     */
    public synchronized void closeFile(String name) throws IOException {
        FileEntry fileEntry = files.get(name);
        closeFile(name, fileEntry);
    }

    private void closeFile(String name, FileEntry fileEntry) throws IOException {
        if (fileEntry != null && fileEntry.inputOutputObject != null) {
            log.info("Close: "+name);
            if (fileEntry.inputOutputObject instanceof Reader) {
                ((Reader) fileEntry.inputOutputObject).close();
            } else if (fileEntry.inputOutputObject instanceof Writer) {
                ((Writer) fileEntry.inputOutputObject).close();
            } else {
                log.error("Unknown inputOutputObject type : " + fileEntry.inputOutputObject.getClass());
            }
            fileEntry.inputOutputObject = null;
        }
    }

    boolean filesOpen() { // package access for test code only
        Iterator<FileEntry> iter = files.values().iterator();
        while (iter.hasNext()) {
            FileEntry fileEntry = iter.next();
            if (fileEntry.inputOutputObject != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method will get a random file in a base directory
     * TODO hey, not sure this method belongs here.  FileServer is for threadsafe
     * File access relative to current test's base directory.
     *
     * @param basedir
     * @return a random File from the basedir that matches one of the extensions
     */
    public File getRandomFile(String basedir, String[] extensions) {
        File input = null;
        if (basedir != null) {
            File src = new File(basedir);
            if (src.isDirectory() && src.list() != null) {
                File[] lfiles = src.listFiles(new JMeterFileFilter(extensions));
                int count = lfiles.length;
                input = lfiles[random.nextInt(count)];
            }
        }
        return input;
    }

    private static class FileEntry{
        private String headerLine;
        private final File file;
        private Object inputOutputObject; // Reader/Writer
        private final String charSetEncoding;
        FileEntry(File f, Object o, String e){
            file=f;
            inputOutputObject=o;
            charSetEncoding=e;
        }
    }
    
    /**
     * Resolve a file name that may be relative to the base directory.
     * If the name begins with the value of the JMeter property
     * "jmeter.save.saveservice.base_prefix" 
     * - default "~/" - then the name is assumed to be relative to the basename.
     * 
     * @param relativeName
     * @return the updated file
     */
    public static String resolveBaseRelativeName(String relativeName) {
        if (relativeName.startsWith(BASE_PREFIX)){
            String newName = relativeName.substring(BASE_PREFIX.length());
            return new File(getFileServer().getBaseDir(),newName).getAbsolutePath();
        }
        return relativeName;
    }
}
