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

/**
 * Utility class to define the JMeter Version string
 *
 */
public class JMeterVersion {

    /*
     * Updated: 20110807 1823
     * The previous line is set to ${DSTAMP} ${TSTAMP}, to ensure that the file is modified
     * when the SVN tag is created so it has the same revision as the tag.
     * The SVN tag can be created as follows:
     * - ensure workspace is up-to-date with SVN
     * - touch this file by running "ant touch-version"
     * - create the tag from workspace: 
     *   "svn cp . https://svn.apache.org/repos/asf/jakarta/jmeter/tags/TAGNAME"
     * - Checkout the tag
     *
     * The REVISION string is checked by the Ant build file to ensure that the value
     * agrees with the tag revision
     */
    private static final String REVISION = "$Revision$"; // will be updated by SVN

    private static final String REVID;

    static {
        REVID = REVISION.split("\\s+")[1]; // extract the id
    }

    /*
     *
     * The string is made private so the compiler can't propagate it into
     * JMeterUtils. (Java compilers may make copies of final variables)
     *
     * This ensures that JMeterUtils always gets the correct
     * version, even if JMeterUtils is not re-compiled during the build.
     */
    private static final String VERSION = "2.5";

    static final String COPYRIGHT = "Copyright (c) 1998-2011 The Apache Software Foundation";

    private JMeterVersion() // Not instantiable
    {
        super();
    }

    static final String getVERSION() {
        return VERSION+" r"+REVID;
    }
}
