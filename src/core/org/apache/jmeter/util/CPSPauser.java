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

package org.apache.jmeter.util;

/**
 *
 * Generate appropriate pauses for a given CPS (characters per second)
 */
public class CPSPauser{
    private final int CPS; // Characters per second to emulate

    // Conversions for milli and nano seconds
    private static final int MS_PER_SEC = 1000;
    private static final int NS_PER_SEC = 1000000000;
    private static final int NS_PER_MS  = NS_PER_SEC/MS_PER_SEC;

    /**
     * Create a pauser with the appropriate speed settings.
     *
     * @param cps CPS to emulate
     */
    public CPSPauser(int cps){
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        CPS=cps;
    }

    /**
     * Pause for an appropriate time according to the number of bytes being transferred.
     *
     * @param bytes number of bytes being transferred
     */
    public void pause(int bytes){
        long sleepMS = (bytes*MS_PER_SEC)/CPS;
        int  sleepNS = ((bytes*MS_PER_SEC)/CPS) % NS_PER_MS;
        try {
            Thread.sleep(sleepMS,sleepNS);
        } catch (InterruptedException ignored) {
        }
    }
}