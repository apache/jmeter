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

package org.apache.jmeter.util;

import java.lang.reflect.Constructor;
import java.security.Provider;
import java.security.Security;
import java.util.Comparator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityProviderLoader {
    private static final Logger log = LoggerFactory.getLogger(SecurityProviderLoader.class);

    public static void addSecurityProvider(Properties properties) {
        properties.keySet().stream()
                .filter(key -> key.toString().matches("security\\.provider(\\.\\d+)?"))
                .sorted(Comparator.comparing(String::valueOf)).forEach(key -> addSecurityProvider(properties.get(key).toString()));
    }

    public static void addSecurityProvider(String securityProviderConfig) {
        Pattern regex = Pattern.compile("^(?<classname>[^:]+)(:(?<position>\\d+)(:(?<config>.+))?)?$");
        Matcher matcher = regex.matcher(securityProviderConfig);

        if (matcher.matches()) {
            final String classname = matcher.group("classname");
            final Integer position = Integer.parseInt(StringUtils.defaultString(matcher.group("position"), "0"));
            final String config = matcher.group("config");

            try {
                Class providerClass = Class.forName(classname);

                Provider provider = null;

                if (config != null) {
                    try {
                        Constructor constructor = providerClass.getConstructor(String.class);
                        provider = (Provider) constructor.newInstance(config);
                    } catch (NoSuchMethodException e) {
                        log.warn("Security Provider {} has no constructor with a single String argument - try to use default constructor.", providerClass);
                    }
                }

                if (provider == null) {
                    provider = (Provider) providerClass.newInstance();
                }
                int installedPosition = Security.insertProviderAt(provider, position);

                log.info("Security Provider {} ({}) is installed at position {}", provider.getClass().getSimpleName(), provider.getName(), installedPosition);
            } catch (Exception exception) {
                String message = String.format("Security Provider '%s' could not be installed.", classname);
                log.error(message, exception);
                System.err.print(message);
                System.err.println(" - see the log for more information.");
            }
        }
    }
}