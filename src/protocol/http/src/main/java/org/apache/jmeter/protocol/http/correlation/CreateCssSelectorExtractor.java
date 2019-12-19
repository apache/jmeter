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

package org.apache.jmeter.protocol.http.correlation;

import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.protocol.http.correlation.extractordata.HtmlExtractorData;
import org.apache.jmeter.testelement.TestElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CreateCssSelectorExtractor {

    private static final String ATTRIBUTE_KEY_VALUE = "value"; //$NON-NLS-1$
    private static final String ATTRIBUTE_KEY_CONTENT = "content"; //$NON-NLS-1$
    private static final String ONE = "1"; //$NON-NLS-1$

    private CreateCssSelectorExtractor() {}

    /**
     * Create CSS Selector Extractor
     *
     * @param html                    HTML response
     * @param attributeValue          Value of the parameter required to correlate
     * @param correlationVariableName alias of the correlated variable
     * @param requestUrl              URL of the request whose response yields the
     *                                parameter required to correlate
     * @param contentType             responseData content type
     * @return HtmlExtractrData object or null
     */
    public static HtmlExtractorData createCssSelectorExtractor(String html, String attributeValue,
            String correlationVariableName, String requestUrl, String contentType) {
        // parse the HTML string
        Document doc = getDocument(html);
        HtmlExtractorData cssSelectorExtractor = null;
        String attribute = ""; //$NON-NLS-1$
        // get all elements with specified attibuteValue with keys: value and content
        // & return empty Map if the parameter isn't found in html
        Elements values;
        Elements valuesForAttributeValue = doc.getElementsByAttributeValue(ATTRIBUTE_KEY_VALUE, attributeValue);
        Elements valuesForAttributeContent = doc.getElementsByAttributeValue(ATTRIBUTE_KEY_CONTENT, attributeValue);
        if (valuesForAttributeValue.isEmpty()) {
            if (valuesForAttributeContent.isEmpty()) {
                // return null object
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
        String cssSelectorExpression = values.first().cssSelector();
        // check if cssSelector is a ID selector and contains illegal characters
        // like (:) or (.)]
        // https://github.com/jhy/jsoup/issues/1055
        // https://github.com/jhy/jsoup/issues/30
        if ('#' == cssSelectorExpression.charAt(0)
                && (cssSelectorExpression.contains(":") || cssSelectorExpression.contains("."))) { //$NON-NLS-1$ //$NON-NLS-2$
            // convert the selector to attribute type
            cssSelectorExpression = toAttributeSelector(cssSelectorExpression);
        }
        // check if created cssSelectorExtractor gives the correct value on evaluation
        if (doc.select(cssSelectorExpression).attr(attribute).equals(attributeValue)) {
            // Match No. = 1, as we are getting first occurrence of the element
            cssSelectorExtractor = new HtmlExtractorData(correlationVariableName, cssSelectorExpression, attribute, ONE,
                    contentType, requestUrl);
            return cssSelectorExtractor;
        } else {
            return cssSelectorExtractor;
        }
    }

    /**
     * Get Jsoup document by parsing HTML string
     *
     * @param html HTML response data
     * @return Jsoup Document object
     */
    public static Document getDocument(String html) {
        return Jsoup.parse(html);
    }

    /**
     * Converts ID selector to attribute selector
     *
     * @param idSelector ID Selector e.g. #prodId
     * @return Attribute selector e.g [id=prodId]
     */
    public static String toAttributeSelector(String idSelector) {
        idSelector = idSelector.replace("#", "[id="); //$NON-NLS-1$ //$NON-NLS-2$
        idSelector = idSelector.concat("]"); //$NON-NLS-1$
        return idSelector;
    }

    /**
     * Create CSS Selector extractor TestElement
     *
     * @param extractor HtmlExtractorData object
     * @param testElement empty testElement object
     * @return CSS selector extractor TestElement
     */
    public static TestElement createHtmlExtractorTestElement(HtmlExtractorData extractor, TestElement testElement) {
        HtmlExtractor htmlExtractor = (HtmlExtractor) testElement;
        htmlExtractor.setName(extractor.getRefname());
        htmlExtractor.setRefName(extractor.getRefname());
        htmlExtractor.setExpression(extractor.getExpr());
        htmlExtractor.setAttribute(extractor.getAttribute());
        htmlExtractor.setMatchNumber(extractor.getMatchNumber());
        return htmlExtractor;
    }

}
