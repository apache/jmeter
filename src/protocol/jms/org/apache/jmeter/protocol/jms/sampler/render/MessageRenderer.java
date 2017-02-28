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

package org.apache.jmeter.protocol.jms.sampler.render;

import com.github.benmanes.caffeine.cache.Cache;

public interface MessageRenderer<T> {

    /**
     * Convert text to expected type
     * 
     * @param text
     *            Text representing the type
     * @return the constructed object
     **/
    T getValueFromText(String text);

    /**
     * Read text from file, eventually replace variables, then convert it.
     * Cached content depends if variabilisation is active or not.
     * 
     * @param filename
     *            name of the file to get the value from
     * @param encoding
     *            encoding of the file
     * @param hasVariable
     *            flag, whether variables inside the value should be replaced
     * @param cache
     *            Cache in which the raw values will be stored/read from
     * @return the constructed object
     **/
    T getValueFromFile(String filename, String encoding, boolean hasVariable, Cache<Object,Object> cache);
}
