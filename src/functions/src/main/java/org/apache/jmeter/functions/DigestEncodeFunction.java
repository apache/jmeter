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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Digest Encode Function that provides computing of different SHA-XXX, can
 * uppercase the result and store it in a variable.
 * Algorithm names can be specified using MessageDigest Algorithms names at
 * <a href=
 * "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html"
 * >StandardNames</a>
 *
 * @since 4.0
 */
@AutoService(Function.class)
public class DigestEncodeFunction extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(DigestEncodeFunction.class);

    /**
     * The algorithm names in this section can be specified when generating an
     * instance of MessageDigest: MD5 SHA-1 SHA-256 SHA-384 SHA-512
     */
    private static final List<String> desc = new ArrayList<>();
    private static final String KEY = "__digest";

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 2;
    private static final int MAX_PARAMETER_COUNT = 5;

    static {
        desc.add(JMeterUtils.getResString("algorithm_string"));
        desc.add(JMeterUtils.getResString("sha_string"));
        desc.add(JMeterUtils.getResString("salt_string"));
        desc.add(JMeterUtils.getResString("upper_case"));
        desc.add(JMeterUtils.getResString("function_name_paropt"));
    }

    private CompoundVariable[] values;

    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        String digestAlgorithm = values[0].execute();
        String stringToEncode = values[1].execute();
        String salt = values.length > 2 ? values[2].execute() : null;
        String encodedString = null;
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            md.update(stringToEncode.getBytes(StandardCharsets.UTF_8));
            if (StringUtilities.isNotEmpty(salt)) {
                md.update(salt.getBytes(StandardCharsets.UTF_8));
            }
            byte[] bytes = md.digest();
            HexFormat hexFormat = HexFormat.of();
            if (values.length > 3) {
                String shouldUpperCase = values[3].execute();
                if (Boolean.parseBoolean(shouldUpperCase)) {
                    hexFormat = hexFormat.withUpperCase();
                }
            }
            encodedString = hexFormat.formatHex(bytes);
            addVariableValue(encodedString, values, 4);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error calling {} function with value {}, digest algorithm {}, salt {}, ", KEY, stringToEncode,
                    digestAlgorithm, salt, e);
        }
        return encodedString;
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray(new CompoundVariable[parameters.size()]);
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
