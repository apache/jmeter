/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
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

package org.apache.jmeter.gui.util;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JTextField;

/**
 * This is Date mask control. Using this control we can pop up our date in the
 * text field. And this control is Devloped basically for  JDK1.3 and lower
 * version support. This control is similer to JSpinner control this is
 * available in JDK1.4 and above only.
 * <p>
 * This will set the date "yyyy/MM/dd HH:mm:ss" in this format only.
 * </p>
 *
 * @author    T.Elanjchezhiyan
 * @version   $Revision$ Last update: $Date$
 */
public class JDateField extends JTextField
{
    private final static DateFormat dateFormat =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /*
     * The following array must agree with dateFormat
     * 
     * It is used to translate the positions in the buffer
     * to the values used by the Calendar class for the field id.
     *
     * Current format: 
     * MM/DD/YYYY HH:MM:SS
     * 01234567890123456789
     * ^buffer positions
     */
	private static int fieldPositions [] = {
		Calendar.YEAR,         // Y
		Calendar.YEAR,         // Y
		Calendar.YEAR,         // Y
		Calendar.YEAR,         // Y
		Calendar.YEAR,         // sp
		Calendar.MONTH,        // M
		Calendar.MONTH,        // M
		Calendar.MONTH,        // /
		Calendar.DAY_OF_MONTH, // D
		Calendar.DAY_OF_MONTH, // D
		Calendar.DAY_OF_MONTH, // /
		Calendar.HOUR_OF_DAY,  // H
		Calendar.HOUR_OF_DAY,  // H
		Calendar.HOUR_OF_DAY,  // :
		Calendar.MINUTE,       // M 
		Calendar.MINUTE,       // M
		Calendar.MINUTE,       // :
		Calendar.SECOND,       // S
		Calendar.SECOND,       // S
		Calendar.SECOND        // end
		};

    /**
     * Create a DateField with the specified date.
     */
    public JDateField(Date date)
    {
        super(20);
        this.addKeyListener(new KeyFocus());
        this.addFocusListener(new FocusClass());
        String myString = dateFormat.format(date);
        setText(myString);
    }

    // Dummy constructor to allo JUnit tests to work
    JDateField()
    {
    	this(new Date());
    }
    
    /**
     * Set the date to the Date mask control.
     */
    public void setDate(Date date)
    {
        setText(dateFormat.format(date));
    }

    /**
     * Get the date from the Date mask control.
     */
    public Date getDate()
    {
        try
        {
            return dateFormat.parse(getText());
        }
        catch (ParseException e)
        {
            return new Date();
        }
        catch (Exception e)
        {
            // DateFormat.parse has some bugs (up to JDK 1.4.2) by which it
            // throws unchecked exceptions. E.g. see:
            // http://developer.java.sun.com/developer/bugParade/bugs/4699765.html
            //
            // To avoid problems with such situations, we'll catch all
            // exceptions here and act just as for ParseException above:
            return new Date(); 
        }
    }

	/*
	 * Convert position in buffer to Calendar type
	 * Assumes that pos >=0 (which is true for getCaretPosition())
	 */
	private static int posToField(int pos){
		if (pos >= fieldPositions.length) {  // if beyond the end
			pos = fieldPositions.length - 1; // then set to the end
		}
		return fieldPositions[pos];
	}


	/**
	 * Converts a date/time to a calendar using the defined format
	 */
	private static Calendar parseDate(String datetime)
	{
		Calendar c = Calendar.getInstance();
		try
		{
			Date dat = dateFormat.parse(datetime);
			c.setTime(dat);
		}
		catch (ParseException e)
		{
			//Do nothing; the current time will be returned
		}
		return c;
	}
	
	/*
	 * Update the current field. The addend is only expected to be +1/-1,
	 * but other values will work.
	 * N.B. the roll() method only supports changes by a single unit - up or down
	 */
	private void update(int addend, boolean shifted){
		Calendar c = parseDate(getText());
		int pos = getCaretPosition();
		int field = posToField(pos);
		if (shifted){
			c.roll(field,true);
		} else {
			c.add(field,addend);
		}
		String newDate =dateFormat.format(c.getTime());
		setText(newDate);
		if (pos > newDate.length()) pos = newDate.length();
		setCaretPosition(pos);// Restore position
		
	}
    /**
     * @author    T.Elanjchezhiyan
     * @version   $Revision$
     */
    class KeyFocus extends KeyAdapter
    {
        KeyFocus()
        {
        }
        
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_UP)
			{
				update(1,e.isShiftDown());
			}
			else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				update(-1,e.isShiftDown());
			}
		}
    }

    /**
     * @author    T.Elanjchezhiyan
     * @version   $Revision$
     */
    class FocusClass implements FocusListener
    {
        FocusClass()
        {
        }
        public void focusGained(FocusEvent e)
        {
        }
        public void focusLost(FocusEvent e)
        {
			try
            {
                dateFormat.parse(getText());
            }
            catch (ParseException e1)
            {
                requestFocus();
            }
        }
    }
}
