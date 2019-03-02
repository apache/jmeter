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

/**
 * This Exception is for use by functions etc to signal a Stop Test condition
 * where there is no access to the normal stop method
 * Stop test now means shutdown test waiting for current running samplers to end
 */
public class JMeterStopTestException extends RuntimeException {
    private static final long serialVersionUID = 240L;

    public JMeterStopTestException() {
        super();
    }

    public JMeterStopTestException(String s) {
        super(s);
    }
    
    public JMeterStopTestException(String s, Throwable ex) {
        super(s, ex);
    }
    
    public JMeterStopTestException(String s, Throwable ex, boolean enableSuppression, boolean writableStackTrace) {
        super(s, ex, enableSuppression, writableStackTrace);
    }
    
    public JMeterStopTestException(Throwable ex) {
        super(ex);
    }
}
