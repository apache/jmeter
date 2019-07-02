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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to create a Thread Dump
 * @since 3.2
 */
public class ThreadDumper {

    // Only invoked by IODH class
    private ThreadDumper() {
        super();
    }

    /**
     * @return Name of file containing thread dump
     * @throws Exception if file cannot be written
     */
    public static String threadDump() throws Exception {
        return threadDump(new File(".")); //$NON-NLS-1$
    }

    /**
     * @param basedir {@link File} Base directory
     * @return Name of file containing thread dump
     * @throws Exception  if file cannot we written
     */
    public static String threadDump(File basedir) throws Exception {
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd_hhmmss_SSS");
        String stamp = timestampFormat.format(new Date());
        File temp = new File(basedir,"thread_dump_"+stamp+".log");
        final String path = temp.getPath();
        try (FileOutputStream fos = new FileOutputStream(temp);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            writeThreadDump(bufferedWriter);
        }
        return path;
    }

    /**
     * Write Thread Dump
     * @param writer {@link Writer}
     * @throws IOException if file cannot be written
     */
    public static void writeThreadDump(Writer writer) throws IOException {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        for (ThreadInfo ti : threadMxBean.dumpAllThreads(true, true)) {
            writer.write(ti.toString());
        }
    }
}
