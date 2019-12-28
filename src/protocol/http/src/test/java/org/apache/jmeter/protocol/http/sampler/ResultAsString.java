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

package org.apache.jmeter.protocol.http.sampler;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.jmeter.samplers.SampleResult;

public class ResultAsString {
    private static final Comparator<SampleResult> ORDER =
            Comparator.comparing((SampleResult r) -> r.getURL().toString(), nullsFirst(naturalOrder()))
                    .thenComparing((SampleResult r) -> r.getResponseData().length, nullsFirst(naturalOrder()))
                    .thenComparing(SampleResult::getResponseDataAsString, nullsFirst(naturalOrder()));

    public static String toString(SampleResult result) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            toString(pw, 0, result);
        }
        return sw.toString();
    }

    private static void toString(PrintWriter pw, int indent, SampleResult result) {
        URL url = result.getURL();
        if (indent > 0) {
            indent(pw, indent - 2).append("- ");
        } else {
            indent(pw, indent);
        }
        if (url != null) {
            pw.append("url: ").println(url);
            indent(pw, indent);
        }
        pw.append("response: ").println(result.getResponseMessage());
        byte[] responseData = result.getResponseData();
        if (responseData != null) {
            indent(pw, indent).append("data.size: ").println(responseData.length);
            indent(pw, indent).append("data: ").println(result.getResponseDataAsString());
        }
        SampleResult[] subResults = result.getSubResults();
        Arrays.sort(subResults, ORDER);
        for (SampleResult subResult : subResults) {
            toString(pw, indent + 2, subResult);
        }
    }

    private static PrintWriter indent(PrintWriter pw, int indent) {
        for (int i = 0; i < indent; i++) {
            pw.append(" ");
        }
        return pw;
    }
}
