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

package org.apache.jorphan.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.jorphan.util.JOrphanUtils;

/**
 * Thread that eats Output and Error Stream to avoid Deadlock on Windows Machines
 * Inspired from:
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 */
class StreamGobbler extends Thread {
    private final InputStream is;
    private final StringBuilder buffer = new StringBuilder();
    /**
     * @param is {@link InputStream}
     */
    StreamGobbler(InputStream is) {
        this.is = is;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is)); // default charset
            String line = null;
            while ((line = br.readLine()) != null)
            {
                buffer.append(line);
                buffer.append("\r\n");
            }
        } catch (IOException e) {
            buffer.append(e.getMessage());
        }
        finally
        {
            JOrphanUtils.closeQuietly(br);
        }
    }

    /**
     * @return Output
     */
    public String getResult()
    {
        return buffer.toString();
    }
}
