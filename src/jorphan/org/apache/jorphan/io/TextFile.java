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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/************************************************************
 *  !ToDo (Class description)
 *
 *@author     Giles Cope (gilescope at users.sourceforge.net), Michael Stover (mstover1 at apache.org)
 ***********************************************************/
public class TextFile extends File
{
	
	transient private static Logger log = LoggingManager.getLoggerFor(
			"jorphan.io");
	/************************************************************
	 *  !ToDo (Constructor description)
	 *
	 *@param  filename  !ToDo (Parameter description)
	 ***********************************************************/
	public TextFile(File filename)
	{
		super(filename.toString());
	}	
	
	public TextFile()
	{
		super("");
	}

	/************************************************************
	 *  !ToDo (Constructor description)
	 *
	 *@param  filename  !ToDo (Parameter description)
	 ***********************************************************/
	public TextFile(String filename)
	{
		super(filename);
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  body  !ToDo (Parameter description)
	 ***********************************************************/
	public void setText(String body)
	{
		try
		{
			Writer writer = new FileWriter(this);
			writer.write(body);
			writer.flush();
			writer.close();
		}
		catch (IOException ioe)
		{
			log.error("",ioe);
		}
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public String getText()
	{
		String lineEnd = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(this));
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
			log.error("",ioe);
		}
		return sb.toString();
	}
}
