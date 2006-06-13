// $Header$
/*
 * ====================================================================
 * Copyright 2002-2004 The Apache Software Foundation.
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

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.
package org.htmlparser;

//////////////////
// Java Imports //
//////////////////
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.htmlparser.parserHelper.ParserHelper;
import org.htmlparser.parserHelper.TagParser;
import org.htmlparser.scanners.AppletScanner;
import org.htmlparser.scanners.BodyScanner;
import org.htmlparser.scanners.BulletListScanner;
import org.htmlparser.scanners.DivScanner;
import org.htmlparser.scanners.DoctypeScanner;
import org.htmlparser.scanners.FormScanner;
import org.htmlparser.scanners.FrameSetScanner;
import org.htmlparser.scanners.HeadScanner;
import org.htmlparser.scanners.HtmlScanner;
import org.htmlparser.scanners.JspScanner;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.scanners.MetaTagScanner;
import org.htmlparser.scanners.ScriptScanner;
import org.htmlparser.scanners.StyleScanner;
import org.htmlparser.scanners.TableScanner;
import org.htmlparser.scanners.TagScanner;
import org.htmlparser.scanners.TitleScanner;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.IteratorImpl;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;
import org.htmlparser.visitors.NodeVisitor;

/**
 * This is the class that the user will use, either to get an iterator into the
 * html page or to directly parse the page and print the results <BR>
 * Typical usage of the parser is as follows : <BR>
 * [1] Create a parser object - passing the URL and a feedback object to the
 * parser<BR>
 * [2] Register the common scanners. See {@link #registerScanners()} <BR>
 * You wouldnt do this if you want to configure a custom lightweight parser. In
 * that case, you would add the scanners of your choice using
 * {@link #addScanner(TagScanner)}<BR>
 * [3] Enumerate through the elements from the parser object <BR>
 * It is important to note that the parsing occurs when you enumerate, ON
 * DEMAND. This is a thread-safe way, and you only get the control back after a
 * particular element is parsed and returned.
 * 
 * <BR>
 * Below is some sample code to parse Yahoo.com and print all the tags.
 * 
 * <pre>
 * Parser parser = new Parser(&quot;http://www.yahoo.com&quot;, new DefaultHTMLParserFeedback());
 * // In this example, we are registering all the common scanners
 * parser.registerScanners();
 * for (NodeIterator i = parser.elements(); e.hasMoreNodes();) {
 * 	Node node = i.nextNode();
 * 	node.print();
 * }
 * </pre>
 * 
 * Below is some sample code to parse Yahoo.com and print only the text
 * information. This scanning will run faster, as there are no scanners
 * registered here.
 * 
 * <pre>
 * Parser parser = new Parser(&quot;http://www.yahoo.com&quot;, new DefaultHTMLParserFeedback());
 * // In this example, none of the scanners need to be registered
 * // as a string node is not a tag to be scanned for.
 * for (NodeIterator i = parser.elements(); e.hasMoreNodes();) {
 * 	Node node = i.nextNode();
 * 	if (node instanceof StringNode) {
 * 		StringNode stringNode = (StringNode) node;
 * 		System.out.println(stringNode.getText());
 * 	}
 * }
 * </pre>
 * 
 * The above snippet will print out only the text contents in the html document.<br>
 * Here's another snippet that will only print out the link urls in a document.
 * This is an example of adding a link scanner.
 * 
 * <pre>
 * Parser parser = new Parser(&quot;http://www.yahoo.com&quot;, new DefaultHTMLParserFeedback());
 * parser.addScanner(new LinkScanner(&quot;-l&quot;));
 * for (NodeIterator i = parser.elements(); e.hasMoreNodes();) {
 * 	Node node = i.nextNode();
 * 	if (node instanceof LinkTag) {
 * 		LinkTag linkTag = (LinkTag) node;
 * 		System.out.println(linkTag.getLink());
 * 	}
 * }
 * </pre>
 * 
 * @see Parser#elements()
 */
public class Parser implements Serializable {
	// Please don't change the formatting of the version variables below.
	// This is done so as to facilitate ant script processing.

	/**
	 * The floating point version number.
	 */
	public final static double VERSION_NUMBER = 1.3;

	/**
	 * The type of version.
	 */
	public final static String VERSION_TYPE = "Release Build";

	/**
	 * The date of the version.
	 */
	public final static String VERSION_DATE = "May 25, 2003";

	/**
	 * The display version.
	 */
	public final static String VERSION_STRING = "" + VERSION_NUMBER + " (" + VERSION_TYPE + " " + VERSION_DATE + ")";

