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

package org.htmlparser.parserHelper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;

public class ParserHelper implements Serializable
{

    public ParserHelper()
    {
        super();
    }

    /**
     * Opens a connection using the given url.
     * @param url The url to open.
     * @param feedback The ibject to use for messages or <code>null</code>.
     * @exception ParserException if an i/o exception occurs accessing the url.
     */
    public static URLConnection openConnection(
        URL url,
        ParserFeedback feedback)
        throws ParserException
    {
        URLConnection ret;

        try
        {
            ret = url.openConnection();
        }
        catch (IOException ioe)
        {
            String msg =
                "HTMLParser.openConnection() : Error in opening a connection to "
                    + url.toExternalForm();
            ParserException ex = new ParserException(msg, ioe);
            if (null != feedback)
                feedback.error(msg, ex);
            throw ex;
        }

        return (ret);
    }

    /**
     * Opens a connection based on a given string.
     * The string is either a file, in which case <code>file://localhost</code>
     * is prepended to a canonical path derived from the string, or a url that
     * begins with one of the known protocol strings, i.e. <code>http://</code>.
     * Embedded spaces are silently converted to %20 sequences.
     * @param string The name of a file or a url.
     * @param feedback The object to use for messages or <code>null</code> for no feedback.
     * @exception ParserException if the string is not a valid url or file.
     */
    public static URLConnection openConnection(
        String string,
        ParserFeedback feedback)
        throws ParserException
    {
        final String prefix = "file://localhost";
        String resource;
        URL url;
        StringBuffer buffer;
        URLConnection ret;

        try
        {
            url = new URL(LinkProcessor.fixSpaces(string));
            ret = ParserHelper.openConnection(url, feedback);
        }
        catch (MalformedURLException murle)
        { // try it as a file
            try
            {
                File file = new File(string);
                resource = file.getCanonicalPath();
                buffer = new StringBuffer(prefix.length() + resource.length());
                buffer.append(prefix);
                buffer.append(resource);
                url = new URL(LinkProcessor.fixSpaces(buffer.toString()));
                ret = ParserHelper.openConnection(url, feedback);
                if (null != feedback)
                    feedback.info(url.toExternalForm());
            }
            catch (MalformedURLException murle2)
            {
                String msg =
                    "HTMLParser.openConnection() : Error in opening a connection to "
                        + string;
                ParserException ex = new ParserException(msg, murle2);
                if (null != feedback)
                    feedback.error(msg, ex);
                throw ex;
            }
            catch (IOException ioe)
            {
                String msg =
                    "HTMLParser.openConnection() : Error in opening a connection to "
                        + string;
                ParserException ex = new ParserException(msg, ioe);
                if (null != feedback)
                    feedback.error(msg, ex);
                throw ex;
            }
        }

        return (ret);
    }

    /**
     * Lookup a character set name.
     * <em>Vacuous for JVM's without <code>java.nio.charset</code>.</em>
     * This uses reflection so the code will still run under prior JDK's but
     * in that case the default is always returned.
     * @param name The name to look up. One of the aliases for a character set.
     * @param _default The name to return if the lookup fails.
     */
    public static String findCharset(String name, String _default)
    {
        String ret;

        try
        {
            Class cls;
            java.lang.reflect.Method method;
            Object object;

            cls = Class.forName("java.nio.charset.Charset");
            method = cls.getMethod("forName", new Class[] { String.class });
            object = method.invoke(null, new Object[] { name });
            method = cls.getMethod("name", new Class[] {
            });
            object = method.invoke(object, new Object[] {
            });
            ret = (String) object;
        }
        catch (ClassNotFoundException cnfe)
        {
            // for reflection exceptions, assume the name is correct
            ret = name;
        }
        catch (NoSuchMethodException nsme)
        {
            // for reflection exceptions, assume the name is correct
            ret = name;
        }
        catch (IllegalAccessException ia)
        {
            // for reflection exceptions, assume the name is correct
            ret = name;
        }
        catch (java.lang.reflect.InvocationTargetException ita)
        {
            // java.nio.charset.IllegalCharsetNameException
            // and java.nio.charset.UnsupportedCharsetException
            // return the default
            ret = _default;
        }

        return (ret);
    }

}
