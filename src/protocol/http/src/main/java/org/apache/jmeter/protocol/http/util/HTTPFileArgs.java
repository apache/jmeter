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

package org.apache.jmeter.protocol.http.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * A set of HTTPFileArg objects.
 *
 */
public class HTTPFileArgs extends ConfigTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    /** The name of the property used to store the files. */
    private static final String HTTP_FILE_ARGS = "HTTPFileArgs.files"; //$NON-NLS-1$

    /**
     * Create a new HTTPFileArgs object with no files.
     */
    public HTTPFileArgs() {
        setProperty(new CollectionProperty(HTTP_FILE_ARGS, new ArrayList<HTTPFileArg>()));
    }

    /**
     * Get the files.
     *
     * @return the files
     */
    public CollectionProperty getHTTPFileArgsCollection() {
        return (CollectionProperty) getProperty(HTTP_FILE_ARGS);
    }

    /**
     * Clear the files.
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(HTTP_FILE_ARGS, new ArrayList<HTTPFileArg>()));
    }

    /**
     * Set the list of files. Any existing files will be lost.
     *
     * @param files the new files
     */
    public void setHTTPFileArgs(List<HTTPFileArg> files) {
        setProperty(new CollectionProperty(HTTP_FILE_ARGS, files));
    }

    /**
     * Add a new file with the given path.
     *
     * @param path
     *  the path of the file
     */
    public void addHTTPFileArg(String path) {
        addHTTPFileArg(new HTTPFileArg(path));
    }

    /**
     * Add a new file.
     *
     * @param file
     *  the new file
     */
    public void addHTTPFileArg(HTTPFileArg file) {
        TestElementProperty newHTTPFileArg = new TestElementProperty(file.getPath(), file);
        if (isRunningVersion()) {
            this.setTemporary(newHTTPFileArg);
        }
        getHTTPFileArgsCollection().addItem(newHTTPFileArg);
    }

    /**
     * adds a new File to the HTTPFileArgs list to be uploaded with http
     * request.
     *
     * @param path file full path.
     * @param param http parameter name.
     * @param mime mime type of file.
     */
    public void addHTTPFileArg(String path, String param, String mime) {
        addHTTPFileArg(new HTTPFileArg(path, param, mime));
    }

    /**
     * Get a PropertyIterator of the files.
     *
     * @return an iteration of the files
     */
    public PropertyIterator iterator() {
        return getHTTPFileArgsCollection().iterator();
    }

    /**
     * Get the current arguments as an array.
     *
     * @return an array of file arguments
     */
    public HTTPFileArg[] asArray(){
        CollectionProperty props = getHTTPFileArgsCollection();
        final int size = props.size();
        HTTPFileArg[] args = new HTTPFileArg[size];
        for(int i=0; i<size; i++){
            args[i]=(HTTPFileArg) props.get(i).getObjectValue();
        }
        return args;
    }
    /**
     * Create a string representation of the files.
     *
     * @return the string representation of the files
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        PropertyIterator iter = getHTTPFileArgsCollection().iterator();
        while (iter.hasNext()) {
            HTTPFileArg file = (HTTPFileArg) iter.next().getObjectValue();
            str.append(file.toString());
            if (iter.hasNext()) {
                str.append("\n"); //$NON-NLS-1$
            }
        }
        return str.toString();
    }

    /**
     * Remove the specified file from the list.
     *
     * @param row
     *  the index of the file to remove
     */
    public void removeHTTPFileArg(int row) {
        if (row < getHTTPFileArgCount()) {
            getHTTPFileArgsCollection().remove(row);
        }
    }

    /**
     * Remove the specified file from the list.
     *
     * @param file
     *  the file to remove
     */
    public void removeHTTPFileArg(HTTPFileArg file) {
        PropertyIterator iter = getHTTPFileArgsCollection().iterator();
        while (iter.hasNext()) {
            HTTPFileArg item = (HTTPFileArg) iter.next().getObjectValue();
            if (file.equals(item)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove the file with the specified path.
     *
     * @param filePath
     *  the path of the file to remove
     */
    public void removeHTTPFileArg(String filePath) {
        PropertyIterator iter = getHTTPFileArgsCollection().iterator();
        while (iter.hasNext()) {
            HTTPFileArg file = (HTTPFileArg) iter.next().getObjectValue();
            if (file.getPath().equals(filePath)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove all files from the list.
     */
    public void removeAllHTTPFileArgs() {
        getHTTPFileArgsCollection().clear();
    }

    /**
     * Add a new empty file to the list. The new file will have the
     * empty string as its path.
     */
    public void addEmptyHTTPFileArg() {
        addHTTPFileArg(new HTTPFileArg(""));
    }

    /**
     * Get the number of files in the list.
     *
     * @return the number of files
     */
    public int getHTTPFileArgCount() {
        return getHTTPFileArgsCollection().size();
    }

    /**
     * Get a single file.
     *
     * @param row
     *  the index of the file to return.
     * @return the file at the specified index, or null if no file
     *  exists at that index.
     */
    public HTTPFileArg getHTTPFileArg(int row) {
        HTTPFileArg file = null;
        if (row < getHTTPFileArgCount()) {
            file = (HTTPFileArg) getHTTPFileArgsCollection().get(row).getObjectValue();
        }
        return file;
    }
}
