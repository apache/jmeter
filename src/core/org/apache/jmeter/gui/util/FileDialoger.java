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
 */package org.apache.jmeter.gui.util;
import java.io.*;
import javax.swing.*;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class FileDialoger
{
	/****************************************
	 * The last directory visited by the user while choosing Files.
	 ***************************************/
	public static String lastJFCDirectory = null;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public FileDialoger() { }

	/****************************************
	 * Prompts the user to choose a file from their filesystems for our own devious
	 * uses. This method maintains the last directory the user visited before
	 * dismissing the dialog. This does NOT imply they actually chose a file from
	 * that directory, only that they closed the dialog there. It is the caller's
	 * responsibility to check to see if the selected file is non-null.
	 *
	 *@return    !ToDo (Return description)
	 *@returns   The JFileChooser that interacted with the user, after they are
	 *      finished using it (accept or otherwise).
	 ***************************************/
	public static JFileChooser promptToOpenFile(String[] exts)
	{
		JFileChooser jfc = null;

		if(lastJFCDirectory == null)
		{
			String start = JMeterUtils.getPropDefault("user.dir", "");

			if(!start.equals(""))
			{
				jfc = new JFileChooser(start);
			}
			else
			{
				jfc = new JFileChooser();
			}
		}
		else
		{
			jfc = new JFileChooser(lastJFCDirectory);
		}

		jfc.addChoosableFileFilter(new JMeterFileFilter(exts));
		int retVal = jfc.showOpenDialog(GuiPackage.getInstance().getMainFrame());
		lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();

		if(retVal == jfc.APPROVE_OPTION)
		{
			return jfc;
		}
		else 
		{
			return null;
		}
	}
	
	public static JFileChooser promptToOpenFile()
	{
		return promptToOpenFile(new String[0]);
	}

	/****************************************
	 * Prompts the user to choose a file from their filesystems for our own devious
	 * uses. This method maintains the last directory the user visited before
	 * dismissing the dialog. This does NOT imply they actually chose a file from
	 * that directory, only that they closed the dialog there. It is the caller's
	 * responsibility to check to see if the selected file is non-null.
	 *
	 *@param filename  !ToDo (Parameter description)
	 *@return          !ToDo (Return description)
	 *@returns         The JFileChooser that interacted with the user, after they
	 *      are finished using it (accept or otherwise).
	 *@see             #promptToOpenFile
	 ***************************************/
	public static JFileChooser promptToSaveFile(String filename)
	{
		JFileChooser jfc = null;
		if(lastJFCDirectory == null)
		{
			String start = JMeterUtils.getPropDefault("user.dir", "");
			if(!start.equals(""))
			{
				jfc = new JFileChooser(start);
			}
			else
			{
				jfc = new JFileChooser();
			}
		}
		else
		{
			jfc = new JFileChooser(lastJFCDirectory);
		}
		String ext = ".jmx";
		if(filename != null)
		{
			jfc.setSelectedFile(new File(lastJFCDirectory, filename));
			int i = -1;
			if((i = filename.indexOf(".")) > -1)
			{
				ext = filename.substring(i);
			}
		}		
		
		jfc.addChoosableFileFilter(new JMeterFileFilter(new String[]{ext}));


		int retVal = jfc.showSaveDialog(GuiPackage.getInstance().getMainFrame());
		lastJFCDirectory = jfc.getCurrentDirectory().getAbsolutePath();
		if(retVal == jfc.APPROVE_OPTION)
		{
			return jfc;
		}
		else 
		{
			return null;
		}
	}
}
