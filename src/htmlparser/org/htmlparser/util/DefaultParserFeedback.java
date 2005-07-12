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
package org.htmlparser.util;

import java.io.Serializable;

/**
 * Default implementation of the HTMLParserFeedback interface. This
 * implementation prints output to the console but users can implement their own
 * classes to support alternate behavior.
 * 
 * @author Claude Duguay
 * @see ParserFeedback
 * @see FeedbackManager
 */
public class DefaultParserFeedback implements ParserFeedback, Serializable {
	/**
	 * Constructor argument for a quiet feedback.
	 */
	public static final int QUIET = 0;

	/**
	 * Constructor argument for a normal feedback.
	 */
	public static final int NORMAL = 1;

	/**
	 * Constructor argument for a debugging feedback.
	 */
	public static final int DEBUG = 2;

	/**
	 * Verbosity level. Corresponds to constructor arguments:
	 * 
	 * <pre>
	 * DEBUG = 2;
	 * NORMAL = 1;
	 * QUIET = 0;
	 * </pre>
	 */
	protected int mode;

	/**
	 * Construct a feedback object of the given type.
	 * 
	 * @param mode
	 *            The type of feedback:
	 * 
	 * <pre>
	 * 
	 *    DEBUG - verbose debugging with stack traces
	 *    NORMAL - normal messages
	 *    QUIET - no messages
	 *  
	 * </pre>
	 */
	public DefaultParserFeedback(int mode) {
		if (mode < QUIET || mode > DEBUG)
			throw new IllegalArgumentException("illegal mode (" + mode + "), must be one of: QUIET, NORMAL, DEBUG");
		this.mode = mode;
	}

	/**
	 * Construct a NORMAL feedback object.
	 */
	public DefaultParserFeedback() {
		this(NORMAL);
	}

	/**
	 * Print an info message.
	 * 
	 * @param message
	 *            The message to print.
	 */
	public void info(String message) {
		if (mode != QUIET)
			System.out.println("INFO: " + message);
	}

	/**
	 * Print an warning message.
	 * 
	 * @param message
	 *            The message to print.
	 */
	public void warning(String message) {
		if (mode != QUIET)
			System.out.println("WARNING: " + message);
	}

	/**
	 * Print an error message.
	 * 
	 * @param message
	 *            The message to print.
	 * @param exception
	 *            The exception for stack tracing.
	 */
	public void error(String message, ParserException exception) {
		if (mode != QUIET) {
			System.out.println("ERROR: " + message);
			if (mode == DEBUG && (exception != null))
				exception.printStackTrace();
		}
	}
}
