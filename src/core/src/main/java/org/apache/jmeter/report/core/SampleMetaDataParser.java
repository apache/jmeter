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

package org.apache.jmeter.report.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple parser to get a {@link SampleMetadata} instance<br>
 *
 * @since 3.0
 */
public class SampleMetaDataParser {

    private final char separator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Pattern DELIMITER_PATTERN = Pattern
            // This assumes the header names are all single words with no spaces
            // word followed by 0 or more repeats of (non-word char + word)
            // where the non-word char (\2) is the same
            // e.g. abc|def|ghi but not abd|def~ghi
            .compile("\\w+((\\W)[\\w ]+)?(\\2[\\w ]+)*(\\2\"[\\w ]+\")*" // $NON-NLS-1$
                    // last entries may be quoted strings
            );
    private static final Pattern ALL_WORD_CHARS = Pattern.compile("^\\w+$");

    public SampleMetaDataParser(char separator) {
        this.separator = separator;
    }

    public SampleMetadata parse(String headRow) {
        char useSep = separator;
        if (headRow.indexOf(useSep) < 0 && !ALL_WORD_CHARS.matcher(headRow).matches()) {
            Matcher matcher = DELIMITER_PATTERN.matcher(headRow);
            if (matcher.matches()) {
                String guessedSep = matcher.group(2);
                if (guessedSep.length() != 1) {
                    throw new IllegalArgumentException(
                            "We guessed a delimiter of '"+guessedSep+"', but we support only one-character-separators");
                }
                useSep = guessedSep.charAt(0);
                logger.warn("Use guessed delimiter '{}' instead of configured '{}'. "
                        +"Please configure the property 'jmeter.save.saveservice.default_delimiter={}'",
                        useSep,
                        separator,
                        useSep);
            }
        }
        String[] cols = headRow.split(Pattern.quote(Character.toString(useSep)));
        return new SampleMetadata(useSep, cols);
    }
}