	// End of formatting

	/**
	 * The default charset. This should be <code>ISO-8859-1</code>, see RFC
	 * 2616 (http://www.ietf.org/rfc/rfc2616.txt?number=2616) section 3.7.1
	 * Another alias is "8859_1".
	 */
	protected static final String DEFAULT_CHARSET = "ISO-8859-1";

	/**
	 * Trigger for charset detection.
	 */
	protected static final String CHARSET_STRING = "charset";

	/**
	 * Feedback object.
	 */
	protected ParserFeedback feedback;

	/**
	 * The URL or filename to be parsed.
	 */
	protected String resourceLocn;

	/**
	 * The html reader associated with this parser.
	 */
	protected transient NodeReader reader;

	/**
	 * The list of scanners to apply at the top level.
	 */
	private Map scanners;

	/**
	 * The encoding being used to decode the connection input stream.
	 */
	protected String character_set;

	/**
	 * The source for HTML.
	 */
	protected transient URLConnection url_conn;

	/**
	 * The bytes extracted from the source.
	 */
	protected transient BufferedInputStream input;

	/**
	 * A quiet message sink. Use this for no feedback.
	 */
	public static ParserFeedback noFeedback = new DefaultParserFeedback(DefaultParserFeedback.QUIET);

	/**
	 * A verbose message sink. Use this for output on <code>System.out</code>.
	 */
	public static ParserFeedback stdout = new DefaultParserFeedback();

	private ParserHelper parserHelper = new ParserHelper();

	//
	// Static methods
	//

	/**
	 * @param lineSeparator
	 *            New Line separator to be used
	 */
	public static void setLineSeparator(String lineSeparator) {
		Node.setLineSeparator(lineSeparator);
	}

	/**
	 * Return the version string of this parser.
	 * 
	 * @return A string of the form:
	 * 
	 * <pre>
	 * &quot;[floating point number] ([build-type] [build-date])&quot;
	 * </pre>
	 */
	public static String getVersion() {
		return (VERSION_STRING);
	}

	/**
	 * Return the version number of this parser.
	 * 
	 * @return A floating point number, the whole number part is the major
	 *         version, and the fractional part is the minor version.
	 */
	public static double getVersionNumber() {
		return (VERSION_NUMBER);
	}

	//
	// Constructors
	//

	/**
	 * Zero argument constructor. The parser is in a safe but useless state. Set
	 * the reader or connection using setReader() or setConnection().
	 * 
	 * @see #setReader(NodeReader)
	 * @see #setConnection(URLConnection)
	 */
	public Parser() {
		setFeedback(null);
		setScanners(null);
		resourceLocn = null;
		reader = null;
		character_set = DEFAULT_CHARSET;
		url_conn = null;
		input = null;
		Tag.setTagParser(new TagParser(getFeedback()));
	}

	/**
	 * This constructor enables the construction of test cases, with readers
	 * associated with test string buffers. It can also be used with readers of
	 * the user's choice streaming data into the parser.<p/> <B>Important:</B>
	 * If you are using this constructor, and you would like to use the parser
	 * to parse multiple times (multiple calls to parser.elements()), you must
	 * ensure the following:<br>
	 * <ul>
	 * <li>Before the first parse, you must mark the reader for a length that
	 * you anticipate (the size of the stream).</li>
	 * <li>After the first parse, calls to elements() must be preceded by calls
	 * to :
	 * 
	 * <pre>
	 * parser.getReader().reset();
	 * </pre>
	 * 
	 * </li>
	 * </ul>
	 * 
	 * @param rd
	 *            The reader to draw characters from.
	 * @param fb
	 *            The object to use when information, warning and error messages
	 *            are produced. If <em>null</em> no feedback is provided.
	 */
	public Parser(NodeReader rd, ParserFeedback fb) {
		setFeedback(fb);
		setScanners(null);
		resourceLocn = null;
		reader = null;
		character_set = DEFAULT_CHARSET;
		url_conn = null;
		input = null;
		setReader(rd);
		Tag.setTagParser(new TagParser(feedback));
	}

	/**
	 * Constructor for custom HTTP access.
	 * 
	 * @param connection
	 *            A fully conditioned connection. The connect() method will be
	 *            called so it need not be connected yet.
	 * @param fb
	 *            The object to use for message communication.
	 */
	public Parser(URLConnection connection, ParserFeedback fb) throws ParserException {
		setFeedback(fb);
		setScanners(null);
		resourceLocn = null;
		reader = null;
		character_set = DEFAULT_CHARSET;
		url_conn = null;
		input = null;
		Tag.setTagParser(new TagParser(feedback));
		setConnection(connection);
	}

