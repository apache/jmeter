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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JTextField;

/**
 * This is Date mask control. Using this control we can pop up our date in the
 * text field. And this control is Devloped basically for  JDK1.3 and lower
 * version support. This control is similer to JSpinner control this is
 * available in JDK1.4 and above only.
 * <p>
 * This will set the date "MM/d/yyyy HH:mm:ss" in this format only.
 * </p>
 *
 * @author    T.Elanjchezhiyan
 * @version   $Revision$
 */
public class JDateField extends JTextField
{
    private final static DateFormat dateFormat =
        new SimpleDateFormat("MM/d/yyyy HH:mm:ss");

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
        catch (Exception e)
        {
            return new Date();
        }
    }

    /**
     * @author    T.Elanjchezhiyan
     * @version   $Revision$
     */
    class KeyFocus extends KeyAdapter
    {

        int mon = 0;
        int day = 0;
        int year = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;

        KeyFocus()
        {
        }

        /**
         * This method will increment year by each "KEY UP" operation.
         */
        public void incrementYear()
        {
            year = year + 1;
        }

        /**
         * This method will increment month by each "KEY UP" operation.
         */
        public void incrementMonth()
        {
            if (mon < 12)
            {
                mon = mon + 1;
            }
            else
            {
                mon = 1;
                incrementYear();
            }
        }

        /**
         * This method will increment day by each "KEY UP" operation.
         */
        public void incrementDay()
        {
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, mon - 1, day);
            int noofdays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (day < noofdays)
            {
                day = day + 1;
            }
            else
            {
                day = 1;
                incrementMonth();
            }
        }
        
        /**
         * This method will increment Hour by each "KEY UP" operation.
         */
        public void incrementHour()
        {
            if (hour < 23)
            {
                hour = hour + 1;
            }
            else
            {
                hour = 0;
                incrementDay();
            }
        }

        /**
         * This method will increment Minute by each "KEY UP" operation.
         */
        public void incrementMinute()
        {
            if (minute < 59)
            {
                minute = minute + 1;
            }
            else
            {
                minute = 0;
                incrementHour();
            }
        }

        /**
         * This method will increment Second by each "KEY UP" operation.
         */
        public void incrementSecond()
        {
            if (second < 59)
            {
                second = second + 1;
            }
            else
            {
                second = 0;
                incrementMinute();
            }
        }

        /**
         * Datetime string will be splited and stored into mon,year,day like.
         */
        public void parseDate(String datetime)
        {
            mon = Integer.parseInt(datetime.substring(0, 2));
            day = Integer.parseInt(datetime.substring(3, 5));
            year = Integer.parseInt(datetime.substring(6, 10));
            hour = Integer.parseInt(datetime.substring(11, 13));
            minute = Integer.parseInt(datetime.substring(14, 16));
            second = Integer.parseInt(datetime.substring(17, 19));
        }

        public void keyPressed(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_UP)
            {
                String datetime = getText();
                parseDate(datetime);
                int pos = getCaretPosition();
                if (pos < 3)
                { //first three character will increment the month
                    incrementMonth();
                }
                else if (pos < 6)
                { //3-6 character will increment the day
                    incrementDay();
                }
                else if (pos < 11)
                { //7-11 character will increment the year
                    incrementYear();
                }
                else if (pos < 14)
                { //12-14 character will increment the hour
                    incrementHour();
                }
                else if (pos < 17)
                { //15-17 character will increment the minute
                    incrementMinute();
                }
                else if (pos < 20)
                { //18-20 character will increment the second
                    incrementSecond();
                }
                setText(
                    concate(mon)
                        + "/"
                        + concate(day)
                        + "/"
                        + concate(year)
                        + " "
                        + concate(hour)
                        + ":"
                        + concate(minute)
                        + ":"
                        + concate(second));
                setCaretPosition(pos);
            }
            else if (e.getKeyCode() == KeyEvent.VK_DOWN)
            {
                String datetime = getText();
                parseDate(datetime);
                int pos = getCaretPosition();
                if (pos < 3)
                { //first 3 character will decrease the month
                    decrementMonth();
                }
                else if (pos < 6)
                { //3-6 character will decrease the day
                    decrementDay();
                }
                else if (pos < 11)
                { //7-11 character will decrease the year
                    decrementYear();
                }
                else if (pos < 14)
                { //12-14 character will decrease the hour
                    decrementHour();
                }
                else if (pos < 17)
                { //15-17 character will decrease the minute
                    decrementMinute();
                }
                else if (pos < 20)
                { //18-20 character will decrease the second
                    decrementSecond();
                }
                setText(
                    concate(mon)
                        + "/"
                        + concate(day)
                        + "/"
                        + concate(year)
                        + " "
                        + concate(hour)
                        + ":"
                        + concate(minute)
                        + ":"
                        + concate(second));
                setCaretPosition(pos);
            }
        }

        /**
         * This method will decrease year by each "KEY DOWN" operation.
         */
        public void decrementYear()
        {
            year = year - 1;
        }
        
        /**
         * This method will decrease month by each "KEY DOWN" operation.
         */
        public void decrementMonth()
        {
            if (mon > 1)
            {
                mon = mon - 1;
            }
            else
            {
                mon = 12;
                decrementYear();
            }
        }

        /**
         * This method will decrease day by each "KEY DOWN" operation.
         */
        public void decrementDay()
        {
            Calendar calendar = new GregorianCalendar();
            if (day > 1)
            {
                day = day - 1;
            }
            else
            {
                if (mon == 1)
                {
                    calendar.set(year, 11, day);
                }
                else
                {
                    calendar.set(year, mon - 2, day);
                }
                int noofdays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                day = noofdays;
                decrementMonth();
            }
        }

        /**
         * This method will decrease hour by each "KEY DOWN" operation.
         */
        public void decrementHour()
        {
            if (hour > 0)
            {
                hour = hour - 1;
            }
            else
            {
                hour = 23;
                decrementDay();
            }
        }

        /**
         * This method will decrease minute by each "KEY DOWN" operation.
         */
        public void decrementMinute()
        {
            if (minute > 0)
            {
                minute = minute - 1;
            }
            else
            {
                minute = 59;
                decrementHour();
            }
        }
        
        /**
         * This method will decrease second by each "KEY DOWN" operation.
         */
        public void decrementSecond()
        {
            if (second > 0)
            {
                second = second - 1;
            }
            else
            {
                second = 59;
                decrementMinute();
            }
        }

        /**
         * Before the Single digit number it will Appends '0'.  For example,
         * 1 => '01'.
         */
        public String concate(int number)
        {
            String value;
            if (String.valueOf(number).length() == 1)
            {
                value = "0" + number;
            }
            else
            {
                value = String.valueOf(number);
            }
            return value;
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
                if (new Date(getText()) instanceof Date)
                { //this will check the date formate
                }
            }
            catch (Exception ex)
            {
                requestFocus();
            }
        }
    }
}
