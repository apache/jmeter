/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.apache.jmeter.protocol.http.parser.HTMLParser;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
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
 * Common constants and methods for HTTP samplers
 * 
 */
public abstract class HTTPSamplerBase extends AbstractSampler implements TestListener {

    private static final Logger log = LoggingManager.getLoggerForClass();

	public static final int DEFAULT_HTTPS_PORT = 443;

	public static final int DEFAULT_HTTP_PORT = 80;

	public final static String ARGUMENTS = "HTTPsampler.Arguments"; // $NON-NLS-1$

	public final static String AUTH_MANAGER = "HTTPSampler.auth_manager"; // $NON-NLS-1$

	public final static String COOKIE_MANAGER = "HTTPSampler.cookie_manager"; // $NON-NLS-1$

	public final static String HEADER_MANAGER = "HTTPSampler.header_manager"; // $NON-NLS-1$

	public final static String MIMETYPE = "HTTPSampler.mimetype"; // $NON-NLS-1$

	public final static String DOMAIN = "HTTPSampler.domain"; // $NON-NLS-1$

	public final static String PORT = "HTTPSampler.port"; // $NON-NLS-1$

	public final static String METHOD = "HTTPSampler.method"; // $NON-NLS-1$

	public final static String PATH = "HTTPSampler.path"; // $NON-NLS-1$

	public final static String FOLLOW_REDIRECTS = "HTTPSampler.follow_redirects"; // $NON-NLS-1$

	public final static String AUTO_REDIRECTS = "HTTPSampler.auto_redirects"; // $NON-NLS-1$

	public final static String PROTOCOL = "HTTPSampler.protocol"; // $NON-NLS-1$

    public static final String PROTOCOL_HTTPS = "https"; // $NON-NLS-1$

    public final static String DEFAULT_PROTOCOL = "http"; // $NON-NLS-1$

	public final static String URL = "HTTPSampler.URL"; // $NON-NLS-1$

	public final static String POST = "POST"; // $NON-NLS-1$

	public final static String GET = "GET"; // $NON-NLS-1$

	public final static String USE_KEEPALIVE = "HTTPSampler.use_keepalive"; // $NON-NLS-1$

	public final static String FILE_NAME = "HTTPSampler.FILE_NAME"; // $NON-NLS-1$

	public final static String FILE_FIELD = "HTTPSampler.FILE_FIELD"; // $NON-NLS-1$

	public final static String FILE_DATA = "HTTPSampler.FILE_DATA"; // $NON-NLS-1$

	public final static String FILE_MIMETYPE = "HTTPSampler.FILE_MIMETYPE"; // $NON-NLS-1$

	public final static String CONTENT_TYPE = "HTTPSampler.CONTENT_TYPE"; // $NON-NLS-1$

	public final static String NORMAL_FORM = "normal_form"; // $NON-NLS-1$

	public final static String MULTIPART_FORM = "multipart_form"; // $NON-NLS-1$

	// public final static String ENCODED_PATH= "HTTPSampler.encoded_path";
	public final static String IMAGE_PARSER = "HTTPSampler.image_parser"; // $NON-NLS-1$

	public final static String MONITOR = "HTTPSampler.monitor"; // $NON-NLS-1$

	/** A number to indicate that the port has not been set. * */
	public static final int UNSPECIFIED_PORT = 0;

	boolean dynamicPath = false;

	protected final static String NON_HTTP_RESPONSE_CODE = "Non HTTP response code";

	protected final static String NON_HTTP_RESPONSE_MESSAGE = "Non HTTP response message";

    private static final String ARG_VAL_SEP = "="; // $NON-NLS-1$

    private static final String QRY_SEP = "&"; // $NON-NLS-1$

    private static final String QRY_PFX = "?"; // $NON-NLS-1$


    
	private static Pattern pattern;

