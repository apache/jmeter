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
 */
package org.apache.jmeter.monitor.model;

/**
 *
 * @version $Revision$
 */
public class ThreadInfoImpl implements ThreadInfo {
    private int maxSpareThreads = 0;

    private int minSpareThreads = 0;

    private int maxThreads = 0;

    private int currentThreadCount = 0;

    private int currentThreadsBusy = 0;

    /**
     *
     */
    public ThreadInfoImpl() {
        super();
    }

    @Override
    public int getMaxSpareThreads() {
        return this.maxSpareThreads;
    }

    @Override
    public void setMaxSpareThreads(int value) {
        this.maxSpareThreads = value;
    }

    @Override
    public int getMinSpareThreads() {
        return this.minSpareThreads;
    }

    @Override
    public void setMinSpareThreads(int value) {
        this.minSpareThreads = value;
    }

    @Override
    public int getMaxThreads() {
        return this.maxThreads;
    }

    @Override
    public void setMaxThreads(int value) {
        this.maxThreads = value;
    }

    @Override
    public int getCurrentThreadsBusy() {
        return this.currentThreadsBusy;
    }

    @Override
    public void setCurrentThreadsBusy(int value) {
        this.currentThreadsBusy = value;
    }

    @Override
    public int getCurrentThreadCount() {
        return this.currentThreadCount;
    }

    @Override
    public void setCurrentThreadCount(int value) {
        this.currentThreadCount = value;
    }

}
