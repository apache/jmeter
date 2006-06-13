// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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
 */
package org.apache.jmeter.monitor.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.jmeter.monitor.model.ObjectFactory;
import org.apache.jmeter.monitor.model.Status;
import org.apache.jmeter.samplers.SampleResult;

public abstract class ParserImpl implements Parser {
	private SAXParserFactory PARSERFACTORY = null;

	private SAXParser PARSER = null;

	private MonitorHandler DOCHANDLER = null;

	private ObjectFactory FACTORY = null;

	/**
	 * 
	 */
	public ParserImpl(ObjectFactory factory) {
		super();
		this.FACTORY = factory;
		try {
			PARSERFACTORY = SAXParserFactory.newInstance();
			PARSER = PARSERFACTORY.newSAXParser();
			DOCHANDLER = new MonitorHandler();
			DOCHANDLER.setObjectFactory(this.FACTORY);
		} catch (SAXException e) {
			// e.printStackTrace();
			// need to add logging later
		} catch (ParserConfigurationException e) {
			// e.printStackTrace();
			// need to add logging later
		}
	}

	/**
	 * parse byte array and return Status object
	 * 
	 * @param bytes
	 * @return Status
	 */
	public Status parseBytes(byte[] bytes) {
		try {
			InputSource is = new InputSource();
			is.setByteStream(new ByteArrayInputStream(bytes));
			PARSER.parse(is, DOCHANDLER);
			return DOCHANDLER.getContents();
		} catch (SAXException e) {
			e.printStackTrace();
			// let bad input fail silently
			return DOCHANDLER.getContents();
		} catch (IOException e) {
			e.printStackTrace();
			// let bad input fail silently
			return DOCHANDLER.getContents();
		}
	}

	/**
	 * @param content
	 * @return Status
	 */
	public Status parseString(String content) {
		return parseBytes(content.getBytes());
	}

	/**
	 * @param result
	 * @return Status
	 */
	public Status parseSampleResult(SampleResult result) {
		return parseBytes(result.getResponseData());
	}

}
