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

import java.text.BreakIterator;

import org.apiguardian.api.API;

/**
 * Wraps text in such a way so the lines do not exceed given maximum length.
 */
@API(since = "5.5", status = API.Status.EXPERIMENTAL)
public class StringWrap {
    private final int minWrap;
    private final int maxWrap;

    private final BreakCursor wordCursor = new BreakCursor(BreakIterator.getLineInstance());
    private final BreakCursor charCursor = new BreakCursor(BreakIterator.getCharacterInstance());

    /**
     * Stores the current and the next position for a given {@link BreakIterator}.
     * It allows reducing the number of calls to {@link BreakIterator}.
     */
    private static class BreakCursor {
        private static final int UNINITIALIZED = -2;

        private final BreakIterator iterator;
        private int pos;
        private int next;

        BreakCursor(BreakIterator iterator) {
            this.iterator = iterator;
        }

        void setText(String text) {
            iterator.setText(text);
            pos = 0;
            next = UNINITIALIZED;
        }

        public int getPos() {
            return pos;
        }

        /**
         * Advances the cursor if possible.
         * @param startWrap the start index of the wrap to consider
         * @param endWrap the end index of the wrap to consider
         * @return true if the next break is detected within startWrap..endWrap boundaries
         */
        public boolean advance(int startWrap, int endWrap) {
            if (pos == BreakIterator.DONE || pos > endWrap) {
                return false;
            }
            pos = next != UNINITIALIZED ? next : iterator.following(startWrap);
            if (pos == BreakIterator.DONE || pos > endWrap) {
                return false;
            }
            // Try adding more items up to endWrap
            while (true) {
                next = iterator.next();
                if (next == BreakIterator.DONE || next > endWrap) {
                    break;
                }
                pos = next;
            }
            return true;
        }
    }

    /**
     * Creates string wrapper instance.
     *
     * @param minWrap minimal word length for the wrap
     * @param maxWrap maximum word length for the wrap
     */
    public StringWrap(int minWrap, int maxWrap) {
        this.minWrap = minWrap;
        this.maxWrap = maxWrap;
    }

    public int getMinWrap() {
        return minWrap;
    }

    public int getMaxWrap() {
        return maxWrap;
    }

    /**
     * Wraps given {@code input} text accoding to
     *
     * @param input     input text
     * @param delimiter delimiter when inserting soft wraps
     * @return modified text with added soft wraps, or input if wraps are not needed
     */
    public String wrap(String input, String delimiter) {
        if (input.length() <= minWrap) {
            return input;
        }
        wordCursor.setText(input);
        charCursor.setText(input);
        int pos = 0;
        StringBuilder sb = new StringBuilder(input.length() + input.length() / minWrap * delimiter.length());
        boolean hasChanges = false;
        int nextLineSeparator = BreakCursor.UNINITIALIZED;
        // Wrap long lines
        while (input.length() - pos > maxWrap) {
            if (nextLineSeparator != BreakIterator.DONE && nextLineSeparator < pos) {
                nextLineSeparator = input.indexOf('\n', pos);
            }
            // Try adding the next line if it does not exceed maxWrap
            int next = nextLineSeparator;
            if (next != -1 && pos - next <= maxWrap) {
                // The existing lines do not exceed maxWrap, just reuse them
                next++; // include newline
                sb.append(input, pos, next);
                pos = next;
                continue;
            }
            int startWrap = pos + minWrap - 1;
            int endWrap = pos + maxWrap;
            // Try breaking on word boundaries first
            if (wordCursor.advance(startWrap, endWrap)) {
                next = wordCursor.getPos();
            } else {
                // If char advances at least once, add it with the break even if it exceeds maxWrap
                // Note: single "char break" might consume multiple Java chars in case like emojis.
                charCursor.advance(startWrap, endWrap);
                next = charCursor.getPos();
                if (next == BreakIterator.DONE || next == input.length()) {
                    break;
                }
            }
            sb.append(input, pos, next);
            sb.append(delimiter);
            hasChanges = true;
            pos = next;
        }
        // Free up the memory
        wordCursor.setText("");
        charCursor.setText("");
        if (!hasChanges) {
            return input;
        }
        if (pos != input.length()) {
            sb.append(input, pos, input.length());
        }
        return sb.toString();
    }
}
