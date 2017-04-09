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

package org.apache.jmeter.functions.util;

import org.apache.oro.text.perl.Perl5Util;

/**
 * Decodes an Argument by replacing '\x' with 'x'
 */
public final class ArgumentDecoder {
    private static final Perl5Util util = new Perl5Util();

    private static final String REGULAR_EXPRESSION = "s#[\\\\](.)#$1#g"; // $NON-NLS-1$

    // TODO does not appear to be used
    public static String decode(String s) {
        return util.substitute(REGULAR_EXPRESSION, s);
    }

    /**
     * Prevent instantiation of utility class.
     */
    private ArgumentDecoder() {
    }
}
