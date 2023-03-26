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

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.auto.service.AutoService;

// See org.apache.jmeter.functions.TestTimeFunction for unit tests

/**
 * __time() function - returns the current time in milliseconds
 * @since 2.2
 */
@AutoService(Function.class)
public class TimeFunction extends AbstractFunction implements TestStateListener {

    private static final String KEY = "__time"; // $NON-NLS-1$

    private static final Pattern DIVISOR_PATTERN = Pattern.compile("/\\d+");

    private static final List<String> desc = new ArrayList<>();

    // Only modified in class init
    private static final Map<String, String> aliases = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(TimeFunction.class);

    private static final LoadingCache<String, Supplier<String>> DATE_TIME_FORMATTER_CACHE =
            Caffeine.newBuilder()
                    .maximumSize(1000)
                    .build((fmt) -> {
                        if (DIVISOR_PATTERN.matcher(fmt).matches()) {
                            long div = Long.parseLong(fmt.substring(1)); // should never case NFE
                            return () -> Long.toString(System.currentTimeMillis() / div);
                        }
                        DateTimeFormatter df = DateTimeFormatter
                                .ofPattern(fmt)
                                .withZone(ZoneId.systemDefault());
                        if (isPossibleUsageOfUInFormat(df, fmt)) {
                            log.warn(
                                    MessageFormat.format(
                                            JMeterUtils.getResString("time_format_changed"),
                                            fmt));
                        }
                        return () -> df.format(Instant.now());
                    });

    private static boolean isPossibleUsageOfUInFormat(DateTimeFormatter df, String fmt) {
        ZoneId mst = ZoneId.of("-07:00");
        return fmt.contains("u") &&
                df.withZone(mst)
                        .format(ZonedDateTime.of(2006, 1, 2, 15, 4, 5, 6, mst))
                        .contains("2006");
    }

    static {
        desc.add(JMeterUtils.getResString("time_format")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
        aliases.put("YMD", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.YMD", //$NON-NLS-1$
                        "yyyyMMdd")); //$NON-NLS-1$
        aliases.put("HMS", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.HMS", //$NON-NLS-1$
                        "HHmmss")); //$NON-NLS-1$
        aliases.put("YMDHMS", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.YMDHMS", //$NON-NLS-1$
                        "yyyyMMdd-HHmmss")); //$NON-NLS-1$
        aliases.put("USER1", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.USER1","")); //$NON-NLS-1$
        aliases.put("USER2", //$NON-NLS-1$
                JMeterUtils.getPropDefault("time.USER2","")); //$NON-NLS-1$
    }

    // Ensure that these are set, even if no parameters are provided
    private String format   = ""; //$NON-NLS-1$
    private String variable = ""; //$NON-NLS-1$

    public TimeFunction(){
        super();
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        String datetime;
        if (format.isEmpty()) {// Default to milliseconds
            datetime = Long.toString(System.currentTimeMillis());
        } else {
            // Resolve any aliases
            String fmt = aliases.get(format);
            if (fmt == null) {
                fmt = format;// Not found
            }
            datetime = DATE_TIME_FORMATTER_CACHE.get(fmt).get();
        }

        if (!variable.isEmpty()) {
            JMeterVariables vars = getVariables();
            if (vars != null){// vars will be null on TestPlan
                vars.put(variable, datetime);
            }
        }
        return datetime;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {

        checkParameterCount(parameters, 0, 2);

        Object []values = parameters.toArray();
        int count = values.length;

        if (count > 0) {
            format = ((CompoundVariable) values[0]).execute();
        }

        if (count > 1) {
            variable = ((CompoundVariable)values[1]).execute().trim();
        }

    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    @Override
    public void testStarted() {
        // We invalidate the cache so it will parse the formats again and will raise a warning if there are
        // %u usages.
        DATE_TIME_FORMATTER_CACHE.invalidateAll();
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        DATE_TIME_FORMATTER_CACHE.invalidateAll();
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }
}
