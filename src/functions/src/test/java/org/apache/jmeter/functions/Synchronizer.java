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

package org.apache.jmeter.functions;

class Synchronizer {
    private Throwable failure;

    void done() {
        rethrow();
        notifyAll();
    }

    @SuppressWarnings("WaitNotInLoop")
    void pass() {
        done();
        try {
            wait(1000);
            rethrow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Did not receive 'continue' signal within 1sec");
        }
    }

    RuntimeException failure(Throwable t, String message) {
        t.addSuppressed(new RuntimeException(message));
        return failure(t);
    }

    RuntimeException failure(Throwable t) {
        failure = t;
        rethrow();
        return null;
    }

    private void rethrow() {
        Throwable failure = this.failure;
        if (failure == null) {
            return;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        throw new RuntimeException("wrap", failure) {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return this;
            }
        };
    }
}
