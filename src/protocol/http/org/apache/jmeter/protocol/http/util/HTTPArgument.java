package org.apache.jmeter.protocol.http.util;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class HTTPArgument extends Argument  implements Serializable {
	
	private static final String ENCODED_NAME = "HTTPArgument.encoded_name";
	private static final String ENCODED_VALUE = "HTTPArgument.encoded_value";
	
	/****************************************
	 * Constructor for the Argument object
	 *
	 *@param name   Description of Parameter
	 *@param value  Description of Parameter
	 *@param metadata Description of Parameter
	 ***************************************/
	public HTTPArgument(String name, Object value, Object metadata)
	{
		this(name,value,false);
		this.setMetaData(metadata);
	}
	
	private void encodeName(String name)
	{
		setProperty(ENCODED_NAME,URLEncoder.encode(name));
	}
	
	private void encodeValue(Object value)
	{
		if(value != null)
		{
			setProperty(ENCODED_VALUE,URLEncoder.encode(value.toString()));
		}
	}
	/****************************************
	 * Constructor for the Argument object
	 *
	 *@param name   Description of Parameter
	 *@param value  Description of Parameter
	 ***************************************/
	public HTTPArgument(String name, Object value)
	{
		this(name,value,false);
	}
	
	public HTTPArgument(String name, Object value, boolean alreadyEncoded)
	{
		super(name,value);
		if(alreadyEncoded)
		{
			setProperty(ENCODED_NAME,name);
			setProperty(ENCODED_VALUE,value.toString());
		}
		else
		{
			encodeName(name);
			encodeValue(value);
		}
	}
	
	public HTTPArgument(Argument arg)
	{
		this(arg.getName(),arg.getValue(),arg.getMetaData());
	}

	/****************************************
	 * Constructor for the Argument object
	 ***************************************/
	public HTTPArgument() { }
	
	/****************************************
	 * Sets the Name attribute of the Argument object
	 *
	 *@param newName  The new Name value
	 ***************************************/
	public void setName(String newName)
	{
		super.setName(newName);
		encodeName(newName);
	}
	
	public String getEncodedValue()
	{
		return getPropertyAsString(ENCODED_VALUE);
	}
	
	public String getEncodedName()
	{
		return getPropertyAsString(ENCODED_NAME);
	}

	/****************************************
	 * Sets the Value attribute of the Argument object
	 *
	 *@param newValue  The new Value value
	 ***************************************/
	public void setValue(Object newValue)
	{
		super.setValue(newValue);
		encodeValue(newValue);
	}
	
	public static void convertArgumentsToHTTP(Arguments args)
	{
		List newArguments = new LinkedList();
		Iterator iter = args.getArguments().iterator();
		while(iter.hasNext())
		{
			Argument arg = (Argument)iter.next();
			if(!(arg instanceof HTTPArgument))
			{
				newArguments.add(new HTTPArgument(arg));
			}
			else
			{
				newArguments.add(arg);
			}
		}
		args.removeAllArguments();
		args.setArguments(newArguments);
	}
	
	public static class Test extends TestCase
	{
		public Test(String name)
		{
			super(name);
		}
		
		public void testCloning() throws Exception
		{
			HTTPArgument arg = new HTTPArgument("name.?","value_ here");
			assertEquals("name.%3F",arg.getEncodedName());
			assertEquals("value_+here",arg.getEncodedValue());
			HTTPArgument clone = (HTTPArgument)arg.clone();
			assertEquals("name.%3F",clone.getEncodedName());
			assertEquals("value_+here",clone.getEncodedValue());
		}
		
		public void testConversion() throws Exception
		{
			Arguments args = new Arguments();
			args.addArgument("name.?","value_ here");
			args.addArgument("name$of property","value_.+");
			HTTPArgument.convertArgumentsToHTTP(args);
			List argList = args.getArguments();
			HTTPArgument httpArg = (HTTPArgument)argList.get(0);
			assertEquals("name.%3F",httpArg.getEncodedName());
			assertEquals("value_+here",httpArg.getEncodedValue());
			httpArg = (HTTPArgument)argList.get(1);
			assertEquals("name%24of+property",httpArg.getEncodedName());
			assertEquals("value_.%2B",httpArg.getEncodedValue());				
		}
	}

}
