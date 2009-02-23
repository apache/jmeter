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

package org.apache.jmeter.config;

public interface ConfigElement extends Cloneable {

    /**
     * Add a configuration element to this one. This allows config elements to
     * combine and give a &quot;layered&quot; effect. For example,
     * HTTPConfigElements have properties for domain, path, method, and
     * parameters. If element A has everything filled in, but null for domain,
     * and element B is added, which has only domain filled in, then after
     * adding B to A, A will have the domain from B. If A already had a domain,
     * then the correct behavior is for A to ignore the addition of element B.
     *
     * @param config
     *            the element to be added to this ConfigElement
     */
    void addConfigElement(ConfigElement config);

    /**
     * If your config element expects to be modified in the process of a test
     * run, and you want those modifications to carry over from sample to sample
     * (as in a cookie manager - you want to save all cookies that get set
     * throughout the test), then return true for this method. Your config
     * element will not be cloned for each sample. If your config elements are
     * more static in nature, return false. If in doubt, return false.
     *
     * @return true if the element expects to be modified over the course of a
     *         test run
     */
    boolean expectsModification();

    Object clone();
}
