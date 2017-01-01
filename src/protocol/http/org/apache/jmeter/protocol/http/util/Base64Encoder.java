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

package org.apache.jmeter.protocol.http.util;

/**
 * This class provides an implementation of Base64 encoding without relying on
 * the sun.* packages.
 *
 * @deprecated as exists now in java.util.Base64, will be removed in next version 3.3
 */
@Deprecated
public final class Base64Encoder {
    private static final char[] pem_array = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82,
            83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
            112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };

    private static final char EQ = 61;

    /**
     * Private constructor to prevent instantiation.
     */
    private Base64Encoder() {
    }

    public static String encode(String s) {
        return encode(s.getBytes()); // TODO - charset?
    }

    public static String encode(byte[] bs) {
        StringBuilder out = new StringBuilder();
        int bl = bs.length;
        for (int i = 0; i < bl; i += 3) {
            out.append(encodeAtom(bs, i, bl - i));
        }
        return out.toString();
    }

    public static String encodeAtom(byte[] b, int strt, int left) {
        StringBuilder out = new StringBuilder();
        if (left == 1) {
            byte b1 = b[strt];
            int k = 0;
            out.append(String.valueOf(pem_array[b1 >>> 2 & 63]));
            out.append(String.valueOf(pem_array[(b1 << 4 & 48) + (k >>> 4 & 15)]));
            out.append(String.valueOf(EQ));
            out.append(String.valueOf(EQ));
            return out.toString();
        }
        if (left == 2) {
            byte b2 = b[strt];
            byte b4 = b[strt + 1];
            int l = 0;
            out.append(String.valueOf(pem_array[b2 >>> 2 & 63]));
            out.append(String.valueOf(pem_array[(b2 << 4 & 48) + (b4 >>> 4 & 15)]));
            out.append(String.valueOf(pem_array[(b4 << 2 & 60) + (l >>> 6 & 3)]));
            out.append(String.valueOf(EQ));
            return out.toString();
        }
        byte b3 = b[strt];
        byte b5 = b[strt + 1];
        byte b6 = b[strt + 2];
        out.append(String.valueOf(pem_array[b3 >>> 2 & 63]));
        out.append(String.valueOf(pem_array[(b3 << 4 & 48) + (b5 >>> 4 & 15)]));
        out.append(String.valueOf(pem_array[(b5 << 2 & 60) + (b6 >>> 6 & 3)]));
        out.append(String.valueOf(pem_array[b6 & 63]));
        return out.toString();
    }
}
