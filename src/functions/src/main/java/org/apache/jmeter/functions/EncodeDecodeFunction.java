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

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encode Decode Function
 * Supports Hex and Base64 methods
 *
 * @since 5.2
 */
public class EncodeDecodeFunction extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(EncodeDecodeFunction.class);

    /**
     * The algorithm names in this section can be specified when generating an
     * instance of MessageDigest: MD5 SHA-1 SHA-256 SHA-384 SHA-512
     */
    private static final List<String> desc = new LinkedList<>();
    private static final String KEY = "__encodeDecode";//NOSONAR
    private static final String BASE64_ENCODE = "BASE64_ENCODE";//NOSONAR
    private static final String BASE64_DECODE = "BASE64_DECODE";//NOSONAR
    private static final String HEX_ENCODE = "HEX_ENCODE";//NOSONAR
    private static final String HEX_DECODE = "HEX_DECODE";//NOSONAR

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 2;
    private static final int MAX_PARAMETER_COUNT = 3;

    static {
        desc.add(JMeterUtils.getResString("algorithm_string"));//NOSONAR
        desc.add(JMeterUtils.getResString("sha_string"));//NOSONAR
        desc.add(JMeterUtils.getResString("function_name_paropt"));//NOSONAR
    }

    private CompoundVariable[] values;
        
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        String digestAlgorithm = values[0].execute();
        String stringToEncode = values[1].execute();
        String encodedString = null;
        try {
            switch (digestAlgorithm) {
            case BASE64_ENCODE:
                encodedString = Base64.encodeBase64String(stringToEncode.getBytes(StandardCharsets.UTF_8.name()));
                break;
            case BASE64_DECODE:
                encodedString = new String(Base64.decodeBase64(stringToEncode), StandardCharsets.UTF_8.name());
                break;
            case HEX_DECODE:
                encodedString = new String(Hex.decodeHex(stringToEncode), StandardCharsets.UTF_8.name());
                break;
            case HEX_ENCODE:
                encodedString = Hex.encodeHexString(stringToEncode.getBytes(StandardCharsets.UTF_8.name()));
                break;
            default:
                throw new InvalidVariableException("Invalid algorithm, suppprt only: BASE64_ENCODE,BASE64_DECODE,HEX_ENCODE,HEX_DECODE");//NOSONAR
            }
            addVariableValue(encodedString, values, 2);
        } catch (Exception e) {
            log.error("Error calling {} function with value {}, digest algorithm {}, ", KEY, stringToEncode, digestAlgorithm, e);//NOSONAR
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