	static {
		try {
			pattern = new Perl5Compiler().compile(" ", Perl5Compiler.READ_ONLY_MASK & Perl5Compiler.SINGLELINE_MASK);
		} catch (MalformedPatternException e) {
			log.error("Cant compile pattern.", e);
			throw new Error(e.toString()); // programming error -- bail out
		}
	}

	public HTTPSamplerBase() {
		setArguments(new Arguments());
	}

	public void setFileField(String value) {
		setProperty(FILE_FIELD, value);
	}

	public String getFileField() {
		return getPropertyAsString(FILE_FIELD);
	}

	public void setFilename(String value) {
		setProperty(FILE_NAME, value);
	}

	public String getFilename() {
		return getPropertyAsString(FILE_NAME);
	}

	public void setProtocol(String value) {
		setProperty(PROTOCOL, value.toLowerCase());
	}

	public String getProtocol() {
		String protocol = getPropertyAsString(PROTOCOL);
		if (protocol == null || protocol.equals("")) {
			return DEFAULT_PROTOCOL;
		} else {
			return protocol;
		}
	}

	/**
	 * Sets the Path attribute of the UrlConfig object Also calls parseArguments
	 * to extract and store any query arguments
	 * 
	 * @param path
	 *            The new Path value
	 */
	public void setPath(String path) {
		if (GET.equals(getMethod())) {
			int index = path.indexOf(QRY_PFX); // $NON-NLS-1$
			if (index > -1) {
				setProperty(PATH, path.substring(0, index));
				parseArguments(path.substring(index + 1));
			} else {
				setProperty(PATH, path);
			}
		} else {
			setProperty(PATH, path);
		}
	}

	public String getPath() {
		String p = getPropertyAsString(PATH);
		if (dynamicPath) {
			return encodeSpaces(p);
		}
		return p;
	}

	public void setFollowRedirects(boolean value) {
		setProperty(new BooleanProperty(FOLLOW_REDIRECTS, value));
	}

	public boolean getFollowRedirects() {
		return getPropertyAsBoolean(FOLLOW_REDIRECTS);
	}

	public void setMethod(String value) {
		setProperty(METHOD, value);
	}

	public String getMethod() {
		return getPropertyAsString(METHOD);
	}

	public void setUseKeepAlive(boolean value) {
		setProperty(new BooleanProperty(USE_KEEPALIVE, value));
	}

	public boolean getUseKeepAlive() {
		return getPropertyAsBoolean(USE_KEEPALIVE);
	}

	public void setMonitor(String value) {
		this.setProperty(MONITOR, value);
	}

	public String getMonitor() {
		return this.getPropertyAsString(MONITOR);
	}

	public boolean isMonitor() {
		return this.getPropertyAsBoolean(MONITOR);
	}

	public void addEncodedArgument(String name, String value, String metaData) {
		log.debug("adding argument: name: " + name + " value: " + value + " metaData: " + metaData);

		HTTPArgument arg = new HTTPArgument(name, value, metaData, true);

		if (arg.getName().equals(arg.getEncodedName()) && arg.getValue().equals(arg.getEncodedValue())) {
			arg.setAlwaysEncoded(false);
		}
		this.getArguments().addArgument(arg);
	}

	public void addArgument(String name, String value) {
		this.getArguments().addArgument(new HTTPArgument(name, value));
	}

	public void addArgument(String name, String value, String metadata) {
		this.getArguments().addArgument(new HTTPArgument(name, value, metadata));
	}

	public void addTestElement(TestElement el) {
		if (el instanceof CookieManager) {
			setCookieManager((CookieManager) el);
		} else if (el instanceof HeaderManager) {
			setHeaderManager((HeaderManager) el);
		} else if (el instanceof AuthManager) {
			setAuthManager((AuthManager) el);
		} else {
			super.addTestElement(el);
		}
	}

	public void setPort(int value) {
		setProperty(new IntegerProperty(PORT, value));
	}

	public int getPort() {
		int port = getPropertyAsInt(PORT);
		if (port == UNSPECIFIED_PORT) {
			if (PROTOCOL_HTTPS.equalsIgnoreCase(getProtocol())) {
				return DEFAULT_HTTPS_PORT;
			}
			return DEFAULT_HTTP_PORT;
		}
		return port;
	}

