/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is an implementation of a full-fledged property editor, providing
 * both object-text transformation and an editor GUI (a custom editor
 * component), from two simpler property editors providing only one
 * of these functionalities each, namely:
 * <dl>
 * <dt>typeEditor<dt>
 * <dd>Provides suitable object-to-string and string-to-object
 * transformation for the property's type. That is: it's a simple editor
 * that only need to support the set/getAsText and set/getValue methods.</dd>
 * <dt>guiEditor</dt>
 * <dd>Provides a suitable GUI for the property, but works on
 * [possibly null] String values. That is: it supportsCustomEditor, but
 * get/setAsText and get/setValue are indentical.</dd>
 * </dl>
 * <p>
 * The resulting editor provides optional support for null values (you
 * can choose whether <b>null</b> is to be a valid property value).
 * It also provides optional support for JMeter 'expressions' (you can
 * choose whether they make valid property values).
 */
class WrapperEditor extends PropertyEditorSupport
		implements PropertyChangeListener
{
    protected static Logger log= LoggingManager.getLoggerForClass();

	/**
	 * The type's property editor.
	 */
	PropertyEditor typeEditor;
	
	/**
	 * The gui property editor
	 */
	PropertyEditor guiEditor;

	/**
	 * Whether to allow <b>null</b> as a property value.
	 */
	boolean acceptsNull;
	
	/**
	 * Whether to allow JMeter 'expressions' as property values.
	 */
	boolean acceptsExpressions;

	/**
	 * Whether to allow any constant values different from the provided tags. 
	 */
	boolean acceptsOther;

    /**
   	 * Keep track of the last valid value in the editor, so that we can
   	 * revert to it if the user enters an invalid value.
   	 */
    private String lastValidValue= null;

/*
I formerly used this method to obtain a type against which I could
validate property values, e.g. in isValidProperty. Later I found that
simple propertyEditors can do this in a simpler way -- they throw
InvalidValueException if the value is not of the appropriate type.

TODO: remove this code once I'm reasonably convinced that what
I said above is true (so I won't need to reinstate this code).

    private static Class objectType(Class type)
    {
    	// Sorry for this -- I have not found a better way:
        if (! type.isPrimitive()) return type;
    	else if (type == boolean.class) return Boolean.class;
    	else if (type == char.class) return Character.class;
    	else if (type == byte.class) return Byte.class;
    	else if (type == short.class) return Short.class;
    	else if (type == int.class) return Integer.class;
    	else if (type == long.class) return Long.class;
    	else if (type == float.class) return Float.class;
    	else if (type == double.class) return Double.class;
    	else if (type == void.class) return Void.class;
    	else
    	{
    		log.error("Class "+type+" is an unknown primitive type.");
    		throw new Error("Class "+type+" is an unknown primitive type");
    			// programming error: bail out.
    	}
    }
*/
	public WrapperEditor(
			PropertyEditor typeEditor, 
			PropertyEditor guiEditor,
			boolean acceptsNull, 
			boolean acceptsExpressions, 
			boolean acceptsOther) {
		super();
		this.typeEditor= typeEditor;
		this.guiEditor= guiEditor;
		this.acceptsNull= acceptsNull;
		this.acceptsExpressions= acceptsExpressions;
		this.acceptsOther= acceptsOther;
		
		guiEditor.addPropertyChangeListener(this);
	}

    public boolean supportsCustomEditor()
    {
        return true;
    }

	public Component getCustomEditor()
	{
		return guiEditor.getCustomEditor();
	}

	public String[] getTags()
	{
		return guiEditor.getTags();
	}

	/**
	 * Determine wheter a string is one of the known tags.
	 * 
	 * @param text
	 * @return true iif text equals one of the getTags()
	 */
	private boolean isATag(String text)
	{
		String[] tags= getTags();
		if (tags == null) return false;
		for (int i=0; i<tags.length; i++)
		{
			if (tags[i].equals(text)) return true;
		}
		return false;
	}
	
    /**
 	 * Determine whether a string is a valid value for the property.
   	 * 
   	 * @param text the value to be checked
   	 * @return true iif text is a valid value
   	 */
    private boolean isValidValue(String text)
    {
		if (text == null) return acceptsNull;

    	if (acceptsExpressions && isExpression(text)) return true;

    	// Not an expression (isn't or can't be), not null.
    	
		// The known tags are assumed to be valid:
		if (isATag(text)) return true;
		
		// Was not a tag, so if we can't accept other values...
		if (! acceptsOther) return false;
		
		// Delegate the final check to the typeEditor:
    	try
    	{
    		typeEditor.setAsText(text);
    	}
    	catch (IllegalArgumentException e1)
    	{
    		// setAsText failed: not valid
    		return false;
    	}
    	// setAsText succeeded: valid
    	return true;
    }

	/**
	 * This method is used to do some low-cost defensive programming:
	 * it is called when a condition that the program logic should prevent
	 * from happening occurs. I hope this will help early detection of
	 * logical bugs in property value handling.
	 * 
	 * @throws Error always throws an error.
	 */
	private final void shouldNeverHappen() throws Error
	{
		throw new Error(); // Programming error: bail out.
	}

	/**
	 * Same as shouldNeverHappen(), but provide a source exception.
	 * 
	 * @param e the exception that helped identify the problem
	 * @throws Error always throws one.
	 */
	private final void shouldNeverHappen(Exception e) throws Error
	{
		throw new Error(e.toString()); // Programming error: bail out.
	}

	/**
	 * Check if a string is a valid JMeter 'expression'.
	 * <p>
	 * The current implementation is very basic: it just accepts any
	 * string containing "${" as a valid expression.
	 * TODO: improve, but keep returning true for "${}". 
	 */
	private final boolean isExpression(String text)
	{
		return text.indexOf("${") != -1;
	}

	/**
	 * Same as isExpression(String).
	 * 
	 * @param text
	 * @return true iif text is a String and isExpression(text).
	 */
	private final boolean isExpression(Object text)
	{
		return text instanceof String && isExpression((String)text);
	}

    /**
     * @see java.beans.PropertyEditor#getValue()
     * @see org.apache.jmeter.testelement.property.JMeterProperty
     */
    public Object getValue()
    {
        String text= (String)guiEditor.getValue();
    
		Object value;

		if (text == null)
		{
			if (!acceptsNull) shouldNeverHappen();
			value= null;
		}
    	else
    	{
    		if (acceptsExpressions && isExpression(text))
    		{
    			value= text;
    		}
    		else
    		{
    			// not an expression (isn't or can't be), not null.
				
				// a check, just in case:
				if (! acceptsOther && ! isATag(text)) shouldNeverHappen();

    			try
    			{
    				typeEditor.setAsText(text);
    			}
    			catch (IllegalArgumentException e)
    			{
    				shouldNeverHappen(e);
    			}
				value= typeEditor.getValue();
    		}
    	}
    
        if (log.isDebugEnabled())
        {
            log.debug(
                "->"
                    + (value != null ? value.getClass().getName() : "NULL")
                    + ":"
                    + value);
        }
        return value;
    }

    public void setValue(Object value)
    {
    	String text;

    	if (log.isDebugEnabled())
    	{
    		log.debug(
    			"<-"
    				+ (value != null ? value.getClass().getName() : "NULL")
    				+ ":"
    				+ value);
    	}

    	if (value == null)
    	{
    		if (!acceptsNull) throw new IllegalArgumentException();
    		text= null;
    	}
    	else if (acceptsExpressions && isExpression(value))
    	{
    		text= (String)value;
    	}
    	else
    	{
			// Not an expression (isn't or can't be), not null.
    		typeEditor.setValue(value); // may throw IllegalArgumentExc.
    		text= typeEditor.getAsText();
    		
    		if (! acceptsOther && ! isATag(text)) throw new IllegalArgumentException();
    	}

    	guiEditor.setValue(text);
    	
    	firePropertyChange();
    }

    public String getAsText()
    {
    	String text= guiEditor.getAsText();
    
		if (text == null)
		{
			if (!acceptsNull) shouldNeverHappen();
		}
    	else if (!acceptsExpressions || !isExpression(text))
    	{
    		// not an expression (can't be or isn't), not null.
			try
			{
				typeEditor.setAsText(text);
			}
			catch (IllegalArgumentException e)
			{
				shouldNeverHappen(e);
			}
			text= typeEditor.getAsText();

			// a check, just in case:
			if (! acceptsOther && ! isATag(text)) shouldNeverHappen();
    	}
    
    	if (log.isDebugEnabled())
    	{
    		log.debug("->\"" + text + "\"");
    	}
    	return text;
    }

    public void setAsText(String text) throws IllegalArgumentException
    {
		if (log.isDebugEnabled())
		{
			log.debug(text == null ? "<-null" : "<-\"" + text + "\"");
		}
    		
		String value;

		if (text == null)
		{
			if (! acceptsNull) throw new IllegalArgumentException();
			value= null;
		}
		else 
		{
			if (acceptsExpressions && isExpression(text))
			{
				value= text;
			}
			else
			{
				// Some editors do tiny transformations (e.g. "true" to "True",...):
				typeEditor.setAsText(text); // may throw IllegalArgumentException
				value= typeEditor.getAsText();
				
				if (! acceptsOther && ! isATag(text)) throw new IllegalArgumentException();
			}
		}

		guiEditor.setValue(value);

		firePropertyChange();
	}

    public void propertyChange(PropertyChangeEvent event)
    {
    	String text= guiEditor.getAsText();
		if (isValidValue(text))
		{
			lastValidValue= text;
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("Invalid value. Reverting to last valid value.");
			}
			// TODO: warn the user. Maybe with a pop-up? A bell?
    
			// Revert to the previously unselected (presumed valid!) value:
			guiEditor.setAsText(lastValidValue);
		}
    }
}
