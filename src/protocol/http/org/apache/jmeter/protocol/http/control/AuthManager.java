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
package org.apache.jmeter.protocol.http.control;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.util.Base64Encoder;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * This class provides a way to provide Authorization in jmeter requests. The
 * format of the authorization file is: URL user pass where URL is an HTTP URL,
 * user a username to use and pass the appropriate password.
 *
 *@author    <a href="mailto:luta.raphael@networks.vivendi.com">Raphael Luta
 *      </a>
 *@created   $Date$
 *@version   0.9
 ***************************************/
public class AuthManager extends ConfigTestElement implements ConfigElement,
		Serializable
{
	private final static String AUTH_LIST = "AuthManager.auth_list";

	private final static int columnCount = 3;
	private final static String[] columnNames = {
			JMeterUtils.getResString("auth_base_url"),
			JMeterUtils.getResString("username"),
			JMeterUtils.getResString("password")
			};

	/****************************************
	 * Default Constructor
	 ***************************************/
	public AuthManager()
	{
		setProperty(new CollectionProperty(AUTH_LIST, new ArrayList()));
	}
    
    public void clear()
    {
        super.clear();
        setProperty(new CollectionProperty(AUTH_LIST, new ArrayList()));
    }

	/****************************************
	 * update an authentication record
	 *
	 *@param index  !ToDo (Parameter description)
	 *@param url    !ToDo (Parameter description)
	 *@param user   !ToDo (Parameter description)
	 *@param pass   !ToDo (Parameter description)
	 ***************************************/
	public void set(int index, String url, String user, String pass)
	{
		Authorization auth = new Authorization(url, user, pass);
		if(index >= 0)
		{
			getAuthObjects().set(index, new TestElementProperty(auth.getName(),auth));
		}
		else
		{
			getAuthObjects().addItem(auth);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param newName  !ToDo (Parameter description)
	 ***************************************/
	public void setName(String newName)
	{
		setProperty(TestElement.NAME, newName);
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public CollectionProperty getAuthObjects()
	{
        return (CollectionProperty)getProperty(AUTH_LIST);
	}


	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int getColumnCount()
	{
		return columnCount;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param column  !ToDo (Parameter description)
	 *@return        !ToDo (Return description)
	 ***************************************/
	public String getColumnName(int column)
	{
		return columnNames[column];
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param column  !ToDo (Parameter description)
	 *@return        !ToDo (Return description)
	 ***************************************/
	public Class getColumnClass(int column)
	{
		return columnNames[column].getClass();
	}


	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param row  !ToDo (Parameter description)
	 *@return     !ToDo (Return description)
	 ***************************************/
	public Authorization getAuthObjectAt(int row)
	{
		return (Authorization)getAuthObjects().get(row).getObjectValue();
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public boolean isEditable()
	{
		return true;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getClassLabel()
	{
		return JMeterUtils.getResString("auth_manager_title");
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Class getGuiClass()
	{
		return org.apache.jmeter.protocol.http.gui.AuthPanel.class;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Collection getAddList()
	{
		return null;
	}

	/****************************************
	 * return the record at index i
	 *
	 *@param i  !ToDo (Parameter description)
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Authorization get(int i)
	{
		return (Authorization)getAuthObjects().get(i);
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param url  !ToDo (Parameter description)
	 *@return     !ToDo (Return description)
	 ***************************************/
	public String getAuthHeaderForURL(URL url)
	{
		if(isSupportedProtocol(url))
		{
			return null;
		}

		StringBuffer header = new StringBuffer();
		for(PropertyIterator enum = getAuthObjects().iterator(); enum.hasNext(); )
		{
			Authorization auth = (Authorization)enum.next().getObjectValue();
			if(url.toString().startsWith(auth.getURL()))
			{
				header.append("Basic " + Base64Encoder.encode(auth.getUser() + ":" + auth.getPass()));
				break;
			}
		}

		if(header.length() != 0)
		{
			return header.toString();
		}
		else
		{
			return null;
		}
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getName()
	{
		return getPropertyAsString(TestElement.NAME);
	}

	/****************************************
	 * !ToDo
	 *
	 *@param config  !ToDo
	 ***************************************/
	public void addConfigElement(ConfigElement config) { }

	/****************************************
	 * !ToDo
	 *
	 *@param auth  !ToDo
	 ***************************************/
	public void addAuth(Authorization auth)
	{
		getAuthObjects().addItem(auth);
	}

	/****************************************
	 * !ToDo
	 ***************************************/
	public void addAuth()
	{
		getAuthObjects().addItem(new Authorization());
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public boolean expectsModification()
	{
		return false;
	}


	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void uncompile() { }

	/****************************************
	 * save the authentication data to a file
	 *
	 *@param authFile         !ToDo (Parameter description)
	 *@exception IOException  !ToDo (Exception description)
	 ***************************************/
	public void save(String authFile) throws IOException
	{
		File file = new File(authFile);
		if(!file.isAbsolute())
		{
			file = new File(System.getProperty("user.dir") + File.separator + authFile);
		}
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		writer.println("# JMeter generated Authorization file");
		for(int i = 0; i < getAuthObjects().size(); i++)
		{
			Authorization auth = (Authorization)getAuthObjects().get(i);
			writer.println(auth.toString());
		}
		writer.flush();
		writer.close();
	}

	/****************************************
	 * add authentication data from a file
	 *
	 *@param authFile         !ToDo
	 *@exception IOException  !ToDo (Exception description)
	 ***************************************/
	public void addFile(String authFile) throws IOException
	{
		File file = new File(authFile);
		if(!file.isAbsolute())
		{
			file = new File(System.getProperty("user.dir") + File.separator + authFile);
		}
		BufferedReader reader = null;
		if(file.canRead())
		{
			reader = new BufferedReader(new FileReader(file));
		}
		else
		{
			throw new IOException("The file you specified cannot be read.");
		}

		String line;
		while((line = reader.readLine()) != null)
		{
			try
			{
				if(line.startsWith("#") || line.trim().length() == 0)
				{
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, "\t");
				String url = st.nextToken();
				String user = st.nextToken();
				String pass = st.nextToken();
				Authorization auth = new Authorization(url, user, pass);
				getAuthObjects().addItem(auth);
			}
			catch(Exception e)
			{
				throw new IOException("Error parsing auth line\n\t'" + line + "'\n\t" + e);
			}
		}
		reader.close();
	}

	/****************************************
	 * remove an authentication record
	 *
	 *@param index  !ToDo (Parameter description)
	 ***************************************/
	public void remove(int index)
	{
		getAuthObjects().remove(index);
	}

	/****************************************
	 * return the number of records
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int size()
	{
		return getAuthObjects().size();
	}

	private boolean isSupportedProtocol(URL url)
	{
		return !url.getProtocol().toUpperCase().equals("HTTP") &&
				!url.getProtocol().toUpperCase().equals("HTTPS");
	}
}

