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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.FunctionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;

/**
 * Common constants and methods for HTTP samplers
 * 
 * @version $Revision$ Last updated $Date$
 */
public abstract class HTTPSamplerBase extends AbstractSampler implements TestListener
{

    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final int DEFAULT_HTTP_PORT = 80;

    public final static String HEADERS= "headers";
    public final static String HEADER= "header";
    public final static String ARGUMENTS= "HTTPsampler.Arguments";
    public final static String AUTH_MANAGER= "HTTPSampler.auth_manager";
    public final static String COOKIE_MANAGER= "HTTPSampler.cookie_manager";
    public final static String HEADER_MANAGER= "HTTPSampler.header_manager";
    public final static String MIMETYPE= "HTTPSampler.mimetype";
    public final static String DOMAIN= "HTTPSampler.domain";
    public final static String PORT= "HTTPSampler.port";
    public final static String METHOD= "HTTPSampler.method";

    public final static String PATH= "HTTPSampler.path";
    public final static String FOLLOW_REDIRECTS= "HTTPSampler.follow_redirects";
	public final static String AUTO_REDIRECTS = "HTTPSampler.auto_redirects";
    public final static String PROTOCOL= "HTTPSampler.protocol";
    public final static String DEFAULT_PROTOCOL= "http";
    public final static String URL= "HTTPSampler.URL";
    public final static String POST= "POST";
    public final static String GET= "GET";
    public final static String USE_KEEPALIVE= "HTTPSampler.use_keepalive";
    public final static String FILE_NAME= "HTTPSampler.FILE_NAME";
    public final static String FILE_FIELD= "HTTPSampler.FILE_FIELD";
    public final static String FILE_DATA= "HTTPSampler.FILE_DATA";
    public final static String FILE_MIMETYPE= "HTTPSampler.FILE_MIMETYPE";
    public final static String CONTENT_TYPE= "HTTPSampler.CONTENT_TYPE";
    public final static String NORMAL_FORM= "normal_form";
    public final static String MULTIPART_FORM= "multipart_form";
    public final static String ENCODED_PATH= "HTTPSampler.encoded_path";
    public final static String IMAGE_PARSER= "HTTPSampler.image_parser";
	public final static String MONITOR = "HTTPSampler.monitor";

    /** A number to indicate that the port has not been set.  **/
    public static final int UNSPECIFIED_PORT= 0;
    boolean dynamicPath = false;
	protected final static String NON_HTTP_RESPONSE_CODE=
		"Non HTTP response code";
	protected final static String NON_HTTP_RESPONSE_MESSAGE=
		"Non HTTP response message";

    static {
        try
        {
            pattern= new Perl5Compiler().compile(
                    " ",
                    Perl5Compiler.READ_ONLY_MASK
                        & Perl5Compiler.SINGLELINE_MASK);
        }
        catch (MalformedPatternException e)
        {
            log.error("Cant compile pattern.", e);
            throw new Error(e.toString()); // programming error -- bail out
        }
    }
    
	public HTTPSamplerBase()
	{
	}

    public void setFileField(String value)
    {
        setProperty(FILE_FIELD, value);
    }
    public String getFileField()
    {
        return getPropertyAsString(FILE_FIELD);
    }
    public void setFilename(String value)
    {
        setProperty(FILE_NAME, value);
    }
    public String getFilename()
    {
        return getPropertyAsString(FILE_NAME);
    }
    public void setProtocol(String value)
    {
        setProperty(PROTOCOL, value.toLowerCase());
    }
    public String getProtocol()
    {
        String protocol= getPropertyAsString(PROTOCOL);
        if (protocol == null || protocol.equals(""))
        {
            return DEFAULT_PROTOCOL;
        }
        else
        {
            return protocol;
        }
    }

    /**
     *  Sets the Path attribute of the UrlConfig object
     * Also calls parseArguments to extract and store any
     * query arguments
     *  
     *@param  path  The new Path value
     */
    public void setPath(String path)
    {
        if (GET.equals(getMethod()))
        {
            int index= path.indexOf("?");
            if (index > -1)
            {
                setProperty(PATH, path.substring(0, index));
                parseArguments(path.substring(index + 1));
            }
            else
            {
                setProperty(PATH, path);
            }
        }
        else
        {
            setProperty(PATH, path);
        }
    }