	public void setDomain(String value) {
		setProperty(DOMAIN, value);
	}

	public String getDomain() {
		return getPropertyAsString(DOMAIN);
	}

	public void setArguments(Arguments value) {
		setProperty(new TestElementProperty(ARGUMENTS, value));
	}

	public Arguments getArguments() {
		return (Arguments) getProperty(ARGUMENTS).getObjectValue();
	}

	public void setAuthManager(AuthManager value) {
		AuthManager mgr = getAuthManager();
		if (mgr != null) {
			log.warn("Existing Manager " + mgr.getName() + " superseded by " + value.getName());
		}
		setProperty(new TestElementProperty(AUTH_MANAGER, value));
	}

	public AuthManager getAuthManager() {
		return (AuthManager) getProperty(AUTH_MANAGER).getObjectValue();
	}

	public void setHeaderManager(HeaderManager value) {
		HeaderManager mgr = getHeaderManager();
		if (mgr != null) {
			log.warn("Existing Manager " + mgr.getName() + " superseded by " + value.getName());
		}
		setProperty(new TestElementProperty(HEADER_MANAGER, value));
	}

	public HeaderManager getHeaderManager() {
		return (HeaderManager) getProperty(HEADER_MANAGER).getObjectValue();
	}

	public void setCookieManager(CookieManager value) {
		CookieManager mgr = getCookieManager();
		if (mgr != null) {
			log.warn("Existing Manager " + mgr.getName() + " superseded by " + value.getName());
		}
		setProperty(new TestElementProperty(COOKIE_MANAGER, value));
	}

	public CookieManager getCookieManager() {
		return (CookieManager) getProperty(COOKIE_MANAGER).getObjectValue();
	}

	public void setMimetype(String value) {
		setProperty(MIMETYPE, value);
	}

	public String getMimetype() {
		return getPropertyAsString(MIMETYPE);
	}

	public boolean isImageParser() {
		return getPropertyAsBoolean(IMAGE_PARSER);
	}

	public void setImageParser(boolean parseImages) {
		setProperty(new BooleanProperty(IMAGE_PARSER, parseImages));
	}

