package org.apache.jmeter.protocol.http.modifier;

import org.apache.jmeter.testelement.AbstractTestElement;
import java.io.Serializable;

/**
 *  This object defines with what a parameter has its value replaced, and the
 *  policies for how that value changes. Used in {@link ParamModifier}.
 *
 *@author     David La France
 *@author     <a href="mailto:seade@backstagetech.com.au">Scott Eade</a>
 *@created    Jan 18, 2002
 */
public class ParamMask extends AbstractTestElement implements Serializable
{
	private String PREFIX = "ParamModifier.prefix";
	private String FIELD_NAME = "ParamModifier.field_name";
	private String UPPER_BOUND = "ParamModifier.upper_bound";
	private String LOWER_BOUND = "ParamModifier.lower_bound";
	private String INCREMENT = "ParamModifier.increment";
	private String SUFFIX = "ParamModifier.suffix";

	private long _value = 0;

	/**
	 *  Default constructor
	 */
	public ParamMask()
	{
		setFieldName("");
		setPrefix("");
		setLowerBound(0);
		setUpperBound(0);
		setIncrement(0);
		setSuffix("");
	}


	/**
	 *  Sets the prefix for the <code>long</code> value. The prefix, the value
	 *  and the suffix are concatenated to give the parameter value. This allows
	 *  a wider range of posibilities for the parameter values.
	 *
	 *@param  prefix  A string to prefix to the parameter value
	 */
	public void setPrefix(String prefix)
	{
		setProperty(PREFIX, prefix);
	}


	/**
	 *  Set the current value of the <code>long<code> portion of the parameter value to replace.
	 * This is usually not used, as the method {@link #resetValue} is used to define a policy for
	 * the starting value.
	 *
	 *@param  val  The new parameter value
	 */
	public void setValue(long val)
	{
		_value = val;
	}

	public void setFieldName(String fieldName)
	{
		setProperty(FIELD_NAME,fieldName);
	}


	/**
	 *  Sets the lowest possible value that the <code>long</code> portion of the
	 *  parameter value may be.
	 *
	 *@param  val  The new lowest possible parameter value
	 */
	public void setLowerBound(long val)
	{
		setProperty(LOWER_BOUND, new Long(val));
	}


	/**
	 *  Sets the highest possible value that the <code>long</code> portion of the
	 *  parameter value may be.
	 *
	 *@param  val  The new highest possible parameter value
	 */
	public void setUpperBound(long val)
	{
		setProperty(UPPER_BOUND,new Long(val));
	}


	/**
	 *  Sets the number by which the parameter value is incremented between loops.
	 *
	 *@param  incr  The new increment for the parameter value
	 */
	public void setIncrement(long incr)
	{
		setProperty(INCREMENT,new Long(incr));
	}


	/**
	 *  Sets the suffix for the <code>long</code> value. The prefix, the value
	 *  and the suffix are concatenated to give the parameter value. This allows
	 *  a wider range of posibilities for the parameter values.
	 *
	 *@param  suffix  A string to suffix to the parameter value
	 */
	public void setSuffix(String suffix)
	{
		setProperty(SUFFIX, suffix);
	}


	/**
	 *  Acessor method to return the <code>String</code> that will be prefixed to
	 *  the <code>long</code> value.
	 *
	 *@return    The parameter value prefix
	 */
	public String getPrefix()
	{
		return (String) getProperty(PREFIX);
	}


	/**
	 *  Acessor method, returns the lowest possible value that the <code>long</code>
	 *  portion of the parameter value may be.
	 *
	 *@return    The lower bound of the possible values
	 */
	public long getLowerBound()
	{
		Object lowerBound = getProperty(LOWER_BOUND);
		return getLongValue(lowerBound);
	}


	/**
	 *  Acessor method, returns the highest possible value that the <code>long</code>
	 *  portion of the parameter value may be.
	 *
	 *@return    The higher bound of the possible values
	 */
	public long getUpperBound()
	{
		Object bound = getProperty(UPPER_BOUND);
		return getLongValue(bound);
	}


	/**
	 *  Acessor method, returns the number by which the parameter value is
	 *  incremented between loops.
	 *
	 *@return    The increment
	 */
	public long getIncrement()
	{
		Object inc = getProperty(INCREMENT);
		return getLongValue(inc);
	}


	/**
	 *  Acessor method to return the <code>String</code> that will be suffixed to
	 *  the <code>long</code> value.
	 *
	 *@return    The parameter value suffix
	 */
	public String getSuffix()
	{
		return (String) getProperty(SUFFIX);
	}


	/*
	 *  ----------------------------------------------------------------------------------------------
	 *  Methods
	 *  --------------------------------------------------------------------------------------------
	 */
	/**
	 *  <P>
	 *
	 *  Returns the current value, prefixed and suffixed, as a string, then
	 *  increments it. If the incremented value is above the upper bound, the
	 *  value is reset to the lower bound. <BR>
	 *  <P>
	 *
	 *  This method determines the policy of what happens when an upper bound is
	 *  reached/surpassed.
	 *
	 *@return    A <code>String</code> representing the current <code>long</code>
	 *      value
	 */
	public String getNextValue()
	{
		// return the current value (don't forget the prefix!)
		String retval = getPrefix() + Long.toString(_value) + getSuffix();

		// increment the value
		_value += getIncrement();
		if (_value > getUpperBound())
		{
			_value = getLowerBound();
		}

		return retval;
	}


	/**
	 *  This method determines the policy of what value to start (and re-start) at
	 */
	public void resetValue()
	{
		_value = getLowerBound();
	}

	public String getFieldName()
	{
		return (String)getProperty(FIELD_NAME);
	}


	/**
	 *  For debugging purposes
	 *
	 *@return    A <code>String</code> representing the object
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("-------------------------------\n");
		sb.append("Dumping ParamMask Object\n");
		sb.append("-------------------------------\n");
		sb.append("Name          = " + getFieldName() + "\n");
		sb.append("Prefix        = " + getPrefix() + "\n");
		sb.append("Current Value = " + _value + "\n");
		sb.append("Lower Bound   = " + getLowerBound() + "\n");
		sb.append("Upper Bound   = " + getUpperBound() + "\n");
		sb.append("Increment     = " + getIncrement() + "\n");
		sb.append("Suffix        = " + getSuffix() + "\n");
		sb.append("-------------------------------\n");

		return sb.toString();
	}


	/**
	 *  Gets the LongValue attribute of the ParamMask object
	 *
	 *@param  bound  Description of Parameter
	 */
	private long getLongValue(Object bound)
	{
		if (bound == null)
		{
			return (long)0;
		} else if (bound instanceof Long)
		{
			return ((Long) bound).longValue();
		} else
		{
			return Long.parseLong((String) bound);
		}
	}
}