	/**
	 * Creates a Parser object with the location of the resource (URL or file)
	 * You would typically create a DefaultHTMLParserFeedback object and pass it
	 * in.
	 * 
	 * @param resourceLocn
	 *            Either the URL or the filename (autodetects). A standard HTTP
	 *            GET is performed to read the content of the URL.
	 * @param feedback
	 *            The HTMLParserFeedback object to use when information, warning
	 *            and error messages are produced. If <em>null</em> no
	 *            feedback is provided.
	 * @see #Parser(URLConnection,ParserFeedback)
	 */
	public Parser(String resourceLocn, ParserFeedback feedback) throws ParserException {
		this(ParserHelper.openConnection(resourceLocn, feedback), feedback);
	}

	/**
	 * Creates a Parser object with the location of the resource (URL or file).
	 * A DefaultHTMLParserFeedback object is used for feedback.
	 * 
	 * @param resourceLocn
	 *            Either the URL or the filename (autodetects).
	 */
	public Parser(String resourceLocn) throws ParserException {
		this(resourceLocn, stdout);
	}

	/**
	 * This constructor is present to enable users to plugin their own readers.
	 * A DefaultHTMLParserFeedback object is used for feedback. It can also be
	 * used with readers of the user's choice streaming data into the parser.<p/>
	 * <B>Important:</B> If you are using this constructor, and you would like
	 * to use the parser to parse multiple times (multiple calls to
	 * parser.elements()), you must ensure the following:<br>
	 * <ul>
	 * <li>Before the first parse, you must mark the reader for a length that
	 * you anticipate (the size of the stream).</li>
	 * <li>After the first parse, calls to elements() must be preceded by calls
	 * to :
	 * 
	 * <pre>
	 * parser.getReader().reset();
	 * </pre>
	 * 
	 * </li>
	 * 
	 * @param reader
	 *            The source for HTML to be parsed.
	 */
	public Parser(NodeReader reader) {
		this(reader, stdout);
	}

	/**
	 * Constructor for non-standard access. A DefaultHTMLParserFeedback object
	 * is used for feedback.
	 * 
	 * @param connection
	 *            A fully conditioned connection. The connect() method will be
	 *            called so it need not be connected yet.
	 * @see #Parser(URLConnection,ParserFeedback)
	 */
	public Parser(URLConnection connection) throws ParserException {
		this(connection, stdout);
	}

	//
	// Serialization support
	//

