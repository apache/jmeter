/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.protocol.http.util;

/**
 * This class provides an implementation of Base64 encoding without relying on
 * the the sun.* packages.
 *
 * @version    $Revision$
 */
public final class Base64Encoder
{
    private final static char[] pem_array = {
            65, 66, 67, 68, 69, 70, 71, 72, 73, 74,
            75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
            85, 86, 87, 88, 89, 90, 97, 98, 99, 100,
            101, 102, 103, 104, 105, 106, 107, 108,
            109, 110, 111, 112, 113, 114, 115, 116,
            117, 118, 119, 120, 121, 122, 48, 49, 50,
            51, 52, 53, 54, 55, 56, 57, 43, 47};
    private final static char eq = 61;

    /**
     * Private constructor to prevent instantiation.
     */
    private Base64Encoder()
    {
    }

    public final static String encode(String s)
    {
        return encode(s.getBytes());
    }

    public final static String encode(byte[] bs)
    {
        StringBuffer out = new StringBuffer("");
        int bl = bs.length;
        for (int i = 0; i < bl; i += 3)
        {
            out.append(encodeAtom(bs, i, (bl - i)));
        }
        return out.toString();
    }

    public final static String encodeAtom(byte[] b, int strt, int left)
    {
        StringBuffer out = new StringBuffer("");
        if (left == 1)
        {
            byte b1 = b[strt];
            int k = 0;
            out.append(String.valueOf(pem_array[b1 >>> 2 & 63]));
            out.append(
                String.valueOf(pem_array[(b1 << 4 & 48) + (k >>> 4 & 15)]));
            out.append(String.valueOf(eq));
            out.append(String.valueOf(eq));
            return out.toString();
        }
        if (left == 2)
        {
            byte b2 = b[strt];
            byte b4 = b[strt + 1];
            int l = 0;
            out.append(String.valueOf(pem_array[b2 >>> 2 & 63]));
            out.append(
                String.valueOf(pem_array[(b2 << 4 & 48) + (b4 >>> 4 & 15)]));
            out.append(
                String.valueOf(pem_array[(b4 << 2 & 60) + (l >>> 6 & 3)]));
            out.append(String.valueOf(eq));
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


