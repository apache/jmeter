/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * Default implementation of the HTMLParserFeedback interface.
 * This implementation prints output to the console but users
 * can implement their own classes to support alternate behavior.
 *
 * @author Claude Duguay
 * @see ParserFeedback
 * @see FeedbackManager
**/
public class DefaultParserFeedback implements ParserFeedback, Serializable
{
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
     * Verbosity level.
     * Corresponds to constructor arguments:
     * <pre>
     *   DEBUG = 2;
     *   NORMAL = 1;
     *   QUIET = 0;
     * </pre>
     */
    protected int mode;

    /**
     * Construct a feedback object of the given type.
     * @param mode The type of feedback:
     * <pre>
     *   DEBUG - verbose debugging with stack traces
     *   NORMAL - normal messages
     *   QUIET - no messages
     * </pre>
     */
    public DefaultParserFeedback(int mode)
    {
        if (mode < QUIET || mode > DEBUG)
            throw new IllegalArgumentException(
                "illegal mode ("
                    + mode
                    + "), must be one of: QUIET, NORMAL, DEBUG");
        this.mode = mode;
    }

    /**
     * Construct a NORMAL feedback object.
     */
    public DefaultParserFeedback()
    {
        this(NORMAL);
    }

    /**
     * Print an info message.
     * @param message The message to print.
     */
    public void info(String message)
    {
        if (mode != QUIET)
            System.out.println("INFO: " + message);
    }

    /**
     * Print an warning message.
     * @param message The message to print.
     */
    public void warning(String message)
    {
        if (mode != QUIET)
            System.out.println("WARNING: " + message);
    }

    /**
     * Print an error message.
     * @param message The message to print.
     * @param exception The exception for stack tracing.
     */
    public void error(String message, ParserException exception)
    {
        if (mode != QUIET)
        {
            System.out.println("ERROR: " + message);
            if (mode == DEBUG && (exception != null))
                exception.printStackTrace();
        }
    }
}