	private void writeObject(ObjectOutputStream out) throws IOException {
		if ((null == getConnection()) || /* redundant */
		(null == getURL()))
			if (null != getReader())
				;
		// commented out by Somik - why are we not allowed to serialize parsers
		// without url
		// throw new IOException ("can only serialize parsers with a URL");
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			// reopen the connection and create a reader which are transient
			// fields
			setURL(getURL());
		} catch (ParserException hpe) {
			throw new IOException(hpe.toString());
		}
	}

	//
	// Bean patterns
	//

	/**
	 * Set the connection for this parser. This method sets four of the fields
	 * in the parser object; <code>resourceLocn</code>, <code>url_conn</code>,
	 * <code>character_set</code> and <code>reader</code>. It does not
	 * adjust the <code>scanners</code> list or <code>feedback</code>
	 * object. The four fields are set atomicly by this method, either they are
	 * all set or none of them is set. Trying to set the connection to null is a
	 * noop.
	 * 
	 * @param connection
	 *            A fully conditioned connection. The connect() method will be
	 *            called so it need not be connected yet.
	 * @exception ParserException
	 *                if the character set specified in the HTTP header is not
	 *                supported, or an i/o exception occurs creating the reader.
	 */
	public void setConnection(URLConnection connection) throws ParserException {
		String res;
		NodeReader rd;
		String chs;
		URLConnection con;

		if (null != connection) {
			res = getURL();
			rd = getReader();
			chs = getEncoding();
			con = getConnection();
			try {
				resourceLocn = connection.getURL().toExternalForm();
				url_conn = connection;
				url_conn.connect();
				character_set = getCharacterSet(url_conn);
				createReader();
			} catch (IOException ioe) {
				String msg = "setConnection() : Error in opening a connection to "
						+ connection.getURL().toExternalForm();
				ParserException ex = new ParserException(msg, ioe);
				feedback.error(msg, ex);
				resourceLocn = res;
				url_conn = con;
				character_set = chs;
				reader = rd;
				throw ex;
			}
		}
	}

	/**
	 * Return the current connection.
	 * 
	 * @return The connection either created by the parser or passed into this
	 *         parser via <code>setConnection</code>.
	 * @see #setConnection(URLConnection)
	 */
	public URLConnection getConnection() {
		return (url_conn);
	}

	/**
	 * Set the URL for this parser. This method sets four of the fields in the
	 * parser object; <code>resourceLocn</code>, <code>url_conn</code>,
	 * <code>character_set</code> and <code>reader</code>. It does not
	 * adjust the <code>scanners</code> list or <code>feedback</code>
	 * object.Trying to set the url to null or an empty string is a noop.
	 * 
	 * @see #setConnection(URLConnection)
	 */
	public void setURL(String url) throws ParserException {
		if ((null != url) && !"".equals(url))
			setConnection(ParserHelper.openConnection(url, getFeedback()));
	}

	/**
	 * Return the current URL being parsed.
	 * 
	 * @return The url passed into the constructor or the file name passed to
	 *         the constructor modified to be a URL.
	 */
	public String getURL() {
		return (resourceLocn);
	}

	/**
	 * Set the encoding for this parser. If there is no connection
	 * (getConnection() returns null) it simply sets the character set name
	 * stored in the parser (Note: the reader object which must have been set in
	 * the constructor or by <code>setReader()</code>, may or may not be
	 * using this character set). Otherwise (getConnection() doesn't return
	 * null) it does this by reopening the input stream of the connection and
	 * creating a reader that uses this character set. In this case, this method
	 * sets two of the fields in the parser object; <code>character_set</code>
	 * and <code>reader</code>. It does not adjust <code>resourceLocn</code>,
	 * <code>url_conn</code>, <code>scanners</code> or
	 * <code>feedback</code>. The two fields are set atomicly by this method,
	 * either they are both set or none of them is set. Trying to set the
	 * encoding to null or an empty string is a noop.
	 * 
	 * @exception ParserException
	 *                If the opening of the reader
	 */
	public void setEncoding(String encoding) throws ParserException {
		String chs;
		NodeReader rd;
		BufferedInputStream in;

		if ((null != encoding) && !"".equals(encoding))
			if (null == getConnection())
				character_set = encoding;
			else {
				rd = getReader();
				chs = getEncoding();
				in = input;
				try {
					character_set = encoding;
					recreateReader();
				} catch (IOException ioe) {
					String msg = "setEncoding() : Error in opening a connection to "
							+ getConnection().getURL().toExternalForm();
					ParserException ex = new ParserException(msg, ioe);
					feedback.error(msg, ex);
					character_set = chs;
					reader = rd;
					input = in;
					throw ex;
				}
			}
	}

	/**
	 * The current encoding. This item is et from the HTTP header but may be
	 * overridden by meta tags in the head, so this may change after the head
	 * has been parsed.
	 */
	public String getEncoding() {
		return (character_set);
	}

	/**
	 * Set the reader for this parser. This method sets four of the fields in
	 * the parser object; <code>resourceLocn</code>, <code>url_conn</code>,
	 * <code>character_set</code> and <code>reader</code>. It does not
	 * adjust the <code>scanners</code> list or <code>feedback</code>
	 * object. The <code>url_conn</code> is set to null since this cannot be
	 * determined from the reader. The <code>character_set</code> is set to
	 * the default character set since this cannot be determined from the
	 * reader. Trying to set the reader to <code>null</code> is a noop.
	 * 
	 * @param rd
	 *            The reader object to use. This reader will be bound to this
	 *            parser after this call.
	 */
	public void setReader(NodeReader rd) {
		if (null != rd) {
			resourceLocn = rd.getURL();
			reader = rd;
			character_set = DEFAULT_CHARSET;
			url_conn = null;
			reader.setParser(this);
		}
	}

	/**
	 * Returns the reader associated with the parser
	 * 
	 * @return NodeReader
	 */
	public NodeReader getReader() {
		return reader;
	}

	/**
	 * Get the number of scanners registered currently in the scanner.
	 * 
	 * @return int number of scanners registered
	 */
	public int getNumScanners() {
		return scanners.size();
	}

	/**
	 * This method is to be used to change the set of scanners in the current
	 * parser.
	 * 
	 * @param newScanners
	 *            Vector holding scanner objects to be used during the parsing
	 *            process.
	 */
	public void setScanners(Map newScanners) {
		scanners = (null == newScanners) ? new HashMap() : newScanners;
	}

	/**
	 * Get an enumeration of scanners registered currently in the parser
	 * 
	 * @return Enumeration of scanners currently registered in the parser
	 */
	public Map getScanners() {
		return scanners;
	}

	/**
	 * Sets the feedback object used in scanning.
	 * 
	 * @param fb
	 *            The new feedback object to use.
	 */
	public void setFeedback(ParserFeedback fb) {
		feedback = (null == fb) ? noFeedback : fb;
	}

	/**
	 * Returns the feedback.
	 * 
	 * @return HTMLParserFeedback
	 */
	public ParserFeedback getFeedback() {
		return feedback;
	}

	//
	// Internal methods
	//

	/**
	 * Open a stream reader on the <code>InputStream</code>. Revise the
	 * character set to it's default value if an
	 * <code>UnsupportedEncodingException</code> is thrown.
	 * 
	 * @exception UnsupportedEncodingException
	 *                in the unlikely event that the default character set is
	 *                not supported on this platform.
	 */
	protected InputStreamReader createInputStreamReader() throws UnsupportedEncodingException {
		InputStreamReader ret;

		try {
			ret = new InputStreamReader(input, character_set);
		} catch (UnsupportedEncodingException uee) {
			StringBuffer msg;
			String message;

			msg = new StringBuffer(1024);
			msg.append(url_conn.getURL().toExternalForm());
			msg.append(" has an encoding (");
			msg.append(character_set);
			msg.append(") which is not supported, using ");
			msg.append(DEFAULT_CHARSET);
			message = msg.toString();
			feedback.warning(message);
			character_set = DEFAULT_CHARSET;
			ret = new InputStreamReader(input, character_set);
		}

		return (ret);
	}

	/**
	 * Create a new reader for the URLConnection object. The current character
	 * set is used to transform the input stream into a character reader.
	 * 
	 * @exception IOException
	 *                if there is a problem constructing the reader.
	 * @see #createInputStreamReader()
	 * @see #getEncoding()
	 */
	protected void createReader() throws IOException {
		InputStream stream;
		InputStreamReader in;

		stream = url_conn.getInputStream();
		input = new BufferedInputStream(stream);
		input.mark(Integer.MAX_VALUE);
		in = createInputStreamReader();
		reader = new NodeReader(in, resourceLocn);
		reader.setParser(this);
	}

	/**
	 * Create a new reader for the URLConnection object but reuse the input
	 * stream. The current character set is used to transform the input stream
	 * into a character reader. Defaults to <code>createReader()</code> if
	 * there is no existing input stream.
	 * 
	 * @exception IOException
	 *                if there is a problem constructing the reader.
	 * @see #createReader()
	 * @see #createInputStreamReader()
	 * @see #getEncoding()
	 */
	protected void recreateReader() throws IOException {
		InputStreamReader in;

		if (null == input)
			createReader();
		else {
			input.reset();
			input.mark(Integer.MAX_VALUE);
			in = createInputStreamReader();
			reader = new NodeReader(in, resourceLocn);
			reader.setParser(this);
		}
	}

	/**
	 * Try and extract the character set from the HTTP header.
	 * 
	 * @param connection
	 *            The connection with the charset info.
	 * @return The character set name to use for this HTML page.
	 */
	protected String getCharacterSet(URLConnection connection) {
		final String field = "Content-Type";

		String string;
		String ret;

		ret = DEFAULT_CHARSET;
		string = connection.getHeaderField(field);
		if (null != string)
			ret = getCharset(string);

		return (ret);
	}

	/**
	 * Get a CharacterSet name corresponding to a charset parameter.
	 * 
	 * @param content
	 *            A text line of the form:
	 * 
	 * <pre>
	 * 
	 *  text/html; charset=Shift_JIS
	 *  
	 * </pre>
	 * 
	 * which is applicable both to the HTTP header field Content-Type
	 *            and the meta tag http-equiv="Content-Type". Note this method
	 *            also handles non-compliant quoted charset directives such as:
	 * 
	 * <pre>
	 * 
	 *  text/html; charset=&quot;UTF-8&quot;
	 *  
	 * </pre>
	 * 
	 * and
	 * 
	 * <pre>
	 * 
	 *  text/html; charset='UTF-8'
	 *  
	 * </pre>
	 * 
	 * @return The character set name to use when reading the input stream. For
	 *         JDKs that have the Charset class this is qualified by passing the
	 *         name to findCharset() to render it into canonical form. If the
	 *         charset parameter is not found in the given string, the default
	 *         character set is returned.
	 * @see ParserHelper#findCharset
	 * @see #DEFAULT_CHARSET
	 */
	protected String getCharset(String content) {
		int index;
		String ret;

		ret = DEFAULT_CHARSET;
		if (null != content) {
			index = content.indexOf(CHARSET_STRING);

			if (index != -1) {
				content = content.substring(index + CHARSET_STRING.length()).trim();
				if (content.startsWith("=")) {
					content = content.substring(1).trim();
					index = content.indexOf(";");
					if (index != -1)
						content = content.substring(0, index);

					// remove any double quotes from around charset string
					if (content.startsWith("\"") && content.endsWith("\"") && (1 < content.length()))
						content = content.substring(1, content.length() - 1);

					// remove any single quote from around charset string
					if (content.startsWith("'") && content.endsWith("'") && (1 < content.length()))
						content = content.substring(1, content.length() - 1);

					ret = ParserHelper.findCharset(content, ret);
					// Charset names are not case-sensitive;
					// that is, case is always ignored when comparing charset
					// names.
					if (!ret.equalsIgnoreCase(content)) {
						feedback.info("detected charset \"" + content + "\", using \"" + ret + "\"");
					}
				}
			}
		}

		return (ret);
	}

	//
	// Public methods
	//

	/**
	 * Add a new Tag Scanner. In typical situations where you require a
	 * no-frills parser, use the registerScanners() method to add the most
	 * common parsers. But when you wish to either compose a parser with only
	 * certain scanners registered, use this method. It is advantageous to
	 * register only the scanners you want, in order to achieve faster parsing
	 * speed. This method would also be of use when you have developed custom
	 * scanners, and need to register them into the parser.
	 * 
	 * @param scanner
	 *            TagScanner object (or derivative) to be added to the list of
	 *            registered scanners
	 */
	public void addScanner(TagScanner scanner) {
		String ids[] = scanner.getID();
		for (int i = 0; i < ids.length; i++) {
			scanners.put(ids[i], scanner);
		}
		scanner.setFeedback(feedback);
	}

	/**
	 * Returns an iterator (enumeration) to the html nodes. Each node can be a
	 * tag/endtag/ string/link/image<br>
	 * This is perhaps the most important method of this class. In typical
	 * situations, you will need to use the parser like this :
	 * 
	 * <pre>
	 * 
	 *  Parser parser = new Parser(&quot;http://www.yahoo.com&quot;);
	 *  parser.registerScanners();
	 *  for (NodeIterator i = parser.elements();i.hasMoreElements();) {
	 *     Node node = i.nextHTMLNode();
	 *     if (node instanceof StringNode) {
	 *       // Downcasting to StringNode
	 *       StringNode stringNode = (StringNode)node;
	 *       // Do whatever processing you want with the string node
	 *       System.out.println(stringNode.getText());
	 *     }
	 *     // Check for the node or tag that you want
	 *     if (node instanceof ...) {
	 *       // Downcast, and process
	 *     }
	 *  }
	 *  
	 * </pre>
	 */
	public NodeIterator elements() throws ParserException {
		boolean remove_scanner;
		Node node;
		MetaTag meta;
		String httpEquiv;
		String charset;
		boolean restart;
		EndTag end;
		IteratorImpl ret;

		remove_scanner = false;
		restart = false;
		ret = new IteratorImpl(reader, resourceLocn, feedback);
		ret = createIteratorImpl(remove_scanner, ret);

		return (ret);
	}

	public IteratorImpl createIteratorImpl(boolean remove_scanner, IteratorImpl ret) throws ParserException {
		Node node;
		MetaTag meta;
		String httpEquiv;
		String charset;
		EndTag end;
		if (null != url_conn)
			try {
				if (null == scanners.get("-m")) {
					addScanner(new MetaTagScanner("-m"));
					remove_scanner = true;
				}

				/* pre-read up to </HEAD> looking for charset directive */
				while (null != (node = ret.peek())) {
					if (node instanceof MetaTag) { // check for charset on
													// Content-Type
						meta = (MetaTag) node;
						httpEquiv = meta.getAttribute("HTTP-EQUIV");
						if ("Content-Type".equalsIgnoreCase(httpEquiv)) {
							charset = getCharset(meta.getAttribute("CONTENT"));
							if (!charset.equalsIgnoreCase(character_set)) { // oops,
																			// different
																			// character
																			// set,
																			// restart
								character_set = charset;
								recreateReader();
								ret = new IteratorImpl(reader, resourceLocn, feedback);
							}
							// once we see the Content-Type meta tag we're
							// finished the pre-read
							break;
						}
					} else if (node instanceof EndTag) {
						end = (EndTag) node;
						if (end.getTagName().equalsIgnoreCase("HEAD"))
							// or, once we see the </HEAD> tag we're finished
							// the pre-read
							break;
					}
				}
			} catch (UnsupportedEncodingException uee) {
				String msg = "elements() : The content of " + url_conn.getURL().toExternalForm()
						+ " has an encoding which is not supported";
				ParserException ex = new ParserException(msg, uee);
				feedback.error(msg, ex);
				throw ex;
			} catch (IOException ioe) {
				String msg = "elements() : Error in opening a connection to " + url_conn.getURL().toExternalForm();
				ParserException ex = new ParserException(msg, ioe);
				feedback.error(msg, ex);
				throw ex;
			} finally {
				if (remove_scanner)
					scanners.remove("-m");
			}
		return ret;
	}

	/**
	 * Flush the current scanners registered. The registered scanners list
	 * becomes empty with this call.
	 */
	public void flushScanners() {
		scanners = new Hashtable();
	}

	/**
	 * Return the scanner registered in the parser having the given id
	 * 
	 * @param id
	 *            The id of the requested scanner
	 * @return TagScanner The Tag Scanner
	 */
	public TagScanner getScanner(String id) {
		return (TagScanner) scanners.get(id);
	}

	/**
	 * Parse the given resource, using the filter provided
	 */
	public void parse(String filter) throws Exception {
		Node node;
		for (NodeIterator e = elements(); e.hasMoreNodes();) {
			node = e.nextNode();
			if (node != null) {
				if (filter == null)
					System.out.println(node.toString());
				else {
					// There is a filter. Find if the associated filter of this
					// node
					// matches the specified filter
					if (!(node instanceof Tag))
						continue;
					Tag tag = (Tag) node;
					TagScanner scanner = tag.getThisScanner();
					if (scanner == null)
						continue;

					String tagFilter = scanner.getFilter();
					if (tagFilter == null)
						continue;
					if (tagFilter.equals(filter))
						System.out.println(node.toString());
				}
			} else
				System.out.println("Node is null");
		}

	}

	/**
	 * This method should be invoked in order to register some common scanners.
	 * The scanners that get added are : <br>
	 * LinkScanner (filter key "-l")<br>
	 * HTMLImageScanner (filter key "-i")<br>
	 * HTMLScriptScanner (filter key "-s") <br>
	 * HTMLStyleScanner (filter key "-t") <br>
	 * HTMLJspScanner (filter key "-j") <br>
	 * HTMLAppletScanner (filter key "-a") <br>
	 * HTMLMetaTagScanner (filter key "-m") <br>
	 * HTMLTitleScanner (filter key "-t") <br>
	 * HTMLDoctypeScanner (filter key "-d") <br>
	 * HTMLFormScanner (filter key "-f") <br>
	 * HTMLFrameSetScanner(filter key "-r") <br>
	 * HTMLBaseHREFScanner(filter key "-b") <br>
	 * <br>
	 * Call this method after creating the Parser object. e.g. <BR>
	 * 
	 * <pre>
	 * Parser parser = new Parser(&quot;http://www.yahoo.com&quot;);
	 * parser.registerScanners();
	 * </pre>
	 */
	public void registerScanners() {
		if (scanners.size() > 0) {
			System.err.println("registerScanners() should be called first, when no other scanner has been registered.");
			System.err.println("Other scanners already exist, hence this method call wont have any effect");
			return;
		}
		LinkScanner linkScanner = new LinkScanner(LinkTag.LINK_TAG_FILTER);
		// Note - The BaseHREF and Image scanners share the same
		// link processor - internally linked up with the factory
		// method in the link scanner class
		addScanner(linkScanner);
		addScanner(linkScanner.createImageScanner(ImageTag.IMAGE_TAG_FILTER));
		addScanner(new ScriptScanner("-s"));
		addScanner(new StyleScanner("-t"));
		addScanner(new JspScanner("-j"));
		addScanner(new AppletScanner("-a"));
		addScanner(new MetaTagScanner("-m"));
		addScanner(new TitleScanner("-T"));
		addScanner(new DoctypeScanner("-d"));
		addScanner(new FormScanner("-f", this));
		addScanner(new FrameSetScanner("-r"));
		addScanner(linkScanner.createBaseHREFScanner("-b"));
		addScanner(new BulletListScanner("-bulletList", this));
		// addScanner(new SpanScanner("-p"));
		addScanner(new DivScanner("-div"));
		addScanner(new TableScanner(this));
	}

	/**
	 * Make a call to registerDomScanners(), instead of registerScanners(), when
	 * you are interested in retrieving a Dom representation of the html page.
	 * Upon parsing, you will receive an Html object - which will contain
	 * children, one of which would be the body. This is still evolving, and in
	 * future releases, you might see consolidation of Html - to provide you
	 * with methods to access the body and the head.
	 */
	public void registerDomScanners() {
		registerScanners();
		addScanner(new HtmlScanner());
		addScanner(new BodyScanner());
		addScanner(new HeadScanner());
	}

	/**
	 * Removes a specified scanner object. You can create an anonymous object as
	 * a parameter. This method will use the scanner's key and remove it from
	 * the registry of scanners. e.g.
	 * 
	 * <pre>
	 * removeScanner(new FormScanner(&quot;&quot;));
	 * </pre>
	 * 
	 * @param scanner
	 *            TagScanner object to be removed from the list of registered
	 *            scanners
	 */
	public void removeScanner(TagScanner scanner) {
		scanners.remove(scanner.getID()[0]);
	}

	/**
	 * The main program, which can be executed from the command line
	 */
	public static void main(String[] args) {
		System.out.println("HTMLParser v" + VERSION_STRING);
		if (args.length < 1 || args[0].equals("-help")) {
			System.out.println();
			System.out.println("Syntax : java -jar htmlparser.jar <resourceLocn/website> -l");
			System.out
					.println("   <resourceLocn> the name of the file to be parsed (with complete path if not in current directory)");
			System.out.println("   -l Show only the link tags extracted from the document");
			System.out.println("   -i Show only the image tags extracted from the document");
			System.out.println("   -s Show only the Javascript code extracted from the document");
			System.out.println("   -t Show only the Style code extracted from the document");
			System.out.println("   -a Show only the Applet tag extracted from the document");
			System.out.println("   -j Parse JSP tags");
			System.out.println("   -m Parse Meta tags");
			System.out.println("   -T Extract the Title");
			System.out.println("   -f Extract forms");
			System.out.println("   -r Extract frameset");
			System.out.println("   -help This screen");
			System.out.println();
			System.out.println("HTML Parser home page : http://htmlparser.sourceforge.net");
			System.out.println();
			System.out.println("Example : java -jar htmlparser.jar http://www.yahoo.com");
			System.out.println();
			System.out
					.println("If you have any doubts, please join the HTMLParser mailing list (user/developer) from the HTML Parser home page instead of mailing any of the contributors directly. You will be surprised with the quality of open source support. ");
			System.exit(-1);
		}
		try {
			if (args[0].indexOf("http") < 0) {
				File input = new File(args[0]);
				try {
					args[0] = input.toURL().toString();
					System.out.println("file converted to URL: " + args[0]);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			Parser parser = new Parser(args[0]);
			System.out.println("Parsing " + parser.getURL());
			parser.registerScanners();
			try {
				long start = System.currentTimeMillis();
				if (args.length == 2) {
					parser.parse(args[1]);
				} else
					parser.parse(null);
				System.out.println("Elapsed Time ms: " + (System.currentTimeMillis() - start));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	public void visitAllNodesWith(NodeVisitor visitor) throws ParserException {
		Node node;
		for (NodeIterator e = elements(); e.hasMoreNodes();) {
			node = e.nextNode();
			node.accept(visitor);
		}
		visitor.finishedParsing();
	}

	/**
	 * Initializes the parser with the given input HTML String.
	 * 
	 * @param inputHTML
	 *            the input HTML that is to be parsed.
	 */
	public void setInputHTML(String inputHTML) {
		if ("".equals(inputHTML)) {
			reader = new NodeReader(new StringReader(inputHTML), "");
		}
	}

	public Node[] extractAllNodesThatAre(Class nodeType) throws ParserException {
		NodeList nodeList = new NodeList();
		for (NodeIterator e = elements(); e.hasMoreNodes();) {
			e.nextNode().collectInto(nodeList, nodeType);
		}
		return nodeList.toNodeArray();
	}

	/**
	 * Creates the parser on an input string.
	 * 
	 * @param inputHTML
	 * @return Parser
	 */
	public static Parser createParser(String inputHTML) {
		NodeReader reader = new NodeReader(new StringReader(inputHTML), "");
		return new Parser(reader);
	}

	public static Parser createLinkRecognizingParser(String inputHTML) {
		Parser parser = createParser(inputHTML);
		parser.addScanner(new LinkScanner(LinkTag.LINK_TAG_FILTER));
		return parser;
	}
}