    public String getPath()
    {
        String p = getPropertyAsString(PATH);
        if(dynamicPath)
        {
           return encodeSpaces(p);
        }
        return p;
    }

    public void setFollowRedirects(boolean value)
    {
        setProperty(new BooleanProperty(FOLLOW_REDIRECTS, value));
    }

    public boolean getFollowRedirects()
    {
        return getPropertyAsBoolean(FOLLOW_REDIRECTS);
    }

    public void setMethod(String value)
    {
        setProperty(METHOD, value);
    }

    public String getMethod()
    {
        return getPropertyAsString(METHOD);
    }

    public void setUseKeepAlive(boolean value)
    {
        setProperty(new BooleanProperty(USE_KEEPALIVE, value));
    }

    public boolean getUseKeepAlive()
    {
        return getPropertyAsBoolean(USE_KEEPALIVE);
    }

	public void setMonitor(String value){
	    this.setProperty(MONITOR,value);
	}
	
	public String getMonitor(){
		return this.getPropertyAsString(MONITOR);
	}
	
	public boolean isMonitor(){
		return this.getPropertyAsBoolean(MONITOR);
	}

    public void addEncodedArgument(String name, String value, String metaData)
    {
        log.debug(
            "adding argument: name: "
                + name
                + " value: "
                + value
                + " metaData: "
                + metaData);

        HTTPArgument arg= new HTTPArgument(name, value, metaData, true);

        if (arg.getName().equals(arg.getEncodedName())
            && arg.getValue().equals(arg.getEncodedValue()))
        {
            arg.setAlwaysEncoded(false);
        }
		this.getArguments().addArgument(arg);
    }

    public void addArgument(String name, String value)
    {
        this.getArguments().addArgument(new HTTPArgument(name, value));
    }

	public void addArgument(String name, String value, String metadata)
	{
		this.getArguments().addArgument(new HTTPArgument(name, value, metadata));
	}

    public void addTestElement(TestElement el)
    {
        if (el instanceof CookieManager)
        {
            setCookieManager((CookieManager)el);
        }
        else if (el instanceof HeaderManager)
        {
            setHeaderManager((HeaderManager)el);
        }
        else if (el instanceof AuthManager)
        {
            setAuthManager((AuthManager)el);
        }
        else
        {
            super.addTestElement(el);
        }
    }

    public void setPort(int value)
    {
        setProperty(new IntegerProperty(PORT, value));
    }

    public int getPort()
    {
        int port= getPropertyAsInt(PORT);
        if (port == UNSPECIFIED_PORT)
        {
            if ("https".equalsIgnoreCase(getProtocol()))
            {
                return DEFAULT_HTTPS_PORT;
            }
            return DEFAULT_HTTP_PORT;
        }
        return port;
    }

    public void setDomain(String value)
    {
        setProperty(DOMAIN, value);
    }

    public String getDomain()
    {
        return getPropertyAsString(DOMAIN);
    }

    public void setArguments(Arguments value)
    {
        setProperty(new TestElementProperty(ARGUMENTS, value));
    }

    public Arguments getArguments()
    {
        return (Arguments)getProperty(ARGUMENTS).getObjectValue();
    }

    public void setAuthManager(AuthManager value)
    {
        setProperty(new TestElementProperty(AUTH_MANAGER, value));
    }

    public AuthManager getAuthManager()
    {
        return (AuthManager)getProperty(AUTH_MANAGER).getObjectValue();
    }

    public void setHeaderManager(HeaderManager value)
    {
        setProperty(new TestElementProperty(HEADER_MANAGER, value));
    }

    public HeaderManager getHeaderManager()
    {
        return (HeaderManager)getProperty(HEADER_MANAGER).getObjectValue();
    }

    public void setCookieManager(CookieManager value)
    {
        setProperty(new TestElementProperty(COOKIE_MANAGER, value));
    }

    public CookieManager getCookieManager()
    {
        return (CookieManager)getProperty(COOKIE_MANAGER).getObjectValue();
    }

