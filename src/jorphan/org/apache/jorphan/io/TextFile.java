/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 */
package org.apache.jorphan.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Utility class to handle text files as a single lump of text.
 * <p>
 * Note this is just as memory-inefficient as handling a text file can be. Use
 * with restraint.
 * 
 * @author Giles Cope (gilescope at users.sourceforge.net)
 * @author Michael Stover (mstover1 at apache.org)
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision$ updated on $Date$
 */
public class TextFile extends File
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    /**
     * File encoding. null means use the platform's default.
     */
    private String encoding= null;
    
    /**
     * Create a TextFile object to handle the named file with the given encoding.
     * 
     * @param filename File to be read & written through this object.
     * @param encoding Encoding to be used when reading & writing this file.
     */
    public TextFile(File filename, String encoding)
    {
        super(filename.toString());
        setEncoding(encoding);
    }
    
    /**
     * Create a TextFile object to handle the named file with the platform
     * default encoding.
     * 
     * @param filename File to be read & written through this object.
     */
    public TextFile(File filename)
    {
        super(filename.toString());
    }

    /**
     * Create a TextFile object to handle the named file with the platform
     * default encoding.
     * 
     * @param filename Name of the file to be read & written through this object.
     */
    public TextFile(String filename)
    {
        super(filename);
    }

    /**
     * Create a TextFile object to handle the named file with the given
     * encoding.
     * 
     * @param filename Name of the file to be read & written through this object.
     * @param encoding Encoding to be used when reading & writing this file.
     */
    public TextFile(String filename, String encoding)
    {
        super(filename);
    }

    /**
     * Create the file with the given string as content -- or replace it's
     * content with the given string if the file already existed.
     * 
     * @param body New content for the file.
     */
    public void setText(String body)
    {
        try
        {
            Writer writer;
            if (encoding == null)
            {
                writer = new FileWriter(this);
            }
            else
            {
                writer = new OutputStreamWriter(
                    new FileOutputStream(this),
                    encoding);
            }
            writer.write(body);
            writer.flush();
            writer.close();
        }
        catch (IOException ioe)
        {
            log.error("", ioe);
        }
    }

    /**
     * Read the whole file content and return it as a string.
     *  
     * @return the content of the file
     */
    public String getText()
    {
        String lineEnd = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        try
        {
            Reader reader;
            if (encoding == null)
            {
                reader= new FileReader(this);
            }
            else
            {
                reader= new InputStreamReader(
                    new FileInputStream(this),
                    encoding);
            }
            BufferedReader br = new BufferedReader(reader);
            String line = "NOTNULL";
            while (line != null)
            {
                line = br.readLine();
                if (line != null)
                {
                    sb.append(line + lineEnd);
                }
            }
        }
        catch (IOException ioe)
        {
            log.error("", ioe);
        }
        return sb.toString();
    }

    /**
     * @return Encoding being used to read & write this file.
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * @param string Encoding to be used to read & write this file.
     */
    public void setEncoding(String string)
    {
        encoding= string;
    }
}
