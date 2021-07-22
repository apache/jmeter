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

package org.apache.jorphan.util;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator for {@link Map.Entry} Objects, that compares based on their keys only. The keys
 * will be compared in a human readable fashion by trying to parse numbers that appear in
 * the keys as integers and compare those, too.<p>
 * Heavily influenced by https://codereview.stackexchange.com/questions/37192/number-aware-string-sorting-with-comparator
 */
public class AlphaNumericKeyComparator implements Comparator<Map.Entry<Object, Object>> {

    public static final AlphaNumericKeyComparator INSTANCE = new AlphaNumericKeyComparator();
    private AlphaNumericComparator<Map.Entry<Object, Object>> comparator;

    private AlphaNumericKeyComparator() {
        // don't instantiate this class on your own.
        this.comparator = new AlphaNumericComparator<Map.Entry<Object, Object>>(e -> e.getKey().toString());
    }

    @Override
    public int compare(Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) {
        return this.comparator.compare(o1, o2);
    }

}
