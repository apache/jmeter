package org.apache.jmeter.monitor.parser;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.jmeter.monitor.model.ObjectFactory;
import org.apache.jmeter.monitor.model.Status;
import org.apache.jmeter.samplers.SampleResult;

public abstract class ParserImpl implements Parser
{
	private SAXParserFactory PARSERFACTORY = null;
	private SAXParser PARSER = null;
	private MonitorHandler DOCHANDLER = null;
	private ObjectFactory FACTORY = null;
	
    /**
     * 
     */
    public ParserImpl(ObjectFactory factory)
    {
        super();
        FACTORY = factory;
        try {
			PARSERFACTORY = SAXParserFactory.newInstance();
			PARSER = PARSERFACTORY.newSAXParser();
			DOCHANDLER = new MonitorHandler();
        } catch (SAXException e) {
        } catch (ParserConfigurationException e){
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.monitor.parser.Parser#parseBytes(byte[])
     */
    public Status parseBytes(byte[] bytes)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.monitor.parser.Parser#parseString(java.lang.String)
     */
    public Status parseString(String content)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.monitor.parser.Parser#parseSampleResult(org.apache.jmeter.samplers.SampleResult)
     */
    public Status parseSampleResult(SampleResult result)
    {
        return null;
    }

}
