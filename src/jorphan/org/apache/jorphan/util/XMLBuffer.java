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

package org.apache.jorphan.util;

import org.apache.commons.collections.ArrayStack;

// @see org.apache.jorphan.util.TestXMLBuffer for unit tests

/**
 * Provides XML string building methods.
 * Not synchronised.
 *
 */
public class XMLBuffer{
    private final StringBuilder sb = new StringBuilder(); // the string so far

    private final ArrayStack tags = new ArrayStack(); // opened tags

    public XMLBuffer() {

    }

    private void startTag(String t) {
        sb.append("<");
        sb.append(t);
        sb.append(">");
    }

    private void endTag(String t) {
        sb.append("</");
        sb.append(t);
        sb.append(">");
        sb.append("\n");
    }

    private void emptyTag(String t) {
        sb.append("<");
        sb.append(t);
        sb.append("/>");
        sb.append("\n");
    }

    /**
     * Open a tag; save on stack.
     *
     * @param tagName name of the tag
     * @return this
     */
    public XMLBuffer openTag(String tagName) {
        tags.push(tagName);
        startTag(tagName);
        return this;
    }

    /**
     * Close top tag from stack.
     *
     * @param tagName name of the tag to close
     *
     * @return this
     *
     * @throws IllegalArgumentException if the tag names do not match
     */
    public XMLBuffer closeTag(String tagName) {
        String tag = (String) tags.pop();
        if (!tag.equals(tagName)) {
            throw new IllegalArgumentException(
                    "Trying to close tag: " + tagName + " ; should be " + tag);
        }
        endTag(tag);
        return this;
    }

    /**
     * Add a complete tag with content.
     *
     * @param tagName name of the tag
     * @param content content to put in tag, or empty content, if an empty tag should be used
     * @return this
     */
    public XMLBuffer tag(String tagName, CharSequence content) {
        if (content.length() == 0) {
            emptyTag(tagName);
        } else {
            startTag(tagName);
            sb.append(content);
            endTag(tagName);
        }
        return this;
    }

    /**
     * Convert the buffer to a string, closing any open tags
     */
    @Override
    public String toString() {
        while (!tags.isEmpty()) {
            endTag((String) tags.pop());
        }
        return sb.toString();
    }
}