    public void setMimetype(String value)
    {
        setProperty(MIMETYPE, value);
    }

    public String getMimetype()
    {
        return getPropertyAsString(MIMETYPE);
    }

    public boolean isImageParser()
    {
        return getPropertyAsBoolean(IMAGE_PARSER);
    }

    public void setImageParser(boolean parseImages)
    {
        setProperty(new BooleanProperty(IMAGE_PARSER, parseImages));
    }

    /**
     * Obtain a result that will help inform the user that an error has occured
     * during sampling, and how long it took to detect the error.
     * 
     * @param e Exception representing the error.
     * @param data a piece of data associated to the error (e.g. URL)
     * @param time time spent detecting the error (0 for client-only issues)
     * @return a sampling result useful to inform the user about the exception.
     */
    protected HTTPSampleResult errorResult(Throwable e, String data, long time)
    {
        HTTPSampleResult res= new HTTPSampleResult(time);
        res.setSampleLabel("Error");
        res.setSamplerData(data);
        res.setDataType(HTTPSampleResult.TEXT);
        ByteArrayOutputStream text= new ByteArrayOutputStream(200);
        e.printStackTrace(new PrintStream(text));
        res.setResponseData(text.toByteArray());
        res.setResponseCode(NON_HTTP_RESPONSE_CODE);
        res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE);
        res.setSuccessful(false);
        try {
			res.setURL(getUrl());
        } catch (MalformedURLException ex){
        }
        res.setMonitor(this.isMonitor());
        return res;
    }

    /**
     * Get the URL, built from its component parts.
     * 
     * @return The URL to be requested by this sampler.
     * @throws MalformedURLException
     */
    public URL getUrl() throws MalformedURLException
    {
        String pathAndQuery= null;
        if (this.getMethod().equals(GET)
            && getQueryString().length() > 0)
        {
            if (this.getPath().indexOf("?") > -1)
            {
                pathAndQuery= this.getPath() + "&" + getQueryString();
            }
            else
            {
                pathAndQuery= this.getPath() + "?" + getQueryString();
            }
        }
        else
        {
            pathAndQuery= this.getPath();
        }
        if (!pathAndQuery.startsWith("/"))
        {
            pathAndQuery= "/" + pathAndQuery;
        }
        if (getPort() == UNSPECIFIED_PORT || getPort() == DEFAULT_HTTP_PORT)
        {
            return new URL(getProtocol(), getDomain(), pathAndQuery);
        }
        else
        {
            return new URL(
                getProtocol(),
                getPropertyAsString(DOMAIN),
                getPort(),
                pathAndQuery);
        }
    }

    /**
     * Gets the QueryString attribute of the UrlConfig object.
     *
     * @return    the QueryString value
     */
    public String getQueryString()
    {
        StringBuffer buf= new StringBuffer();
        PropertyIterator iter= getArguments().iterator();
        boolean first= true;
        while (iter.hasNext())
        {
            HTTPArgument item= null;
            try
            {
                item= (HTTPArgument)iter.next().getObjectValue();
            }
            catch (ClassCastException e)
            {
                item= new HTTPArgument((Argument)iter.next().getObjectValue());
            }
            if (!first)
            {
                buf.append("&");
            }
            else
            {
                first= false;
            }
            buf.append(item.getEncodedName());
            if (item.getMetaData() == null)
            {
                buf.append("=");
            }
            else
            {
                buf.append(item.getMetaData());
            }
            buf.append(item.getEncodedValue());
        }
        return buf.toString();
    }


    //Mark Walsh 2002-08-03, modified to also parse a parameter name value
    //string, where string contains only the parameter name and no equal sign.
    /**
     * This method allows a proxy server to send over the raw text from a
     * browser's output stream to be parsed and stored correctly into the
     * UrlConfig object.
     *
     * For each name found, addEncodedArgument() is called 
     *
     * @param queryString - the query string
     * 
     */
    public void parseArguments(String queryString)
    {
        String[] args= JOrphanUtils.split(queryString, "&");
        for (int i= 0; i < args.length; i++)
        {
            // need to handle four cases:   string contains name=value
            //                              string contains name=
            //                              string contains name
            //                              empty string
            // find end of parameter name
            int endOfNameIndex= 0;
            String metaData= ""; // records the existance of an equal sign
            if (args[i].indexOf("=") != -1)
            {
                // case of name=value, name=
                endOfNameIndex= args[i].indexOf("=");
                metaData= "=";
            }
            else
            {
                metaData= "";
                if (args[i].length() > 0)
                {
                    endOfNameIndex= args[i].length(); // case name
                }
                else
                {
                    endOfNameIndex= 0; //case where name value string is empty
                }
            }
            // parse name
            String name= ""; // for empty string
            if (args[i].length() > 0)
            {
                //for non empty string
                name= args[i].substring(0, endOfNameIndex);
            }
            // parse value
            String value= "";
            if ((endOfNameIndex + 1) < args[i].length())
            {
                value= args[i].substring(endOfNameIndex + 1, args[i].length());
            }
            if (name.length() > 0)
            {
                    addEncodedArgument(name, value, metaData);
            }
        }
    }

    public String toString()
    {
        try
        {
            return this.getUrl().toString()
                + ((POST.equals(getMethod()))
                    ? "\nQuery Data: " + getQueryString()
                    : "");
        }
        catch (MalformedURLException e)
        {
            return "";
        }
    }

	/**
	 * Do a sampling and return its results.
	 *
	 * @param e  <code>Entry</code> to be sampled
	 * @return   results of the sampling
	 */
	public SampleResult sample(Entry e) {
	    return sample();
	}
	
    /**
     * Perform a sample, and return the results
     * 
     * @return results of the sampling
     */
    public SampleResult sample()
    {
        try
        {
            SampleResult res= sample(getUrl(), getMethod(), false, 0);
            res.setSampleLabel(getName());
            return res;
        }
        catch (MalformedURLException e)
        {
            return errorResult(e, getName(), 0);
        }
    }

	protected abstract HTTPSampleResult sample(URL u, String s, boolean b, int i);

	protected static Pattern pattern;
	private static ThreadLocal localMatcher = new ThreadLocal()
	    {
	        protected synchronized Object initialValue()
	        {
	            return new Perl5Matcher();
	        }
	    };
	private static Substitution spaceSub = new StringSubstitution("%20");


	protected String encodeSpaces(String path) {
		// TODO JDK1.4 
		// this seems to be equivalent to path.replaceAll(" ","%20");
	    // TODO move to JMeterUtils or jorphan.
	    // unless we move to JDK1.4. (including the
	    // 'pattern' initialization code earlier on)
	    path=
	        Util.substitute(
	            (Perl5Matcher)localMatcher.get(),
	            pattern,
	            spaceSub,
	            path,
	            Util.SUBSTITUTE_ALL);
	    return path;
	}

	protected static final int MAX_REDIRECTS = 5;
	protected static final int MAX_FRAME_DEPTH = 5;
   /* (non-Javadoc)
    * @see org.apache.jmeter.testelement.TestListener#testEnded()
    */
   public void testEnded()
   {
      dynamicPath = false;
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
    */
   public void testEnded(String host)
   {
      testEnded();
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
    */
   public void testIterationStart(LoopIterationEvent event)
   {
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.testelement.TestListener#testStarted()
    */
   public void testStarted()
   {
      JMeterProperty pathP = getProperty(PATH);
      log.info("path property is a " + pathP.getClass().getName());
      log.info("path beginning value = " + pathP.getStringValue());
      if(pathP instanceof StringProperty && !"".equals(pathP.getStringValue()))
      {
         log.info("Encoding spaces in path");
         pathP.setObjectValue(encodeSpaces(pathP.getStringValue()));
      }
      else
      {
         log.info("setting dynamic path to true");
         dynamicPath = true;
      }
      log.info("path ending value = " + pathP.getStringValue());
   }
   /* (non-Javadoc)
    * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
    */
   public void testStarted(String host)
   {
      testStarted();
   }
   /* (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      HTTPSamplerBase base = (HTTPSamplerBase)super.clone();
      base.dynamicPath = dynamicPath;
      return base;
   }
}
