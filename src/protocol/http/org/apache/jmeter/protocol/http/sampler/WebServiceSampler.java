package org.apache.jmeter.protocol.http.sampler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.io.TextFile;
import org.apache.soap.Envelope;
import org.apache.soap.messaging.Message;
import org.apache.soap.transport.SOAPTransport;
import org.apache.soap.util.xml.XMLParserUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Sampler to handle Web Service requests. It uses Apache
 * soap drivers to perform the XML generation, connection
 * soap encoding and other soap functions.
 *
 * @author Peter Lin
 * @version $Id: 
 */
public class WebServiceSampler extends HTTPSampler
{
	public static final String XML_DATA = "HTTPSamper.xml_data";
	public static final String SOAP_ACTION = "Soap.Action";
    public static final String XML_DATA_FILE = "WebServiceSampler.xml_data_file";
	public String SOAPACTION = null;
	transient SampleResult RESULT = null;
	protected Document XMLMSG = null;

	/**
	 * set the XML data
	 * @param String data
	 */
	public void setXmlData(String data)
	{
		setProperty(XML_DATA,data);
	}
    
    public void setXmlFile(String filename)
    {
        setProperty(XML_DATA_FILE,filename);
    }
    
    public String getXmlFile()
    {
        return getPropertyAsString(XML_DATA_FILE);
    }

	/**
	 * get the XML data as a string
	 * @return String data
	 */
	public String getXmlData()
	{
		return getPropertyAsString(XML_DATA);
	}

	/**
	 * set the soap action which should be in
	 * the form of an URN
	 * @param String data
	 */
	public void setSoapAction(String data){
		setProperty(SOAP_ACTION,data);
	}

	/**
	 * return the soap action string
	 * @return
	 */
	public String getSoapAction() {
		System.out.println(getPropertyAsString(SOAP_ACTION));
		return getPropertyAsString(SOAP_ACTION);
	}
    
    private String retrieveRuntimeXmlData()
    {
        String file = getXmlFile();
        if(file.length() > 0)
        {
            TextFile contents = new TextFile(file);
            if(contents.exists())
            {
                return contents.getText();
            }
        }
        return getXmlData();
    }

	/**
	 * This method uses Apache soap util to create
	 * the proper DOM elements.
	 * @return Element
	 */	
	public org.w3c.dom.Element createDocument(){
        String xmlData = retrieveRuntimeXmlData();
		if (xmlData != null && xmlData.length() > 0){	
			try {
				DocumentBuilder xdb = XMLParserUtils.getXMLDocBuilder();
				Document doc = xdb.parse(new InputSource(new StringReader(xmlData)));
				return doc.getDocumentElement();
			} catch (Exception ex){
				ex.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public SampleResult sample(Entry e)
	{
		return sample();
	}

	public SampleResult sample(){
		RESULT = new SampleResult();
		sampleWithApache();
		return RESULT;
	}

	public void sampleWithApache() {
		try {
			Envelope msgEnv = Envelope.unmarshall(createDocument());

			// send the message
			Message msg = new Message ();
			long start = System.currentTimeMillis();
			msg.send(this.getUrl(), "http://tempuri.org/doInference", msgEnv);
			RESULT.setTime(System.currentTimeMillis() - start);

			SOAPTransport st = msg.getSOAPTransport ();
			BufferedReader br = st.receive();
			StringBuffer buf = new StringBuffer();
			String line;
			while((line = br.readLine()) != null){
				buf.append(line);
			}
			RESULT.setResponseMessage(buf.toString());
			RESULT.setSuccessful(true);
			// this doesn't really apply, since the soap
			// driver doesn't provide a resonse code
			// RESULT.setResponseCode("200");

		} catch (Exception exception){
			// exception.printStackTrace();
			RESULT.setSuccessful(false);
		}
	}

	/**
	 * We override this to prevent the wrong encoding
	 * and provide no implementation. We want to
	 * reuse the other parts of HTTPSampler, but not
	 * the connection. The connection is handled by
	 * the Apache SOAP driver.
	 */
	public void addEncodedArgument(String name, String value, String metaData)	{
	}

	/**
	 * We override this to prevent the wrong encoding
	 * and provide no implementation. We want to
	 * reuse the other parts of HTTPSampler, but not
	 * the connection. The connection is handled by
	 * the Apache SOAP driver.
	 */
	protected HttpURLConnection setupConnection(URL u, String method)
		throws IOException
	{
		return null;
	}
	
	/**
	 * We override this to prevent the wrong encoding
	 * and provide no implementation. We want to
	 * reuse the other parts of HTTPSampler, but not
	 * the connection. The connection is handled by
	 * the Apache SOAP driver.
	 */
	protected long connect() throws IOException
	{
		return -1;
	}

}

