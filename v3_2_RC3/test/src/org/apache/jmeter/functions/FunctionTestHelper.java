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

package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.util.CompoundVariable;

public class FunctionTestHelper {

    public static Collection<CompoundVariable> makeParams(String p1, String p2, String p3) {
        Collection<CompoundVariable> parms = new LinkedList<>();
        if (p1 != null) {
            parms.add(new CompoundVariable(p1));
        }
        if (p2 != null) {
            parms.add(new CompoundVariable(p2));
        }
        if (p3 != null) {
            parms.add(new CompoundVariable(p3));
        }
        return parms;
    }
}
