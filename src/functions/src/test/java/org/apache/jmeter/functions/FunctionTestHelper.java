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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;

import org.apache.jmeter.engine.util.CompoundVariable;

public interface FunctionTestHelper {

    static Collection<CompoundVariable> makeParams(Iterable<Object> params) {
        Deque<CompoundVariable> res = new ArrayDeque<>();
        for (Object param : params) {
            res.add(new CompoundVariable(param == null ? null : String.valueOf(param)));
        }
        // Trim null values from the end of the list
        while (!res.isEmpty() && res.peekLast().getRawParameters() == null) {
            res.removeLast();
        }
        return res;
    }

    static Collection<CompoundVariable> makeParams(Object...params) {
        return makeParams(Arrays.asList(params));
    }
}
