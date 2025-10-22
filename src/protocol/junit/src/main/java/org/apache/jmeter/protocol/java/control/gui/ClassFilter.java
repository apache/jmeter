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

package org.apache.jmeter.protocol.java.control.gui;

import java.util.ArrayList;
import java.util.List;


class ClassFilter {

    private String[] pkgs = new String[0];

    ClassFilter() {
        super();
    }

    void setPackges(String[] pk) {
        this.pkgs = pk;
    }

    private boolean include(String text) {
        if (pkgs.length == 0) { return true; } // i.e. no filter
        for (String pkg : pkgs) {
            if (text.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    String[] filterArray(List<String> items) {
        List<String> newList = new ArrayList<>();
        for (String item : items) {
            if (include(item)) {
                newList.add(item);
            }
        }
        if (!newList.isEmpty()) {
            return newList.toArray(new String[0]);
        } else {
            return new String[0];
        }
    }

    int size(){
        return pkgs.length;
    }
}