	/**
	 * Obtain a result that will help inform the user that an error has occured
	 * during sampling, and how long it took to detect the error.
	 * 
	 * @param e
	 *            Exception representing the error.
	 * @param current
	 *            SampleResult
	 * @return a sampling result useful to inform the user about the exception.
	 */
	protected HTTPSampleResult errorResult(Throwable e, HTTPSampleResult res) {
		res.setSampleLabel("Error");
		res.setDataType(HTTPSampleResult.TEXT);
		ByteArrayOutputStream text = new ByteArrayOutputStream(200);
		e.printStackTrace(new PrintStream(text));
		res.setResponseData(text.toByteArray());
		res.setResponseCode(NON_HTTP_RESPONSE_CODE);
		res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE);
		res.setSuccessful(false);
		res.setMonitor(this.isMonitor());
		return res;
	}

	/**
	 * Get the URL, built from its component parts.
	 * 
	 * @return The URL to be requested by this sampler.
	 * @throws MalformedURLException
	 */
	public URL getUrl() throws MalformedURLException {
		String pathAndQuery = null;
		if (this.getMethod().equals(GET) && getQueryString().length() > 0) {
			if (this.getPath().indexOf(QRY_PFX) > -1) {
				pathAndQuery = this.getPath() + QRY_SEP + getQueryString();
			} else {
				pathAndQuery = this.getPath() + QRY_PFX + getQueryString();
			}
		} else {
			pathAndQuery = this.getPath();
		}
		if (!pathAndQuery.startsWith("/")) { // $NON-NLS-1$
			pathAndQuery = "/" + pathAndQuery; // $NON-NLS-1$
		}
		if (getPort() == UNSPECIFIED_PORT || getPort() == DEFAULT_HTTP_PORT) {
			return new URL(getProtocol(), getDomain(), pathAndQuery);
		} else {
			return new URL(getProtocol(), getPropertyAsString(DOMAIN), getPort(), pathAndQuery);
		}
	}

	/**
	 * Gets the QueryString attribute of the UrlConfig object.
	 * 
	 * @return the QueryString value
	 */
	public String getQueryString() {
		StringBuffer buf = new StringBuffer();
		PropertyIterator iter = getArguments().iterator();
		boolean first = true;
		while (iter.hasNext()) {
			HTTPArgument item = null;
			try {
				item = (HTTPArgument) iter.next().getObjectValue();
			} catch (ClassCastException e) {
				item = new HTTPArgument((Argument) iter.next().getObjectValue());
			}
			if (!first) {
				buf.append(QRY_SEP);
			} else {
				first = false;
			}
			buf.append(item.getEncodedName());
			if (item.getMetaData() == null) {
				buf.append(ARG_VAL_SEP); // $NON-NLS-1$
			} else {
				buf.append(item.getMetaData());
			}
			buf.append(item.getEncodedValue());
		}
		return buf.toString();
	}

	// Mark Walsh 2002-08-03, modified to also parse a parameter name value
	// string, where string contains only the parameter name and no equal sign.
	/**
	 * This method allows a proxy server to send over the raw text from a
	 * browser's output stream to be parsed and stored correctly into the
	 * UrlConfig object.
	 * 
	 * For each name found, addEncodedArgument() is called
	 * 
	 * @param queryString -
	 *            the query string
	 * 
	 */
	public void parseArguments(String queryString) {
		String[] args = JOrphanUtils.split(queryString, QRY_SEP); // $NON-NLS-1$
		for (int i = 0; i < args.length; i++) {
			// need to handle four cases: string contains name=value
			// string contains name=
			// string contains name
			// empty string
			// find end of parameter name
			int endOfNameIndex = 0;
			String metaData = ""; // records the existance of an equal sign
			if (args[i].indexOf(ARG_VAL_SEP) != -1) {
				// case of name=value, name=
				endOfNameIndex = args[i].indexOf(ARG_VAL_SEP);
				metaData = ARG_VAL_SEP;
			} else {
				metaData = "";
				if (args[i].length() > 0) {
					endOfNameIndex = args[i].length(); // case name
				} else {
					endOfNameIndex = 0; // case where name value string is empty
				}
			}
			// parse name
			String name = ""; // for empty string
			if (args[i].length() > 0) {
				// for non empty string
				name = args[i].substring(0, endOfNameIndex);
			}
			// parse value
			String value = "";
			if ((endOfNameIndex + 1) < args[i].length()) {
				value = args[i].substring(endOfNameIndex + 1, args[i].length());
			}
			if (name.length() > 0) {
				addEncodedArgument(name, value, metaData);
			}
		}
	}

	public String toString() {
		try {
			return this.getUrl().toString() + ((POST.equals(getMethod())) ? "\nQuery Data: " + getQueryString() : "");
		} catch (MalformedURLException e) {
			return "";
		}
	}

	/**
	 * Do a sampling and return its results.
	 * 
	 * @param e
	 *            <code>Entry</code> to be sampled
	 * @return results of the sampling
	 */
	public SampleResult sample(Entry e) {
		return sample();
	}

	/**
	 * Perform a sample, and return the results
	 * 
	 * @return results of the sampling
	 */
	public SampleResult sample() {
		SampleResult res = null;
		try {
			res = sample(getUrl(), getMethod(), false, 0);
			res.setSampleLabel(getName());
			return res;
		} catch (MalformedURLException e) {
			return errorResult(e, new HTTPSampleResult());
		}
	}

	protected abstract HTTPSampleResult sample(URL u, String s, boolean b, int i);

	private static ThreadLocal localMatcher = new ThreadLocal() {
		protected synchronized Object initialValue() {
			return new Perl5Matcher();
		}
	};

	private static Substitution spaceSub = new StringSubstitution("%20");

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
	 * @param res
	 *            result of the initial request - must contain an HTML response
	 * @param createContainerResult
	 *            whether to create a "container" or just use the provided
	 *            <code>res</code> for that purpose
	 * @param frameDepth
	 *            Depth of this target in the frame structure. Used only to
	 *            prevent infinite recursion.
	 * @return "Container" result with one subsample per request issued
	 */
	protected HTTPSampleResult downloadPageResources(HTTPSampleResult res, HTTPSampleResult container, int frameDepth) {
		Iterator urls = null;
		try {
			if (res.getContentType().toLowerCase().indexOf("text/html") != -1) { // $NON-NLS-1$
				urls = HTMLParser.getParser().getEmbeddedResourceURLs(res.getResponseData(), res.getURL());
			}
		} catch (HTMLParseException e) {
			// Don't break the world just because this failed:
			res.addSubResult(errorResult(e, res));
			res.setSuccessful(false);
		}

		// Iterate through the URLs and download each image:
		if (urls != null && urls.hasNext()) {
			if (container == null) {
				res = new HTTPSampleResult(res);
			} else {
				res = container;
			}

			while (urls.hasNext()) {
				Object binURL = urls.next();
				try {
					HTTPSampleResult binRes = sample((URL) binURL, GET, false, frameDepth + 1);
					res.addSubResult(binRes);
					res.setSuccessful(res.isSuccessful() && binRes.isSuccessful());
				} catch (ClassCastException e) {
					res.addSubResult(errorResult(new Exception(binURL + " is not a correct URI"), res));
					res.setSuccessful(false);
					continue;
				}
			}
		}
		return res;
	}

	protected String encodeSpaces(String path) {
		// TODO JDK1.4
		// this seems to be equivalent to path.replaceAll(" ","%20");
		// TODO move to JMeterUtils or jorphan.
		// unless we move to JDK1.4. (including the
		// 'pattern' initialization code earlier on)
		path = Util.substitute((Perl5Matcher) localMatcher.get(), pattern, spaceSub, path, Util.SUBSTITUTE_ALL);
		return path;
	}

	protected static final int MAX_REDIRECTS = JMeterUtils.getPropDefault("httpsampler.max_redirects", 5); // $NON-NLS-1$

	protected static final int MAX_FRAME_DEPTH = JMeterUtils.getPropDefault("httpsampler.max_frame_depth", 5); // $NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded()
	 */
	public void testEnded() {
		dynamicPath = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
	 */
	public void testEnded(String host) {
		testEnded();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
	public void testIterationStart(LoopIterationEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted()
	 */
	public void testStarted() {
		JMeterProperty pathP = getProperty(PATH);
		log.debug("path property is a " + pathP.getClass().getName());
		log.debug("path beginning value = " + pathP.getStringValue());
		if (pathP instanceof StringProperty && !"".equals(pathP.getStringValue())) {
			log.debug("Encoding spaces in path");
			pathP.setObjectValue(encodeSpaces(pathP.getStringValue()));
		} else {
			log.debug("setting dynamic path to true");
			dynamicPath = true;
		}
		log.debug("path ending value = " + pathP.getStringValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
	 */
	public void testStarted(String host) {
		testStarted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		HTTPSamplerBase base = (HTTPSamplerBase) super.clone();
		base.dynamicPath = dynamicPath;
		return base;
	}

	/**
	 * Iteratively download the redirect targets of a redirect response.
	 * <p>
	 * The returned result will contain one subsample for each request issued,
	 * including the original one that was passed in. It will be an
	 * HTTPSampleResult that should mostly look as if the final destination of
	 * the redirect chain had been obtained in a single shot.
	 * 
	 * @param res
	 *            result of the initial request - must be a redirect response
	 * @param frameDepth
	 *            Depth of this target in the frame structure. Used only to
	 *            prevent infinite recursion.
	 * @return "Container" result with one subsample per request issued
	 */
	protected HTTPSampleResult followRedirects(HTTPSampleResult res, int frameDepth) {
		HTTPSampleResult totalRes = new HTTPSampleResult(res);
		HTTPSampleResult lastRes = res;

		int redirect;
		for (redirect = 0; redirect < MAX_REDIRECTS; redirect++) {
			String location = encodeSpaces(lastRes.getRedirectLocation());
			// Browsers seem to tolerate Location headers with spaces,
			// replacing them automatically with %20. We want to emulate
			// this behaviour.
			try {
				lastRes = sample(new URL(lastRes.getURL(), location), GET, true, frameDepth);
			} catch (MalformedURLException e) {
				lastRes = errorResult(e, lastRes);
			}
			if (lastRes.getSubResults() != null && lastRes.getSubResults().length > 0) {
				SampleResult[] subs = lastRes.getSubResults();
				for (int i = 0; i < subs.length; i++) {
					totalRes.addSubResult(subs[i]);
				}
			} else
				totalRes.addSubResult(lastRes);

			if (!lastRes.isRedirect()) {
				break;
			}
		}
		if (redirect >= MAX_REDIRECTS) {
			lastRes = errorResult(new IOException("Exceeeded maximum number of redirects: " + MAX_REDIRECTS), lastRes);
			totalRes.addSubResult(lastRes);
		}

		// Now populate the any totalRes fields that need to
		// come from lastRes:
		totalRes.setSampleLabel(totalRes.getSampleLabel() + "->" + lastRes.getSampleLabel());
		// The following three can be discussed: should they be from the
		// first request or from the final one? I chose to do it this way
		// because that's what browsers do: they show the final URL of the
		// redirect chain in the location field.
		totalRes.setURL(lastRes.getURL());
		totalRes.setHTTPMethod(lastRes.getHTTPMethod());
		totalRes.setQueryString(lastRes.getQueryString());
		totalRes.setRequestHeaders(lastRes.getRequestHeaders());

		totalRes.setResponseData(lastRes.getResponseData());
		totalRes.setResponseCode(lastRes.getResponseCode());
		totalRes.setSuccessful(lastRes.isSuccessful());
		totalRes.setResponseMessage(lastRes.getResponseMessage());
		totalRes.setDataType(lastRes.getDataType());
		totalRes.setResponseHeaders(lastRes.getResponseHeaders());
        totalRes.setContentType(lastRes.getContentType());
        totalRes.setDataEncoding(lastRes.getDataEncoding());
		return totalRes;
	}

	/**
	 * Follow redirects and download page resources if appropriate. this works,
	 * but the container stuff here is what's doing it. followRedirects() is
	 * actually doing the work to make sure we have only one container to make
	 * this work more naturally, I think this method - sample() - needs to take
	 * an HTTPSamplerResult container parameter instead of a
	 * boolean:areFollowingRedirect.
	 * 
	 * @param areFollowingRedirect
	 * @param frameDepth
	 * @param res
	 * @return
	 */
	protected HTTPSampleResult resultProcessing(boolean areFollowingRedirect, int frameDepth, HTTPSampleResult res) {
		if (!areFollowingRedirect) {
			if (res.isRedirect()) {
				log.debug("Location set to - " + res.getRedirectLocation());

				if (getFollowRedirects()) {
					res = followRedirects(res, frameDepth);
					areFollowingRedirect = true;
				}
			}
		}
		if (isImageParser() && (HTTPSampleResult.TEXT).equals(res.getDataType()) && res.isSuccessful()) {
			if (frameDepth > MAX_FRAME_DEPTH) {
				res.addSubResult(errorResult(new Exception("Maximum frame/iframe nesting depth exceeded."), res));
			} else {
				// If we followed redirects, we already have a container:
				HTTPSampleResult container = (HTTPSampleResult) (areFollowingRedirect ? res.getParent() : res);

				res = downloadPageResources(res, container, frameDepth);
			}
		}
		return res;
	}
}
