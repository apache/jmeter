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

package org.apache.jorphan.util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;

/**
 * Class allowing access to Sun's heapDump method (Java 1.6+).
 * Uses Reflection so that the code compiles on Java 1.5.
 * The code will only work on Sun Java 1.6+.
 */
public class HeapDumper {

    // SingletonHolder idiom for lazy initialisation
    private static class DumperHolder {
        private static final HeapDumper DUMPER = new HeapDumper();
    }

    private static HeapDumper getInstance(){
        return DumperHolder.DUMPER;
    }

    // This is the name of the HotSpot Diagnostic platform MBean (Sun Java 1.6)
    // See: http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/HotSpotDiagnosticMXBean.html
    private static final String HOTSPOT_BEAN_NAME =
         "com.sun.management:type=HotSpotDiagnostic";

    // These are needed for invoking the method
    private final MBeanServer server;
    private final ObjectName hotspotDiagnosticBean;

    // If we could not find the method, store the exception here
    private final Exception exception;

    // Only invoked by IODH class
    private HeapDumper() {
        server = ManagementFactory.getPlatformMBeanServer(); // get the platform beans
        ObjectName on = null;
        Exception ex = null;
        try {
            on = new ObjectName(HOTSPOT_BEAN_NAME); // should never fail
            server.getObjectInstance(on); // See if we can actually find the object
        } catch (MalformedObjectNameException e) { // Should never happen
            throw new AssertionError("Could not establish the HotSpotDiagnostic Bean Name: "+e);
        } catch (InstanceNotFoundException e) {
            ex = e;
            on = null; // Prevent useless dump attempts
        }
        exception = ex;
        hotspotDiagnosticBean = on;
    }

    /**
     * Initialise the dumper, and report if there is a problem.
     * This is optional, as the dump methods will initialise if necessary.
     *
     * @throws Exception if there is a problem finding the heapDump MXBean
     */
    public static void init() throws Exception {
        Exception e =getInstance().exception;
        if (e != null) {
            throw e;
        }
    }

    /**
     * Dumps the heap to the outputFile file in the same format as the hprof heap dump.
     * <p>
     * Calls the dumpHeap() method of the HotSpotDiagnostic MXBean, if available.
     * <p>
     * See
     * <a href="http://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/HotSpotDiagnosticMXBean.html">
     * HotSpotDiagnosticMXBean
     * </a>
     * @param fileName name of the heap dump file. Must be creatable, i.e. must not exist.
     * @param live if true, dump only the live objects
     * @throws Exception if the MXBean cannot be found, or if there is a problem during invocation
     */
    public static void dumpHeap(String fileName, boolean live) throws Exception{
        getInstance().dumpHeap0(fileName, live);
    }

    /**
     * Dumps live objects from the heap to the outputFile file in the same format as the hprof heap dump.
     * <p>
     * @see #dumpHeap(String, boolean)
     * @param fileName name of the heap dump file. Must be creatable, i.e. must not exist.
     * @throws Exception if the MXBean cannot be found, or if there is a problem during invocation
     */
    public static void dumpHeap(String fileName) throws Exception{
        dumpHeap(fileName, true);
    }

    /**
     * Dumps live objects from the heap to the outputFile file in the same format as the hprof heap dump.
     * <p>
     * Creates the dump using the file name: dump_yyyyMMdd_hhmmss_SSS.hprof
     * The dump is created in the current directory.
     * <p>
     * @see #dumpHeap(boolean)
     * @return the name of the dump file that was created
     * @throws Exception if the MXBean cannot be found, or if there is a problem during invocation
     */
    public static String dumpHeap() throws Exception{
        return dumpHeap(true);
    }

    /**
     * Dumps objects from the heap to the outputFile file in the same format as the hprof heap dump.
     * <p>
     * Creates the dump using the file name: dump_yyyyMMdd_hhmmss_SSS.hprof
     * The dump is created in the current directory.
     * <p>
     * @see #dumpHeap(String, boolean)
     * @param live true id only live objects are to be dumped.
     *
     * @return the name of the dump file that was created
     * @throws Exception if the MXBean cannot be found, or if there is a problem during invocation
     */
    public static String dumpHeap(boolean live) throws Exception {
        return dumpHeap(new File("."), live);
    }

    /**
     * Dumps objects from the heap to the outputFile file in the same format as the hprof heap dump.
     * The dump is created in the specified directory.
     * <p>
     * Creates the dump using the file name: dump_yyyyMMdd_hhmmss_SSS.hprof
     * <p>
     * @see #dumpHeap(String, boolean)
     * @param basedir File object for the target base directory.
     * @param live true id only live objects are to be dumped.
     *
     * @return the name of the dump file that was created
     * @throws Exception if the MXBean cannot be found, or if there is a problem during invocation
     */
    public static String dumpHeap(File basedir, boolean live) throws Exception {
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd_hhmmss_SSS");
        String stamp = timestampFormat.format(new Date());
        File temp = new File(basedir,"dump_"+stamp+".hprof");
        final String path = temp.getPath();
        dumpHeap(path, live);
        return path;
    }

    /**
     * Perform the dump using the dumpHeap method.
     *
     * @param fileName the file to use
     * @param live true to dump only live objects
     * @throws Exception if the MXBean cannot be found, or if there is a problem during invocation
     */
    private void dumpHeap0(String fileName, boolean live) throws Exception {
        try {
            if (exception == null) {
                server.invoke(hotspotDiagnosticBean,
                        "dumpHeap",
                        new Object[]{fileName, Boolean.valueOf(live)},
                        new String[]{"java.lang.String", "boolean"});
            } else {
                throw exception;
            }
        } catch (RuntimeMBeanException e) {
            Throwable f = e.getCause();
            if (f instanceof Exception){
                throw (Exception )f;
            }
            throw e;
        } catch (MBeanException e) {
            Throwable f = e.getCause();
            if (f instanceof Exception){
                throw (Exception )f;
            }
            throw e;
        }
    }
}

