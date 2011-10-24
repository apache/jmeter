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
        if (pkgs.length == 0) return true; // i.e. no filter
        boolean inc = false;
        for (int idx=0; idx < pkgs.length; idx++) {
            if (text.startsWith(pkgs[idx])){
                inc = true;
                break;
            }
        }
        return inc;
    }

    Object[] filterArray(List<String> items) {
        ArrayList<Object> newlist = new ArrayList<Object>();
        for (String item : items) {
            if (include(item)) {
                newlist.add(item);
            }
        }
        if (newlist.size() > 0) {
            return newlist.toArray();
        } else {
            return new Object[0];
        }
    }

    int size(){
        return pkgs.length;
    }
}
