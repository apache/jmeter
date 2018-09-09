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

/*
 * Created on 02-Oct-2003
 *
 * This class defines the JMeter version only (moved from JMeterUtils)
 *
 * Version changes no longer change the JMeterUtils source file
 * - easier to spot when JMeterUtils really changes
 * - much smaller to download when the version changes
 *
 */
package org.apache.jmeter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * Utility class to define the JMeter Version string
 *
 */
public final class JMeterVersion {

    /*
     *
     * The string is made private so the compiler can't propagate it into
     * JMeterUtils. (Java compilers may make copies of final variables)
     *
     * This ensures that JMeterUtils always gets the correct
     * version, even if JMeterUtils is not re-compiled during the build.
     */
    private static final String VERSION = "5.0";

    private static final String IMPLEMENTATION;

    // Same applies to copyright string
    private static final String COPYRIGHT = "Copyright (c) 1998-2018 The Apache Software Foundation";

    static {
        String impl=null;
        final Class<?> myClass = JMeterVersion.class;
        // This assumes that the JVM treats a class file as a resource (not all do).
        URL resource = myClass.getResource("JMeterVersion.class");
        // For example:
        // jar:file:/JMeter/lib/ext/ApacheJMeter_core.jar!/org/apache/jmeter/util/JMeterVersion.class
        // or if using an IDE        
        // file:/workspaces/JMeter/build/core/org/apache/jmeter/util/JMeterVersion.class


        try {
            // Convert to URL for manifest
            String url = resource.toString().replaceFirst("!/.+", "!/META-INF/MANIFEST.MF");
            resource=new URL(url);
            InputStream inputStream = resource.openStream();
            if (inputStream != null) {
                Properties props = new Properties();
                try {
                    props.load(inputStream);
                    impl = props.getProperty("Implementation-Version");
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        } catch (IOException ioe) {
            // Ignored
        }
        if (impl == null) {
            IMPLEMENTATION = VERSION; // default to plain version
        } else {
            IMPLEMENTATION = impl;
        }
    }

    private JMeterVersion() // Not instantiable
    {
        super();
    }

    static String getVERSION() {
        return IMPLEMENTATION;
    }

    public static String getCopyRight() {
        return COPYRIGHT;
    }
}
