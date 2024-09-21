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

package org.apache.jmeter.protocol.http.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.schema.PropertiesAccessor;
import org.apache.jmeter.testelement.schema.PropertyDescriptor;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Class representing a file parameter for http upload.
 * Consists of a http parameter name/file path pair with (optional) mimetype.
 *
 * Also provides temporary storage for the headers which are sent with files.
 *
 */
public class HTTPFileArg extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    /** temporary storage area for the body header. */
    private String header;

    private static final Tika tika = createTika();

    /**
     * Constructor for an empty HTTPFileArg object
     */
    public HTTPFileArg() {
    }

    /**
     * Constructor for the HTTPFileArg object with given path.
     *
     * @param path
     *            path to the file to use
     * @throws IllegalArgumentException
     *             if <code>path</code> is <code>null</code>
     */
    public HTTPFileArg(String path) {
        this(path, "", "");
    }

    /**
     * Constructor for the HTTPFileArg object with full information.
     *
     * @param path
     *            path of the file to use
     * @param paramname
     *            name of the http parameter to use for the file
     * @param mimetype
     *            mimetype of the file
     * @throws IllegalArgumentException
     *             if any parameter is <code>null</code>
     */
    public HTTPFileArg(String path, String paramname, String mimetype) {
        if (path == null || paramname == null || mimetype == null){
            throw new IllegalArgumentException("Parameters must not be null");
        }
        setPath(path);
        setParamName(paramname);
        setMimeType(detectMimeType(path, mimetype));
    }

    private static Tika createTika() {
        try {
            return new Tika(new TikaConfig(HTTPFileArg.class.getClassLoader()
                    .getResourceAsStream("org/apache/jmeter/protocol/http/gui/action/tika-config.xml")));
        } catch (TikaException | IOException | SAXException e) {
            return new Tika();
        }
    }

    private static String detectMimeType(String path, String mimetype) {
        if (StringUtils.isNotBlank(mimetype)) {
            return mimetype;
        }
        mimetype = Objects.toString(mimetype, "");
        if (StringUtils.isBlank(path)) {
            return mimetype;
        }
        File file = new File(path);
        if (file.canRead()) {
            try {
                return tika.detect(file);
            } catch (IOException e) {
                // do nothing, we will detect it later by name
            }
        }
        return tika.detect(path);
    }

    /**
     * Constructor for the HTTPFileArg object with full information,
     * using existing properties
     *
     * @param path
     *            path of the file to use
     * @param paramname
     *            name of the http parameter to use for the file
     * @param mimetype
     *            mimetype of the file
     * @throws IllegalArgumentException
     *             if any parameter is <code>null</code>
     */
    public HTTPFileArg(JMeterProperty path, JMeterProperty paramname, JMeterProperty mimetype) {
        if (path == null || paramname == null || mimetype == null){
            throw new IllegalArgumentException("Parameters must not be null");
        }
        setProperty(getSchema().getPath(), path);
        setProperty(getSchema().getMimeType(), mimetype);
        setProperty(getSchema().getParameterName(), paramname);
    }

    @Override
    public HTTPFileArgSchema getSchema() {
        return HTTPFileArgSchema.INSTANCE;
    }

    @Override
    public PropertiesAccessor<? extends HTTPFileArg, ? extends HTTPFileArgSchema> getProps() {
        return new PropertiesAccessor<>(this, getSchema());
    }

    private void setProperty(PropertyDescriptor<?, ?> descriptor, JMeterProperty prop) {
        JMeterProperty jmp = prop.clone();
        jmp.setName(descriptor.getName());
        setProperty(jmp);
    }

    /**
     * Copy Constructor.
     *
     * @param file
     *            {@link HTTPFileArg} to get information about the path, http
     *            parameter name and mimetype of the file
     * @throws IllegalArgumentException
     *             if any of those retrieved information is <code>null</code>
     */
    public HTTPFileArg(HTTPFileArg file) {
        this(file.getPath(), file.getParamName(), file.getMimeType());
    }

    /**
     * Set the http parameter name of the File.
     *
     * @param newParamName
     * the new http parameter name
     */
    public void setParamName(String newParamName) {
        set(getSchema().getParameterName(), newParamName);
    }

    /**
     * Get the http parameter name of the File.
     *
     * @return the http parameter name
     */
    public String getParamName() {
        return get(getSchema().getParameterName());
    }

    /**
     * Set the mimetype of the File.
     *
     * @param newMimeType
     * the new mimetype
     */
    public void setMimeType(String newMimeType) {
        set(getSchema().getMimeType(), newMimeType);
    }

    /**
     * Get the mimetype of the File.
     *
     * @return the http parameter mimetype
     */
    public String getMimeType() {
        return get(getSchema().getMimeType());
    }

    /**
     * Set the path of the File.
     *
     * @param newPath
     *  the new path
     */
    public void setPath(String newPath) {
        setMimeType(detectMimeType(newPath, getMimeType()));
        set(getSchema().getPath(), newPath);
    }

    /**
     * Get the path of the File.
     *
     * @return the file's path
     */
    public String getPath() {
        return get(getSchema().getPath());
    }

   /**
    * Sets the body header for the HTTPFileArg object. Header
    * contains path, parameter name and mime type information.
    * This is only intended for use by methods which need to store information
    * temporarily whilst creating the HTTP body.
    *
    * @param newHeader
    *  the new Header value
    */
   public void setHeader(String newHeader) {
       header = newHeader;
   }

   /**
    * Gets the saved body header for the HTTPFileArg object.
    *
    * @return saved body header
    */
   public String getHeader() {
       return header;
   }

    /**
     * returns path, param name, mime type information of
     * HTTPFileArg object.
     *
     * @return the string demonstration of HTTPFileArg object in this
     * format:
     *    "path:'&lt;PATH&gt;'|param:'&lt;PARAM NAME&gt;'|mimetype:'&lt;MIME TYPE&gt;'"
     */
    @Override
    public String toString() {
        return "path:'" + getPath()
            + "'|param:'" + getParamName()
            + "'|mimetype:'" + getMimeType() + "'";
    }

    /**
     * Check if the entry is not empty.
     * @return true if Path, name or mimetype fields are not the empty string
     */
    public boolean isNotEmpty() {
        return getPath().length() > 0
            || getParamName().length() > 0
            || getMimeType().length() > 0; // TODO should we allow mimetype only?
    }

}
