package org.apache.jmeter.protocol.http.parser;

import java.util.*;

/************************************************************
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author
 *@created    June 29, 2001
 *@version    1.0
 ***********************************************************/

class HtmlForm
{

	/************************************************************
	 *  Constructor for the HtmlForm object
	 ***********************************************************/
	public HtmlForm()
	{
	}

	/************************************************************
	 *  Give the action string and a list of FormElement objects to create
	 *  the new HtmlForm, to be used by the HtmlParser.
	 *
	 *@param  action        Description of Parameter
	 *@param  formElements  Description of Parameter
	 ***********************************************************/
	public HtmlForm(String action, Collection formElements)
	{
	}

	public static Collection extractFormElements(String formString)
	{
		return Collections.EMPTY_LIST;
	}
}
