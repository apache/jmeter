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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;

import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.DefaultMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.apache.jmeter.protocol.http.parser.HTMLParser;
import org.apache.jmeter.protocol.http.util.HTTPArgument;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

import org.apache.jmeter.util.JMeterUtils;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;

import org.apache.log.Logger;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 *
 * @version $Revision$ Last updated $Date$
 */
public class HTTPSampler2 extends AbstractSampler
{
	transient private static Logger log= LoggingManager.getLoggerForClass();

    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final int DEFAULT_HTTP_PORT = 80;

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

    /** A number to indicate that the port has not been set.  **/
    public static final int UNSPECIFIED_PORT= 0;
    private static final int MAX_REDIRECTS= 5; // As recommended by RFC 2068
    private static final int MAX_FRAME_DEPTH= 5;
    
    protected static String encoding= "iso-8859-1";
    //private static final PostWriter postWriter= new PostWriter();

	protected final static String NON_HTTP_RESPONSE_CODE=
		"Non HTTP response code";
	protected final static String NON_HTTP_RESPONSE_MESSAGE=
		"Non HTTP response message";

    static {// TODO - document what this is doing and why
        System.setProperty(
            "java.protocol.handler.pkgs",
            JMeterUtils.getPropDefault(
                "ssl.pkgs",
                "com.sun.net.ssl.internal.www.protocol"));
        System.setProperty("javax.net.ssl.debug", "all");
        // Set the default to Avalon Logkit, if not already defined:
        if (System.getProperty("org.apache.commons.logging.Log")==null)
        {
        	System.setProperty("org.apache.commons.logging.Log"
        			,"org.apache.commons.logging.impl.LogKitLogger");
        }
    }

    /*
     * Connection is re-used if possible
     */
    private transient HttpConnection httpConn = null;

    /*
     * These variables are recreated every time
     * Find a better way of passing them round
     */
    private transient HttpMethodBase httpMethod = null;
    private transient HttpState httpState = null;

    private static Pattern pattern;
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
    
    private static ThreadLocal localMatcher= new ThreadLocal()
    {
        protected synchronized Object initialValue()
        {
            return new Perl5Matcher();
        }
    };

    private static Substitution spaceSub= new StringSubstitution("%20");

    /* Should we delegate redirects to the URLConnection implementation?
     * This can be useful with alternate URLConnection implementations.
     * 
     * Defaults to true, because HttpClient handles redirects OK 
     */
    private static boolean delegateRedirects=
        JMeterUtils
            .getJMeterProperties()
            .getProperty("HTTPSampler.delegateRedirects", "true")
            .equalsIgnoreCase("true");

