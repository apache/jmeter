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

package org.apache.jmeter.protocol.http.config;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Title:        JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

public class MultipartUrlConfig implements Serializable
{

	public static String MULTIPART_FORM = "multipart/form-data";
	private static String BOUNDARY = "boundary";
	private String boundary,filename,fileField,mimetype;
	private Arguments args;

	public MultipartUrlConfig()
	{
		args = new Arguments();
	}

	public MultipartUrlConfig(String boundary)
	{
		this();
		this.boundary = boundary;
	}

	public void setBoundary(String boundary)
	{
		this.boundary = boundary;
	}

	public String getBoundary()
	{
		return boundary;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getFilename()
	{
		return filename;
	}
	
	public Arguments getArguments()
	{
		return args;
	}

	public void setFileFieldName(String name)
	{
		this.fileField = name;
	}

	public String getFileFieldName()
	{
		return fileField;
	}

	public void setMimeType(String type)
	{
		mimetype = type;
	}

	public String getMimeType()
	{
		return mimetype;
	}
	
	public void addArgument(String name,String value)
	{
		Arguments args = this.getArguments();
		args.addArgument(new HTTPArgument(name,value));
	}

	public void addArgument(String name,String value,String metadata)
	{
		Arguments args = this.getArguments();
		args.addArgument(new HTTPArgument(name,value,metadata));
	}
	
	public void addEncodedArgument(String name,String value)
	{
		Arguments args = getArguments();
		args.addArgument(new HTTPArgument(name,value,true));
	}

		/**
	 * This method allows a proxy server to send over the raw text from a browser's
	 * output stream to be parsed and stored correctly into the UrlConfig object.
	 */
	public void parseArguments(String queryString)
	{
		String[] parts = JMeterUtils.split(queryString,"--"+getBoundary());
		for (int i = 0; i < parts.length; i++)
		{
			if(parts[i].indexOf("filename=") > -1)
			{
				int index = parts[i].indexOf("name=\"")+6;
				String name = parts[i].substring(index,parts[i].indexOf("\"",index));
				index = parts[i].indexOf("filename=\"")+10;
				String filename = parts[i].substring(index,parts[i].indexOf("\"",index));
				index = parts[i].indexOf("\n",index);
				index = parts[i].indexOf(":",index)+1;
				String mimetype = parts[i].substring(index,parts[i].indexOf("\n",index)).trim();
				this.setFileFieldName(name);
				this.setFilename(filename);
				this.setMimeType(mimetype);
			}
			else if(parts[i].indexOf("name=") > -1)
			{
				int index = parts[i].indexOf("name=\"")+6;
				String name = parts[i].substring(index,parts[i].indexOf("\"",index));
				index = parts[i].indexOf("\n",index)+2;
				String value = parts[i].substring(index).trim();
				this.addEncodedArgument(name,value);
			}
		}
	}
}
