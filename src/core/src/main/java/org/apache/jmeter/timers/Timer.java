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

package org.apache.jmeter.timers;

import java.io.Serializable;

/**
 * This interface defines those methods that must be implemented by timer
 * plugins.
 */
public interface Timer extends Serializable {
    /**
     * This method is called after a sampling process is done to know how much
     * time the sampling thread has to wait until sampling again.
     *
     * @return the computed delay value.
     */
    long delay();
    
    /**
     * @return true if factor can be applied to it
     */
    default boolean isModifiable() {
        return false;
    }
}