	/**
	 * Constructor for the HTTPSampler object.
	 */
	public HTTPSampler2()
	{
		setArguments(new Arguments());
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

    public void setEncodedPath(String path)
    {
        path= encodeSpaces(path);
        setProperty(ENCODED_PATH, path);
    }

    private String encodeSpaces(String path)
    {
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

    public String getEncodedPath()
    {
        return getPropertyAsString(ENCODED_PATH);
    }

    public void setProperty(JMeterProperty prop)
    {
        super.setProperty(prop);
        if (PATH.equals(prop.getName()))
        {
            setEncodedPath(prop.getStringValue());
        }
    }

    public void addProperty(JMeterProperty prop)
    {
        super.addProperty(prop);
        if (PATH.equals(prop.getName()))
        {
            super.addProperty(
                new StringProperty(
                    ENCODED_PATH,
                    encodeSpaces(prop.getStringValue())));
        }
    }

    public String getPath()
    {
        return getPropertyAsString(PATH);
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
     * Do a sampling and return its results.
     *
     * @param e  <code>Entry</code> to be sampled
     * @return   results of the sampling
     */
    public SampleResult sample(Entry e)
    {
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
            res.setSampleLabel(getName());// resets the parent
            return res;
        }
        catch (MalformedURLException e)
        {
            return errorResult(e, getName(), 0);
        }
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
    private HTTPSampleResult errorResult(Throwable e, String data, long time)
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
        if (this.getMethod().equals(HTTPSampler2.GET)
            && getQueryString().length() > 0)
        {
            if (this.getEncodedPath().indexOf("?") > -1)
            {
                pathAndQuery= this.getEncodedPath() + "&" + getQueryString();
            }
            else
            {
                pathAndQuery= this.getEncodedPath() + "?" + getQueryString();
            }
        }
        else
        {
            pathAndQuery= this.getEncodedPath();
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
                getPropertyAsString(HTTPSampler2.DOMAIN),
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

    /**
     * Set request headers in preparation to opening a connection.
     *
     * @param conn       <code>URLConnection</code> to set headers on
     * @exception IOException  if an I/O exception occurs
     */
    private void setPostHeaders(PostMethod post) throws IOException
    {
    	// Probably nothing needed, because the PostMethod class takes care of it
//        /*postWriter.*/
//    	setHeaders(post, this);
    }
    
//    protected final static String BOUNDARY =
//        "---------------------------7d159c1302d0y0";
//    //protected final static byte[] CRLF = { 0x0d, 0x0A };
//
//    public void setHeaders(HttpMethod method, HTTPSampler2 sampler)
//    throws IOException
//    {
//
//    // If filename was specified then send the post using multipart syntax
//    String filename = sampler.getFileField();
//    if ((filename != null) && (filename.trim().length() > 0))
//    {
//    	method.setRequestHeader(
//            "Content-type",
//            "multipart/form-data; boundary=" + BOUNDARY);
////        connection.setDoOutput(true);
////        connection.setDoInput(true);
//    }
//
//    // No filename specified, so send the post using normal syntax
//    else
//    {
//        String postData = sampler.getQueryString();
//    	method.setRequestHeader(
//            "Content-length",
//            "" + postData.length());
//    	method.setRequestHeader(
//            "Content-type",
//            "application/x-www-form-urlencoded");
////        connection.setDoOutput(true);
//    }
//}

    /**
     * Send POST data from <code>Entry</code> to the open connection.
     *
     * @param connection <code>URLConnection</code> where POST data should
     *                   be sent
     * @exception IOException  if an I/O exception occurs
     */
    private void sendPostData(HttpMethod connection) throws IOException
    {
        /*postWriter.*/
    	sendPostData((PostMethod)connection, this);
    }

    /**
     * Send POST data from Entry to the open connection.
     */
    public void sendPostData(PostMethod post, HTTPSampler2 sampler)
        throws IOException
    {
        PropertyIterator args = sampler.getArguments().iterator();
        while (args.hasNext())
        {
            Argument arg = (Argument) args.next().getObjectValue();
            post.addParameter(arg.getName(),arg.getValue());
        }
        // If filename was specified then send the post using multipart syntax
        String filename = sampler.getFilename();
        if ((filename != null) && (filename.trim().length() > 0))
        {
        	File input = new File(filename);
            if (input.length() < Integer.MAX_VALUE) {
                post.setRequestContentLength((int)input.length());
            } else {
                post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
            }
            //TODO - is this correct?
            post.setRequestHeader("Content-Disposition",
        		"form-data; name=\""
	            + encode(sampler.getFileField())
	            + "\"; filename=\""
	            + encode(filename)
	            + "\"");
            // Specify content type and encoding
            post.setRequestHeader("Content-type", sampler.getMimetype());
            post.setRequestBody(new FileInputStream(input));
        }
    }

    private String encode(String value)
    {
        StringBuffer newValue = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            if (chars[i] == '\\')
            {
                newValue.append("\\\\");
            }
            else
            {
                newValue.append(chars[i]);
            }
        }
        return newValue.toString();
    }

    /**
     * Returns an <code>HttpURLConnection</code> fully ready to attempt 
     * connection. This means it sets the request method (GET or
     * POST), headers, cookies, and authorization for the URL request.
     * <p>
     * The request infos are saved into the sample result if one is provided.
     *
     * @param u                <code>URL</code> of the URL request
     * @param method            http/https
     * @param res               sample result to save request infos to 
     * @return                 <code>HttpURLConnection</code> ready for .connect
     * @exception IOException  if an I/O Exception occurs
     */
    private HttpConnection setupConnection(
        URL u,
        String method,
        HTTPSampleResult res)
        throws IOException
    {
    
    	String urlStr = u.toString();

       	org.apache.commons.httpclient.URI 
		    uri = new org.apache.commons.httpclient.URI(urlStr);

        String schema = uri.getScheme();
        if ((schema == null) || (schema.equals("")))
        {
            schema = "http";
        }
        Protocol protocol = Protocol.getProtocol(schema);

        String host = uri.getHost();
        int port = uri.getPort();
        
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(host,port,protocol);
        if (httpConn!= null && hc.hostEquals(httpConn))
        {
        	//Same details, no need to reset
        }
        else
        {
        	httpConn = new HttpConnection(hc);
        	//TODO check these
            httpConn.setProxyHost(System.getProperty("http.proxyHost"));
            httpConn.setProxyPort( Integer.parseInt(System.getProperty("http.proxyPort","80")));
        }

        if (method.equals(HTTPSampler2.POST))
        {
            httpMethod = new PostMethod(urlStr);
        }
        else
        {
        	httpMethod = new GetMethod(urlStr);
        	//httpMethod;
        	new DefaultMethodRetryHandler();
        }
        

        // TODO make this a JMeter property
        httpMethod.setHttp11(!System.getProperty("http.version","1.1").equals("1.0"));

        httpState = new HttpState();
        if (httpConn.isProxied() && httpConn.isSecure()) {
            httpMethod = new ConnectMethod(httpMethod);
        }
        
        // Allow HttpClient to handle the redirects:
        httpMethod.setFollowRedirects(delegateRedirects && res.isRedirect());
        
        //HttpURLConnection conn=null;

        // a well-bahaved browser is supposed to send 'Connection: close'
        // with the last request to an HTTP server. Instead, most browsers
        // leave it to the server to close the connection after their
        // timeout period. Leave it to the JMeter user to decide.
        if (getUseKeepAlive())
        {
        	httpMethod.setRequestHeader("Connection", "keep-alive");
        }
        else
        {
        	httpMethod.setRequestHeader("Connection", "close");
        }

        String hdrs=setConnectionHeaders(httpMethod, u, getHeaderManager());
        String cookies= setConnectionCookie(httpMethod, u, getCookieManager());

        if (res != null)
        {
            StringBuffer sb= new StringBuffer();
            if (method.equals(HTTPSampler.POST))
            {
            	String q = this.getQueryString();
				res.setQueryString(q);
                sb.append("Query data:\n");
                sb.append(q);
                sb.append('\n');
            }
            if (cookies != null)
            { 
            	res.setCookies(cookies);
                sb.append("\nCookie Data:\n");
                sb.append(cookies);
                sb.append('\n');
            }
            res.setSamplerData(sb.toString());
            //TODO rather than stuff all the information in here,
            //pick it up from the individual fields later 
            
            res.setURL(u);
            res.setHTTPMethod(method);
            res.setRequestHeaders(hdrs);
        }

        setConnectionAuthorization(httpMethod, u, getAuthManager());

        if (method.equals(HTTPSampler2.POST))
        {
            setPostHeaders((PostMethod) httpMethod);
        }
        return httpConn;
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


    /**
     * Gets the ResponseHeaders from the URLConnection
     *
     * @param conn  connection from which the headers are read
     * @return string containing the headers, one per line
     */
    protected String getResponseHeaders(HttpMethod conn)
        throws IOException
    {
        StringBuffer headerBuf= new StringBuffer();
        Header rh[]=conn.getResponseHeaders();
		headerBuf.append(rh[0].toString());//Leave header as is 
        headerBuf.append("\n");

        for (int i= 1; i < rh.length; i++)
        {
        	String key = rh[i].getName();
            if (!key.equalsIgnoreCase("transfer-encoding"))//TODO - why is this not saved?
            {
                headerBuf.append(key);
                headerBuf.append(": ");
                headerBuf.append(rh[i].getValue());
                headerBuf.append("\n");
            }
        }
        return headerBuf.toString();
    }

    /**
     * Extracts all the required cookies for that particular URL request and
     * sets them in the <code>HttpURLConnection</code> passed in.
     *
     * @param conn          <code>HttpUrlConnection</code> which represents the
     *                      URL request
     * @param u             <code>URL</code> of the URL request
     * @param cookieManager the <code>CookieManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     */
    private String setConnectionCookie(
        HttpMethod conn,
        URL u,
        CookieManager cookieManager)
    {
        String cookieHeader= null;
        if (cookieManager != null)
        {
            cookieHeader= cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null)
            {
                conn.setRequestHeader("Cookie", cookieHeader);
            }
        }
        return cookieHeader;
    }

    /**
     * Extracts all the required headers for that particular URL request and
     * sets them in the <code>HttpURLConnection</code> passed in
     *
     *@param conn           <code>HttpUrlConnection</code> which represents the
     *                      URL request
     *@param u              <code>URL</code> of the URL request
     *@param headerManager  the <code>HeaderManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     * @return the headers as a string
     */
    private String setConnectionHeaders(
        HttpMethod conn,
        URL u,
        HeaderManager headerManager)
    {
    	StringBuffer hdrs = new StringBuffer(100);
        if (headerManager != null)
        {
            CollectionProperty headers= headerManager.getHeaders();
            if (headers != null)
            {
                PropertyIterator i= headers.iterator();
                while (i.hasNext())
                {
                    Header header= (Header)i.next().getObjectValue();
                    String n=header.getName();
                    String v=header.getValue();
                    conn.setRequestHeader(n,v);
                    hdrs.append(n);  
					hdrs.append(": ");  
					hdrs.append(v);  
					hdrs.append("\n");  
                }
            }
        }
        return hdrs.toString();
    }

    /**
     * Extracts all the required authorization for that particular URL request
     * and sets it in the <code>HttpURLConnection</code> passed in.
     *
     * @param conn        <code>HttpUrlConnection</code> which represents the
     *                    URL request
     * @param u           <code>URL</code> of the URL request
     * @param authManager the <code>AuthManager</code> containing all the
     *                    cookies for this <code>UrlConfig</code>
     */
    private void setConnectionAuthorization(
        HttpMethod method,
        URL u,
        AuthManager authManager)
    {
        if (authManager != null)
        {
            Authorization authHeader= authManager.getAuthForURL(u);
            if (authHeader != null)
            {
            	//authHeader.get
                httpState.setCredentials(
                	"realm",
					authHeader.getURL(),
                	new NTCredentials(// Includes other types of Credentials
                			authHeader.getUser(),
							authHeader.getPass(),
                			"thishost",
							"targetdomain"
							)
					);

            }
        }
    }

    /**
     * Samples the URL passed in and stores the result in
     * <code>HTTPSampleResult</code>, following redirects and downloading
     * page resources as appropriate.
     * <p>
     * When getting a redirect target, redirects are not followed and 
     * resources are not downloaded. The caller will take care of this.
     *
     * @param url           URL to sample
     * @param method        HTTP method: GET, POST,...
     * @param areFollowingRedirect whether we're getting a redirect target
     * @param frameDepth    Depth of this target in the frame structure.
     *                      Used only to prevent infinite recursion.
     * @return              results of the sampling
     */
    private HTTPSampleResult sample(
        URL url,
        String method,
        boolean areFollowingRedirect,
        int frameDepth)
    {

        String urlStr = url.toString();
        
		log.debug("Start : sample" + urlStr);
		log.debug("method" + method);

		httpMethod=null;
		
        HTTPSampleResult res= new HTTPSampleResult();
		res.setSampleLabel(urlStr); //was anyway reset as below

		res.sampleStart(); // Count the retries as well in the time

        try
        {
            HttpConnection connection = setupConnection(url, method, res);
            	
            if (method.equals(HTTPSampler2.POST))
            {
                sendPostData(httpMethod);
            }

            httpMethod.execute(httpState, connection);

            if (httpMethod.getStatusCode() == HttpStatus.SC_OK) {
            } else {
                System.err.println("Unexpected failure: " + httpMethod.getStatusLine().toString());
            }

            // Request sent. Now get the response:
            byte[] responseData= httpMethod.getResponseBody();

            res.sampleEnd();
            // Done with the sampling proper.

            // Now collect the results into the HTTPSampleResult:

            res.setResponseData(responseData);

            int errorLevel= httpMethod.getStatusCode();
            res.setResponseCode(Integer.toString(errorLevel));
            res.setSuccessful(200 <= errorLevel && errorLevel <= 399);

            res.setResponseMessage(httpMethod.getStatusText());

            String ct= httpMethod.getResponseHeader("Content-type").getValue();
            res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
            if (ct != null)
            {
            	// Extract charset and store as DataEncoding
            	// TODO do we need process http-equiv META tags, e.g.:
            	// <META http-equiv="content-type" content="text/html; charset=foobar">
            	// or can we leave that to the renderer ?
            	String de=ct.toLowerCase();
            	final String cs="charset=";
            	int cset= de.indexOf(cs);
            	if (cset >= 0)
            	{
                	res.setDataEncoding(de.substring(cset+cs.length()));
            	}
	           	if (ct.startsWith("image/"))
	            {
	                res.setDataType(HTTPSampleResult.BINARY);
	            }
	            else
	            {
	                res.setDataType(HTTPSampleResult.TEXT);
	            }
            }

            res.setResponseHeaders(getResponseHeaders(httpMethod));
            if (res.isRedirect())
            {
                res.setRedirectLocation(httpMethod.getResponseHeader("Location").getValue());
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpState, getCookieManager());

            // Follow redirects and download page resources if appropriate:
            if (!areFollowingRedirect)
            {
                boolean didFollowRedirects= false;
                if (res.isRedirect())
                {
                    log.debug("Location set to - " + res.getRedirectLocation());
                    
                    if (getFollowRedirects())
                    {
                        res= followRedirects(res, frameDepth);
                        didFollowRedirects= true;
                    }
                }

                if (isImageParser()
                    && res.getDataType().equals(HTTPSampleResult.TEXT)
                    && res.isSuccessful())
                {
                    if (frameDepth > MAX_FRAME_DEPTH)
                    {
                        res.addSubResult(
                            errorResult(
                                new Exception("Maximum frame/iframe nesting depth exceeded."),
                                null,
                                0));
                    }
                    else
                    {
                        // If we followed redirects, we already have a container:
                        boolean createContainerResults= !didFollowRedirects;

                        res=
                            downloadPageResources(
                                res,
                                createContainerResults,
                                frameDepth);
                    }
                }
            }

            log.debug("End : sample");
            if (httpMethod != null) httpMethod.releaseConnection();
            return res;
        }
        catch (IOException e)
        {
        	res.sampleEnd();
            return errorResult(e, url.toString(), res.getTime());
        }
        finally
        {
            if (httpMethod != null) httpMethod.releaseConnection();
        }
    }

    /**
     * Iteratively download the redirect targets of a redirect response.
     * <p>
     * The returned result will contain one subsample for each request issued,
     * including the original one that was passed in. It will be an
     * HTTPSampleResult that should mostly look as if the final destination
     * of the redirect chain had been obtained in a single shot.
     * 
     * @param res result of the initial request - must be a redirect response
     * @param frameDepth    Depth of this target in the frame structure.
     *                      Used only to prevent infinite recursion.
     * @return "Container" result with one subsample per request issued
     */
    private HTTPSampleResult followRedirects(
        HTTPSampleResult res,
        int frameDepth)
    {
        HTTPSampleResult totalRes= new HTTPSampleResult(res);
        HTTPSampleResult lastRes= res;

        int redirect;
        for (redirect= 0; redirect < MAX_REDIRECTS; redirect++)
        {
            String location= encodeSpaces(lastRes.getRedirectLocation());
                // Browsers seem to tolerate Location headers with spaces,
                // replacing them automatically with %20. We want to emulate
                // this behaviour.
            try
            {
                lastRes=
                    sample(
                        new URL(lastRes.getURL(), location),
                        HTTPSampler2.GET,
                        true,
                        frameDepth);
            }
            catch (MalformedURLException e)
            {
                lastRes= errorResult(e, location, 0);
            }
            totalRes.addSubResult(lastRes);

            if (!lastRes.isRedirect())
            {
                break;
            }
        }
        if (redirect >= MAX_REDIRECTS)
        {
            lastRes=
                errorResult(
                    new IOException("Exceeeded maximum number of redirects: "+MAX_REDIRECTS),
                    null,
                    0);
            totalRes.addSubResult(lastRes);
        }

        // Now populate the any totalRes fields that need to
        // come from lastRes:
        totalRes.setSampleLabel(
            totalRes.getSampleLabel() + "->" + lastRes.getSampleLabel());
        // The following three can be discussed: should they be from the
        // first request or from the final one? I chose to do it this way
        // because that's what browsers do: they show the final URL of the
        // redirect chain in the location field. 
        totalRes.setURL(lastRes.getURL());
        totalRes.setHTTPMethod(lastRes.getHTTPMethod());
        totalRes.setRequestHeaders(lastRes.getRequestHeaders());

        totalRes.setResponseData(lastRes.getResponseData());
        totalRes.setResponseCode(lastRes.getResponseCode());
        totalRes.setSuccessful(lastRes.isSuccessful());
        totalRes.setResponseMessage(lastRes.getResponseMessage());
        totalRes.setDataType(lastRes.getDataType());
        totalRes.setResponseHeaders(lastRes.getResponseHeaders());
        return totalRes;
    }

    /**
     * Download the resources of an HTML page.
     * <p>
     * If createContainerResult is true, the returned result will contain one 
     * subsample for each request issued, including the original one that was 
     * passed in. It will otherwise look exactly like that original one.
     * <p>
     * If createContainerResult is false, one subsample will be added to the
     * provided result for each requests issued.
     * 
     * @param res           result of the initial request - must contain an HTML
     *                      response
     * @param createContainerResult whether to create a "container" or just
     *                      use the provided <code>res</code> for that purpose
     * @param frameDepth    Depth of this target in the frame structure.
     *                      Used only to prevent infinite recursion.
     * @return              "Container" result with one subsample per request
     *                      issued
     */
    private HTTPSampleResult downloadPageResources(
        HTTPSampleResult res,
        boolean createContainerResult,
        int frameDepth)
    {
        Iterator urls= null;
        try
        {
            urls=
                HTMLParser.getParser().getEmbeddedResourceURLs(
                    res.getResponseData(),
                    res.getURL());
        }
        catch (HTMLParseException e)
        {
            // Don't break the world just because this failed:
            res.addSubResult(errorResult(e, null, 0));
            res.setSuccessful(false);
        }

        // Iterate through the URLs and download each image:
        if (urls != null && urls.hasNext())
        {
            if (createContainerResult)
            {
                res= new HTTPSampleResult(res);
            }

            while (urls.hasNext())
            {
                Object binURL= urls.next();
                try
                {
                    HTTPSampleResult binRes=
                        sample(
                            (URL)binURL,
                            HTTPSampler2.GET,
                            false,
                            frameDepth + 1);
                    res.addSubResult(binRes);
                    res.setSuccessful(
                        res.isSuccessful() && binRes.isSuccessful());
                }
                catch (ClassCastException e)
                {
                    res.addSubResult(
                        errorResult(
                            new Exception(binURL + " is not a correct URI"),
                            null,
                            0));
                    res.setSuccessful(false);
                    continue;
                }
            }
        }
        return res;
    }

//    protected void disconnect(HttpURLConnection conn)
//    {
//        if (conn != null)
//        {
//            String connection= conn.getHeaderField("Connection");
//            String protocol= conn.getHeaderField(0);
//            if ((connection == null && (protocol == null || !protocol.startsWith("HTTP/1.1")))
//                || (connection != null && connection.equalsIgnoreCase("close")))
//            {
//                conn.disconnect();
//            }
//        }
//    }
//
    /**
     * From the <code>HttpURLConnection</code>, store all the "set-cookie"
     * key-pair values in the cookieManager of the <code>UrlConfig</code>.
     *
     * @param conn          <code>HttpUrlConnection</code> which represents the
     *                      URL request
     * @param u             <code>URL</code> of the URL request
     * @param cookieManager the <code>CookieManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     */
    private void saveConnectionCookies(
        HttpState state,
        CookieManager cookieManager)
    {
        if (cookieManager != null)
        {
        	org.apache.commons.httpclient.Cookie [] c = state.getCookies();
            for (int i= 0; i < c.length ; i++)
            {
                   cookieManager.add(
                   		new org.apache.jmeter.protocol.http.control.
						Cookie(c[i].getName(),
								c[i].getValue(),
								c[i].getDomain(),
								c[i].getPath(),
								c[i].getSecure(),
								c[i].getExpiryDate().getTime()
							  )
						);
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

    public void threadStarted()
	{
    	log.info("Thread Started");
    }
    
    public void threadFinished()
	{
    	log.info("Thread Finished");
    	if (httpConn != null) httpConn.close();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static class Test extends junit.framework.TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testArgumentWithoutEquals() throws Exception
        {
            HTTPSampler2 sampler= new HTTPSampler2();
            sampler.setProtocol("http");
            sampler.setMethod(HTTPSampler2.GET);
            sampler.setPath("/index.html?pear");
            sampler.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?pear",
                sampler.getUrl().toString());
        }

        public void testMakingUrl() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }
        public void testMakingUrl2() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1&p1=p2",
                config.getUrl().toString());
        }
        public void testMakingUrl3() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.POST);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?p1=p2",
                config.getUrl().toString());
        }

        // test cases for making Url, and exercise method
        // addArgument(String name,String value,String metadata)

        public void testMakingUrl4() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.addArgument("param1", "value1", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }
        public void testMakingUrl5() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.addArgument("param1", "", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=",
                config.getUrl().toString());
        }
        public void testMakingUrl6() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.addArgument("param1", "", "");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1",
                config.getUrl().toString());
        }

        // test cases for making Url, and exercise method
        // parseArguments(String queryString)

        public void testMakingUrl7() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.parseArguments("param1=value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }

        public void testMakingUrl8() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.parseArguments("param1=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=",
                config.getUrl().toString());
        }

        public void testMakingUrl9() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.parseArguments("param1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1",
                config.getUrl().toString());
        }

        public void testMakingUrl10() throws Exception
        {
            HTTPSampler2 config= new HTTPSampler2();
            config.setProtocol("http");
            config.setMethod(HTTPSampler2.GET);
            config.parseArguments("");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html",
                config.getUrl().toString());
        }
    }
}
