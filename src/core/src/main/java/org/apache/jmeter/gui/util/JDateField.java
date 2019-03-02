/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
import javax.swing.SwingUtilities;

/**
 * This is Date mask control. Using this control we can pop up our date in the
 * text field. And this control is developed basically for JDK1.3 and lower
 * version support. This control is similar to JSpinner control this is
 * available in JDK1.4 and above only.
 * <p>
 * This will set the date "yyyy/MM/dd HH:mm:ss" in this format only.
 * </p>
 */
public class JDateField extends JTextField {

    private static final long serialVersionUID = 240L;

    // Datefields are not thread-safe
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // $NON-NLS-1$

    /*
     * The following array must agree with dateFormat
     *
     * It is used to translate the positions in the buffer to the values used by
     * the Calendar class for the field id.
     *
     * Current format: MM/DD/YYYY HH:MM:SS 01234567890123456789 ^buffer
     * positions
     */
    private static final int[] FIELD_POSITIONS = {
            Calendar.YEAR, // Y
            Calendar.YEAR, // Y
            Calendar.YEAR, // Y
            Calendar.YEAR, // Y
            Calendar.YEAR, // sp
            Calendar.MONTH, // M
            Calendar.MONTH, // M
            Calendar.MONTH, // /
            Calendar.DAY_OF_MONTH, // D
            Calendar.DAY_OF_MONTH, // D
            Calendar.DAY_OF_MONTH, // /
            Calendar.HOUR_OF_DAY, // H
            Calendar.HOUR_OF_DAY, // H
            Calendar.HOUR_OF_DAY, // :
            Calendar.MINUTE, // M
            Calendar.MINUTE, // M
            Calendar.MINUTE, // :
            Calendar.SECOND, // S
            Calendar.SECOND, // S
            Calendar.SECOND // end
    };

    /**
     * Create a DateField with the specified date.
     *
     * @param date
     *            The {@link Date} to be used
     */
    public JDateField(Date date) {
        super(20);
        this.addKeyListener(new KeyFocus());
        this.addFocusListener(new FocusClass());
        String myString = dateFormat.format(date);
        setText(myString);
    }

    // Dummy constructor to allow JUnit tests to work
    public JDateField() {
        this(new Date());
    }

    /**
     * Set the date to the Date mask control.
     *
     * @param date
     *            The {@link Date} to be set
     */
    public void setDate(Date date) {
        setText(dateFormat.format(date));
    }

    /**
     * Get the date from the Date mask control.
     *
     * @return The currently set date
     */
    public Date getDate() {
        try {
            return dateFormat.parse(getText());
        } catch (ParseException e) {
            return new Date();
        }
    }

    /*
     * Convert position in buffer to Calendar type Assumes that pos >=0 (which
     * is true for getCaretPosition())
     */
    private static int posToField(int pos) {
        if (pos >= FIELD_POSITIONS.length) { // if beyond the end
            pos = FIELD_POSITIONS.length - 1; // then set to the end
        }
        return FIELD_POSITIONS[pos];
    }

    /**
     * Converts a date/time to a calendar using the defined format
     */
    private Calendar parseDate(String datetime) {
        Calendar c = Calendar.getInstance();
        try {
            Date dat = dateFormat.parse(datetime);
            c.setTime(dat);
        } catch (ParseException e) {
            // Do nothing; the current time will be returned
        }
        return c;
    }

    /*
     * Update the current field. The addend is only expected to be +1/-1, but
     * other values will work. N.B. the roll() method only supports changes by a
     * single unit - up or down
     */
    private void update(int addend, boolean shifted) {
        Calendar c = parseDate(getText());
        int pos = getCaretPosition();
        int field = posToField(pos);
        if (shifted) {
            c.roll(field, true);
        } else {
            c.add(field, addend);
        }
        String newDate = dateFormat.format(c.getTime());
        setText(newDate);
        if (pos > newDate.length()) {
            pos = newDate.length();
        }
        final int newPosition = pos;
        SwingUtilities.invokeLater(() -> setCaretPosition(newPosition));
    }

    class KeyFocus extends KeyAdapter {
        KeyFocus() {}

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                update(1, e.isShiftDown());
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                update(-1, e.isShiftDown());
            }
        }
    }

    class FocusClass implements FocusListener {
        FocusClass() {}

        @Override
        public void focusGained(FocusEvent e) {}

        @Override
        public void focusLost(FocusEvent e) {
            if(getText() == null || getText().isEmpty()) {
                return;
            }
            try {
                dateFormat.parse(getText());
            } catch (ParseException e1) {
                requestFocusInWindow();
            }
        }
    }
}
