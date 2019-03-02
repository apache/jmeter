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
package org.apache.jmeter.report.processor;

/**
 * A Job that can be used to execute an operation that produce a result that we
 * want to get in the future when it is ready.
 * 
 * 
 * @param <T>
 *            The result type
 * @since 3.0
 */
abstract class Job<T> implements Runnable {

    private volatile boolean resultReady = false;

    private final Object lock = new Object();

    private volatile T result;

    @Override
    public final void run() {
        resultReady = false;
        result = exec();
        synchronized (lock) {
            resultReady = true;
            lock.notify();
        }
    }

    protected abstract T exec();

    public T getResult() throws InterruptedException {
        synchronized (lock) {
            while (!resultReady) {
                lock.wait();
            }
        }
        return result;
    }
}
