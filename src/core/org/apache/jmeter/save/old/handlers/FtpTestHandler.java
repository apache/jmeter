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
 package org.apache.jmeter.save.old.handlers;

import java.io.Writer;
import java.util.List;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.protocol.ftp.sampler.FTPSampler;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.xml.sax.Attributes;

/**
 *  Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 *  Apache Foundation
 *
 *@created    July 30, 2001
 *@version    1.0
 */

public class FtpTestHandler extends TagHandler
{
	FTPSampler sampler;
	//Constructor for the FtpTestHandler object

		  public FtpTestHandler()	{}

	/**
	 *  Gets the PrimaryTagName attribute of the FtpTestHandler
	 */
	public String getPrimaryTagName()
	{
		return "FtpTestSampler";
	}

	//called by reflection when the <defaultUrl> tag is ended
		  public void defaultUrlTagEnd()
	{
		List children = xmlParent.takeChildObjects(this);
		if (children.size() == 1)
		{
			sampler.addTestElement((TestElement)((TagHandler)children.get(0)).getModel());
		}
	}

		  //called by reflection when the <defaultLogin> tag is ended
		  public void loginConfigTagEnd()
	{
		List children = xmlParent.takeChildObjects(this);
		if (children.size() == 1)
		{
			sampler.addTestElement((TestElement)((TagHandler)children.get(0)).getModel());
		}
	}
	
	public void setAtts(Attributes atts) throws java.lang.Exception
	{
		sampler = new FTPSampler();
		sampler.setName(atts.getValue("name"));
		sampler.setProperty(TestElement.GUI_CLASS,"org.apache.jmeter.protocol.ftp.control.gui.FtpTestSamplerGui");

	}
	
	public Object getModel()
	{
		return sampler;
	}
	/**
	 *  Does the saving, calls write methods below to output config info to XML config file.
			*/
	public void save(Saveable cont, Writer out) throws java.io.IOException
	{
		/*FtpTestSampler controller = (FtpTestSampler) cont;
		writeMainTag(out, controller);
		writeDefaultUrl(out, controller);
		writeLoginConfig(out, controller);
		this.writeSubElements(controller,out);
		out.write("</FtpTestSampler>\n");*/
	}

	/**
	 *  This method writes the opening main tag to the XML config file, i.e. <FtpTestSampler>
	 */
	protected void writeMainTag(Writer out, GenericController controller) throws java.io.IOException
	{
		out.write("<FtpTestSampler type=\"");
		out.write(JMeterHandler.convertToXML(controller.getClass().getName()));
		out.write("\" name=\"");
		out.write(JMeterHandler.convertToXML(controller.getName()));
		out.write("\">\n");
	}

	/**
			* Write out default domain and default filename defined in 'FTP Testing'
	 * @exception  java.io.IOException  Description of Exception
	 */
	protected void writeDefaultUrl(Writer out)
			 throws java.io.IOException
	{
		/*out.write("<defaultUrl>\n");
		JMeterHandler.writeObject(controller.getDefaultUrl(), out);
		out.write("</defaultUrl>\n");*/
	}


		  /**
			* Write username and password info
			*/
		  protected void writeLoginConfig(Writer out)
			 throws java.io.IOException
	{
		/*out.write("<loginConfig>\n");
		JMeterHandler.writeObject(controller.getLoginConfig(), out);
		out.write("</loginConfig>\n");*/
	}
}



