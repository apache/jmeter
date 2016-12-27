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

package org.apache.jmeter.gui;

/**
 * Interface for nodes that have replaceable content.
 * <p>
 * A {@link Replaceable} component will get asked for tokens, that should be used
 * in a search. These tokens will then be matched against a user given search
 * string.
 * @since 3.2
 */
public interface Replaceable {
    /**
     * Replace in object  by replaceBy
     *
     * @param regex Regular expression to search for
     * @param replaceBy Text used as replacement
     * @param caseSensitive flag, whether search should be done case sensitive
     * @return number of replacements
     * @throws Exception
     *             when something fails while replacing
     */
    int replace(String regex, String replaceBy, boolean caseSensitive)
        throws Exception;
}
