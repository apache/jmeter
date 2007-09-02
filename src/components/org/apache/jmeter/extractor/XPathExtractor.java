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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterError;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//@see org.apache.jmeter.extractor.TestXPathExtractor for unit tests

/**
 * Extracts text from (X)HTML response using XPath query language
 * Example XPath queries:
 * <dl> 
 * <dt>/html/head/title</dt>
 *     <dd>extracts Title from HTML response</dd>
 * <dt>//form[@name='countryForm']//select[@name='country']/option[text()='Czech Republic'])/@value
 *     <dd>extracts value attribute of option element that match text 'Czech Republic'
 *                 inside of select element with name attribute  'country' inside of
 *                 form with name attribute 'countryForm'</dd>
 * </dl>
 */
 /* This file is inspired by RegexExtractor.
 * author <a href="mailto:hpaluch@gitus.cz">Henryk Paluch</a>
 *            of <a href="http://www.gitus.com">Gitus a.s.</a>
 *
 * See Bugzilla: 37183
 */
public class XPathExtractor extends AbstractTestElement implements
        PostProcessor, Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();
	private static final String MATCH_NR = "matchNr"; // $NON-NLS-1$
	protected static final String KEY_PREFIX = "XPathExtractor."; // $NON-NLS-1$
	public static final String XPATH_QUERY = KEY_PREFIX +"xpathQuery"; // $NON-NLS-1$
	public static final String REFNAME = KEY_PREFIX +"refname"; // $NON-NLS-1$
	public static final String DEFAULT = KEY_PREFIX +"default"; // $NON-NLS-1$
	public static final String TOLERANT = KEY_PREFIX +"tolerant"; // $NON-NLS-1$


    private String concat(String s1,String s2){
        return new StringBuffer(s1).append("_").append(s2).toString(); // $NON-NLS-1$
    }
    
	/**
	 * Do the job - extract value from (X)HTML response using XPath Query.
	 * Return value as variable defined by REFNAME. Returns DEFAULT value
	 * if not found.
	 */
    public void process() {
        JMeterContext context = getThreadContext();        
		JMeterVariables vars = context.getVariables();
		String refName = getRefName();
		vars.put(refName, getDefaultValue());
        vars.put(concat(refName,MATCH_NR), "0"); // In case parse fails // $NON-NLS-1$
        vars.remove(concat(refName,"1")); // In case parse fails // $NON-NLS-1$

		try{			
			Document d = parseResponse(context.getPreviousResult());		
			getValuesForXPath(d,getXPathQuery(),vars, refName);
		}catch(IOException e){// Should not happen
			final String errorMessage = "error on "+XPATH_QUERY+"("+getXPathQuery()+")";
			log.error(errorMessage,e);
			throw new JMeterError(errorMessage,e);
		} catch (ParserConfigurationException e) {// Should not happen
			final String errrorMessage = "error on "+XPATH_QUERY+"("+getXPathQuery()+")";
			log.error(errrorMessage,e);
			throw new JMeterError(errrorMessage,e);
		} catch (SAXException e) {// Can happen for bad input document
			log.warn("error on "+XPATH_QUERY+"("+getXPathQuery()+")"+e.getLocalizedMessage());
		} catch (TransformerException e) {// Can happen for incorrect XPath expression
			log.warn("error on "+XPATH_QUERY+"("+getXPathQuery()+")"+e.getLocalizedMessage());
		}
    }    
            
    /**
     * Clone?
     */
    public Object clone() {
		XPathExtractor cloned = (XPathExtractor) super.clone();
		return cloned;
	}    
        
    /*============= object properties ================*/
    public void setXPathQuery(String val){
        setProperty(XPATH_QUERY,val);   
    }
    
    public String getXPathQuery(){
        return getPropertyAsString(XPATH_QUERY);
    }
  
	public void setRefName(String refName) {
		setProperty(REFNAME, refName);
	}

	public String getRefName() {
		return getPropertyAsString(REFNAME);
	}
    
    public void setDefaultValue(String val) {
		setProperty(DEFAULT, val);
	}

	public String getDefaultValue() {
		return getPropertyAsString(DEFAULT);
	}
	
    public void setTolerant(boolean val) {
		setProperty(new BooleanProperty(TOLERANT, val));
	}

	public boolean isTolerant() {
		return getPropertyAsBoolean(TOLERANT);
	}
	
	/*================= internal business =================*/
    /**
     * Converts (X)HTML response to DOM object Tree.
     * This version cares of charset of response.
     * @param result
     * @return
     * 
     */
    private Document parseResponse(SampleResult result)
      throws UnsupportedEncodingException, IOException, ParserConfigurationException,SAXException
    {
      //TODO: validate contentType for reasonable types?

      // NOTE: responseData encoding is server specific
      //       Therefore we do byte -> unicode -> byte conversion
      //       to ensure UTF-8 encoding as required by XPathUtil
      String unicodeData = new String(result.getResponseData(),
		                      result.getDataEncoding());
      // convert unicode String -> UTF-8 bytes
      byte[] utf8data = unicodeData.getBytes("UTF-8"); // $NON-NLS-1$
      ByteArrayInputStream in = new ByteArrayInputStream(utf8data);
      // this method assumes UTF-8 input data
      return XPathUtil.makeDocument(in,false,false,false,isTolerant());
    }

    /**
     * Extract value from Document d by XPath query.
     * @param d
     * @param query
     * @throws TransformerException
     */
    private void getValuesForXPath(Document d,String query, JMeterVariables vars, String refName)
     throws TransformerException
    {
        String val = null;
     	XObject xObject = XPathAPI.eval(d, query);
        if (xObject.getType() == XObject.CLASS_NODESET) {
	        NodeList matches = xObject.nodelist();
			int length = matches.getLength();
	        vars.put(concat(refName,MATCH_NR), String.valueOf(length));
	        for (int i = 0 ; i < length; i++) {
	            Node match = matches.item(i);
				if ( match instanceof Element){
				// elements have empty nodeValue, but we are usually interested in their content
				   final Node firstChild = match.getFirstChild();
				   if (firstChild != null) {
					   val = firstChild.getNodeValue();
				   } else {
					   val = match.getNodeValue(); // TODO is this correct?
				   }
				} else {				
				   val = match.getNodeValue();
				}
	            if ( val!=null){
	                if (i==0) {// Treat 1st match specially
	                    vars.put(refName,val);                    
	                }
	                vars.put(concat(refName,String.valueOf(i+1)),val);
	            }
			}
	        vars.remove(concat(refName,String.valueOf(length+1)));
     	} else {
	        val = xObject.toString();
	        vars.put(concat(refName, MATCH_NR), "1");
	        vars.put(refName, val);
	        vars.put(concat(refName, "1"), val);
	        vars.remove(concat(refName, "2"));
	    }
    }
}
