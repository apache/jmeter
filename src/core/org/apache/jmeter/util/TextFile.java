package org.apache.jmeter.util;

import java.io.*;

/************************************************************
 *  !ToDo (Class description)
 *
 *@author     $Author$
 *@created    $Date$
 *@version    $Revision$
 ***********************************************************/
public class TextFile extends File
{
	/************************************************************
	 *  !ToDo (Constructor description)
	 *
	 *@param  filename  !ToDo (Parameter description)
	 ***********************************************************/
	public TextFile(File filename)
	{
		super(filename.toString());
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
			System.err.println(ioe.getMessage());
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
			System.err.println(ioe.getMessage());
		}
		return sb.toString();
	}
}
