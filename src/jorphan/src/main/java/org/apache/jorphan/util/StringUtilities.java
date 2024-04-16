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

import org.apiguardian.api.API;

/**
 * Functions that are missing in other libraries.
 *
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class StringUtilities {
    private StringUtilities() {
    }

    /**
     * Counts the number of times a given char is present in the string.
     *
     * @param input input string
     * @param ch    char to search
     * @return number of times the character is present in the string, or 0 if no char found
     */
    public static int count(String input, char ch) {
        if (input.isEmpty()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = input.indexOf(ch, idx)) != -1) {
            count++;
            idx++;
        }
        return count;
    }
}
