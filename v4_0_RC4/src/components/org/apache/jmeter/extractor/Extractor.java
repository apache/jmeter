/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.extractor;

import java.io.Serializable;
import java.util.List;

/**
 * CSS/JQuery based extractor for HTML pages
 * @since 2.9
 */
public interface Extractor extends Serializable {
    /**
     * 
     * @param expression Expression used for extraction of nodes
     * @param attribute Attribute name to return 
     * @param matchNumber Match number
     * @param inputString Page or excerpt
     * @param result List of results
     * @param found current matches found
     * @param cacheKey If not null, the implementation is encouraged to cache parsing result and use this key as part of cache key
     * @return match found updated
     */
    int extract(
            String expression,
            String attribute,
            int matchNumber, 
            String inputString, 
            List<String> result,
            int found,
            String cacheKey);
}
