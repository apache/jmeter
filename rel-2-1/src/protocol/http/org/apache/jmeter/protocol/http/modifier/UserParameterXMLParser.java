// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.http.modifier;

import java.io.IOException;
import java.util.List;

import org.apache.jmeter.util.JMeterUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Parse an XML file to obtain parameter name and value information for all
 * users defined in the XML file.
 * 
 * @author Mark Walsh
 * @version $Revision$
 */
public class UserParameterXMLParser {
	// private String vendorParseClass = "org.apache.xerces.parsers.SAXParser";

	/**
	 * Parse all user parameter data defined in XML file.
	 * 
	 * @param xmlURI
	 *            name of the XML to load users parameter data
	 * @return all users name value pairs obtained from XML file
	 */
	public List getXMLParameters(String xmlURI) throws SAXException, IOException {
		// create instances needed for parsing
		XMLReader reader = JMeterUtils.getXMLParser();
		// XMLReaderFactory.createXMLReader(vendorParseClass);
		UserParameterXMLContentHandler threadParametersContentHandler = new UserParameterXMLContentHandler();
		UserParameterXMLErrorHandler parameterErrorHandler = new UserParameterXMLErrorHandler();

		// register content handler
		reader.setContentHandler(threadParametersContentHandler);

		// register error handler
		reader.setErrorHandler(parameterErrorHandler);

		// Request validation
		reader.setFeature("http://xml.org/sax/features/validation", true);

		// parse
		InputSource inputSource = new InputSource(xmlURI);
		reader.parse(inputSource);

		return threadParametersContentHandler.getParsedParameters();
	}
}
