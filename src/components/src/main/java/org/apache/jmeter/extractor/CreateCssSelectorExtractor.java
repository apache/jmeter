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

package org.apache.jmeter.extractor;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CreateCssSelectorExtractor {

    static final String CSS_SELECTOR_VARIABLE_NAME = "HtmlExtractor.refname";
    static final String CSS_SELECTOR_EXPRESSION = "HtmlExtractor.expr";
    static final String CSS_SELECTOR_ATTRIBUTE = "HtmlExtractor.attribute";
    static final String CSS_SELECTOR_MATCH_NO = "HtmlExtractor.match_number";

    static final String ATTRIBUTE_KEY_VALUE = "value";
    static final String ATTRIBUTE_KEY_CONTENT = "content";
    static final String CONTENT_TYPE = "contentType";
    static final String TEST_NAME = "testname";

    static final String ONE = "1";

    /**
     * @param html                    HTML response
     * @param attributeValue          Value of the parameter required to correlate
     * @param correlationVariableName alias of the correlated variable
     * @param requestUrl              URL of the request whose response yields the
     *                                parameter required to correlate
     * @param contentType             responseData content type
     * @return
     * @throws Error
     * @throws UnsupportedEncodingException
     */

    public static Map<String, String> createCssSelectorExtractor(String html, String attributeValue,
            String correlationVariableName, String requestUrl, String contentType) throws UnsupportedEncodingException {

        // parse the HTML string
        Document doc = getDocument(html);

        Map<String, String> cssSelectorExtractor = new HashMap<>();
        String cssSelectorExpression;
        String attribute;

        // decode the value string if encoded
        attributeValue = java.net.URLDecoder.decode(attributeValue, StandardCharsets.UTF_8.name());

        // get all elements with specified attibuteValue with keys: value and content
        // & return empty Map if the parameter isn't found in html
        Elements valuesForAttributeValue;
        Elements valuesForAttributeContent;
        Elements values;
        valuesForAttributeValue = doc.getElementsByAttributeValue(ATTRIBUTE_KEY_VALUE, attributeValue);
        valuesForAttributeContent = doc.getElementsByAttributeValue(ATTRIBUTE_KEY_CONTENT, attributeValue);
        if (valuesForAttributeValue.isEmpty()) {
            if (valuesForAttributeContent.isEmpty()) {
                // return empty cssSelectorExtractor
                return cssSelectorExtractor;
            } else {
                values = valuesForAttributeContent;
                attribute = ATTRIBUTE_KEY_CONTENT;
            }
        } else {
            values = valuesForAttributeValue;
            attribute = ATTRIBUTE_KEY_VALUE;
        }

        // get first occurrence of the element and extract its cssSelector
        cssSelectorExpression = values.first().cssSelector();

        // check if cssSelector is a ID selector and contains illegal characters
        // like (:) or (.)]
        // https://github.com/jhy/jsoup/issues/1055
        // https://github.com/jhy/jsoup/issues/30
        if ('#' == cssSelectorExpression.charAt(0)
                && (cssSelectorExpression.contains(":") || cssSelectorExpression.contains("."))) {
            // convert the selector to attribute type
            cssSelectorExpression = toAttributeSelector(cssSelectorExpression);
        }

        // check if created cssSelectorExtractor gives the correct value
        if (doc.select(cssSelectorExpression).attr(attribute).equals(attributeValue)) {
            cssSelectorExtractor.put(CSS_SELECTOR_VARIABLE_NAME, correlationVariableName);
            cssSelectorExtractor.put(CSS_SELECTOR_EXPRESSION, cssSelectorExpression);
            cssSelectorExtractor.put(CSS_SELECTOR_ATTRIBUTE, attribute);
            // Match No. = 1, as we are getting first occurrence of the element
            cssSelectorExtractor.put(CSS_SELECTOR_MATCH_NO, ONE);
            cssSelectorExtractor.put(CONTENT_TYPE, contentType);
            cssSelectorExtractor.put(TEST_NAME, requestUrl);
            return cssSelectorExtractor;
        } else {
            return cssSelectorExtractor;
        }
    }

    /**
     * @param html HTML response data
     * @return Jsoup Document object
     */
    public static Document getDocument(String html) {
        return Jsoup.parse(html);
    }

    /**
     * @param idSelector ID Selector e.g. #prodId
     * @return Attribute selector e.g [id=prodId]
     */
    public static String toAttributeSelector(String idSelector) {
        idSelector = idSelector.replace("#", "[id=");
        idSelector = idSelector.concat("]");
        return idSelector;
    }

}
