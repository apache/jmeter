package org.apache.jmeter.protocol.http.modifier;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.config.Modifier;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;

import java.io.Serializable;
import java.util.*;

/**
 *  <P>
 *
 *  This modifier will replace any single http sampler's url parameter value
 *  with a value from a given range - thereby "masking" the value set in the
 *  http sampler. The parameter names must match exactly, and the parameter
 *  value must be preset to "*" to diferentiate between duplicate parameter
 *  names. <BR>
 *  <P>
 *
 *  For example, if you set up the modifier with a lower bound of 1, an upper
 *  bound of 10, and an increment of 2, and run the loop 12 times, the parameter
 *  will have the following values (one per loop): 1, 3, 5, 7, 9, 1, 3, 5, 7, 9,
 *  1, 3 <BR>
 *  <P>
 *
 *  The {@link ParamMask} object contains most of the logic for stepping through
 *  this loop. You can make large modifications to this modifier's behaviour by
 *  changing one or two method implementations there.
 *
 *@author     David La France
 *@created    Jan 18, 2002
 *@see        ParamMask
 */
public class ParamModifier extends AbstractTestElement implements Modifier, Serializable
{

	/*
	 *  ----------------------------------------------------------------------------------------------
	 *  Fields
	 *  --------------------------------------------------------------------------------------------
	 */
	/**
	 *  The key used to find the ParamMask object in the HashMap
	 */
	private final static String MASK = "ParamModifier.mask";


	/*
	 *  ----------------------------------------------------------------------------------------------
	 *  Constructors
	 *  --------------------------------------------------------------------------------------------
	 */
	/**
	 *  Default constructor
	 */
	public ParamModifier()
	{
		setProperty(MASK,new ParamMask());
	}

	public ParamMask getMask()
	{
		return (ParamMask)getProperty(MASK);
	}

	/*
	 *  ----------------------------------------------------------------------------------------------
	 *  Methods implemented from interface org.apache.jmeter.config.Modifier
	 *  --------------------------------------------------------------------------------------------
	 */
	/**
	 *  Modifies an entry object to replace the value of any url parameter that
	 *  matches a defined mask.
	 *
	 *@param  entry  Entry object containing information about the current test
	 *@return        <code>True</code> if modified, else <code>false</code>
	 */
	public boolean modifyEntry(Sampler sam)
	{
		HTTPSampler sampler = null;
		if(!(sam instanceof HTTPSampler))
		{
			return false;
		}
		else
		{
			sampler = (HTTPSampler)sam;
		}
		boolean modified = false;
		Iterator iter = sampler.getArguments().iterator();
		while (iter.hasNext())
		{
			Argument arg = (Argument) iter.next();
			modified = modifyArgument(arg);
			if (modified)
			{
				break;
			}
		}
		return modified;
	}


	/*
	 *  ----------------------------------------------------------------------------------------------
	 *  Methods
	 *  --------------------------------------------------------------------------------------------
	 */
	/**
	 *  Helper method for {@link #modifyEntry} Replaces a parameter's value if the
	 *  parameter name matches the mask name and the value is a "*"
	 *
	 *@param  arg  An {@link Argument} representing a http parameter
	 *@return      <code>true</code>if the value was replaced
	 */
	private boolean modifyArgument(Argument arg)
	{
		// if a mask for this argument exists
		if (arg.getName().equals(getMask().getFieldName()))
		{
			// values to be masked must be set in the WebApp to "*"
			if ("*".equals(arg.getValue()))
			{
				arg.setValue(getMask().getNextValue());
				return true;
			}
		}
		return false;
	}
}
