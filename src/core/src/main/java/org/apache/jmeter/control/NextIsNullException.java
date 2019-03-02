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
 * Created on Apr 30, 2003
 */
package org.apache.jmeter.control;

/**
 * Used by the Generic and Interleave controllers to signal the end of their samples
 */
public class NextIsNullException extends Exception {
    private static final long serialVersionUID = 240L;

    public NextIsNullException() {
        super();
    }

    public NextIsNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public NextIsNullException(String message) {
        super(message);
    }

    public NextIsNullException(Throwable cause) {
        super(cause);
    }

}
