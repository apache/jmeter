package org.apache.jmeter.protocol.http.parser;

/************************************************************
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@created    June 16, 2001
 *@version    1.0
 ***********************************************************/

public class FormElement
{
	private boolean multipleAllowed;
	private String name;
	private java.util.List possibleValues;
	private java.util.List defaultValues;

	/************************************************************
	 *  Constructor for the FormElement object
	 ***********************************************************/
	public FormElement()
	{
	}

	/************************************************************
	 *  Sets the MultipleAllowed attribute of the FormElement object
	 *
	 *@param  newMultipleAllowed  The new MultipleAllowed value
	 ***********************************************************/
	public void setMultipleAllowed(boolean newMultipleAllowed)
	{
		multipleAllowed = newMultipleAllowed;
	}

	/************************************************************
	 *  Sets the Name attribute of the FormElement object
	 *
	 *@param  newName  The new Name value
	 ***********************************************************/
	public void setName(String newName)
	{
		name = newName;
	}

	/************************************************************
	 *  Sets the PossibleValues attribute of the FormElement object
	 *
	 *@param  newPossibleValues  The new PossibleValues value
	 ***********************************************************/
	public void setPossibleValues(java.util.List newPossibleValues)
	{
		possibleValues = newPossibleValues;
	}

	/************************************************************
	 *  Sets the DefaultValues attribute of the FormElement object
	 *
	 *@param  newDefaultValues  The new DefaultValues value
	 ***********************************************************/
	public void setDefaultValues(java.util.List newDefaultValues)
	{
		defaultValues = newDefaultValues;
	}

	/************************************************************
	 *  Gets the MultipleAllowed attribute of the FormElement object
	 *
	 *@return    The MultipleAllowed value
	 ***********************************************************/
	public boolean isMultipleAllowed()
	{
		return multipleAllowed;
	}

	/************************************************************
	 *  Gets the Name attribute of the FormElement object
	 *
	 *@return    The Name value
	 ***********************************************************/
	public String getName()
	{
		return name;
	}

	/************************************************************
	 *  Gets the PossibleValues attribute of the FormElement object
	 *
	 *@return    The PossibleValues value
	 ***********************************************************/
	public java.util.List getPossibleValues()
	{
		return possibleValues;
	}

	/************************************************************
	 *  Gets the DefaultValues attribute of the FormElement object
	 *
	 *@return    The DefaultValues value
	 ***********************************************************/
	public java.util.List getDefaultValues()
	{
		return defaultValues;
	}

}
