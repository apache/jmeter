package org.apache.jmeter.protocol.http.util;

import junit.framework.TestCase;
import java.net.URLEncoder;
import java.net.URLDecoder;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.Serializable;

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
	private static final String ALWAYS_ENCODE = "HTTPArgument.always_encode";
	
	
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
		if(getAlwaysEncode())
		{
			name = URLEncoder.encode(name);
		}
		setProperty(ENCODED_NAME,name);
	}
	
	public void setAlwaysEncode(boolean ae)
	{
		setProperty(ALWAYS_ENCODE,new Boolean(ae));
	}
	
	public boolean getAlwaysEncode()
	{
		return getPropertyAsBoolean(ALWAYS_ENCODE);
	}
	
	private void encodeValue(Object value)
	{
		if(value != null)
		{
			if(getAlwaysEncode())
			{
				value = URLEncoder.encode(value.toString());
			}
			setProperty(ENCODED_VALUE,value.toString());
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
		setAlwaysEncode(true);
		if(alreadyEncoded)
		{
			try
			{
				setName(URLDecoder.decode(name));
			}
			catch(IllegalArgumentException e)
			{
				setName(name);
			}
			try
			{
				setValue(URLDecoder.decode(value.toString()));
			}
			catch(IllegalArgumentException e)
			{
				setValue(value.toString());
			}
			setProperty(ENCODED_NAME,name);
			setProperty(ENCODED_VALUE,value.toString());
		}
		else
		{
			setName(name);
			setValue(value);
		}
	}
	
	public void setProperty(String key,Object value)
	{
		if(value == null || !value.equals(getProperty(key)))
		{
			if(Argument.NAME.equals(key))
			{
				if(value == null)
				{
					encodeName("");
				}
				else
				{
					encodeName(value.toString());
				}
			}
			else if(Argument.VALUE.equals(key))
			{
				encodeValue(value);
			}
			super.setProperty(key,value);
		}
	}
	
	public HTTPArgument(String name,Object value,Object metaData,boolean alreadyEncoded)
	{
		this(name,value,alreadyEncoded);
		setMetaData(metaData);
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
		if(newName == null || !newName.equals(getName()))
		{
			super.setName(newName);
		}
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
		if(newValue == null || !newValue.equals(getValue()))
		{
			super.setValue(newValue);
		}
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
