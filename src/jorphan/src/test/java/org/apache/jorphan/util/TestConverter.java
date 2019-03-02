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

package org.apache.jorphan.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * Tests for {@link Converter}
 *
 */
public class TestConverter {

    /**
     * Test {@link Converter#getCalendar(Object, Calendar)} with a given Date
     * and null as default value
     */
    @Test
    public void testGetCalendarObjectCalendarWithTimeAndNullDefault() {
        Calendar cal = new GregorianCalendar();
        Date time = cal.getTime();
        assertEquals(cal, Converter.getCalendar(time, null));
    }

    /**
     * Test {@link Converter#getCalendar(Object, Calendar)} with null as Date
     * and a sensible default value
     */
    @Test
    public void testGetCalendarObjectCalendarWithNullAndCalendarAsDefault() {
        Calendar cal = new GregorianCalendar();
        assertEquals(cal, Converter.getCalendar(null, cal));
    }

    /**
     * Test {@link Converter#getCalendar(Object, Calendar)} with correctly
     * formatted strings and <code>null</code> as default value
     */
    @Test
    public void testGetCalendarObjectCalendarWithValidStringAndNullDefault() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        for (int formatId : new int[]{DateFormat.SHORT, DateFormat.MEDIUM,
                DateFormat.LONG, DateFormat.FULL}) {
            DateFormat formatter = DateFormat.getDateInstance(formatId);
            assertEquals(cal,
                    Converter.getCalendar(formatter.format(time), null));
        }
    }

    /**
     * Test {@link Converter#getCalendar(Object, Calendar)} with an invalid
     * string and <code>null</code> as default value
     */
    @Test
    public void testGetCalendarObjectCalendarWithInvalidStringAndNullDefault() {
        assertNull(Converter.getCalendar("invalid date", null));
    }

    /**
     * Test {@link Converter#getDate(Object, Date)} with a given Date
     * and null as default value
     */
    @Test
    public void testGetDateObjectDateWithTimeAndNullDefault() {
        Date time = new Date();
        assertEquals(time, Converter.getDate(time, null));
    }

    /**
     * Test {@link Converter#getDate(Object, Date)} with null as Date
     * and a sensible default value
     */
    @Test
    public void testGetDateObjectDateWithNullAndDateAsDefault() {
        Date date = new Date();
        assertEquals(date, Converter.getDate(null, date));
    }

    /**
     * Test {@link Converter#getDate(Object, Date)} with correctly
     * formatted strings and <code>null</code> as default value
     */
    @Test
    public void testGetDateObjectDateWithValidStringAndNullDefault() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        for (int formatId : new int[]{DateFormat.SHORT, DateFormat.MEDIUM,
                DateFormat.LONG, DateFormat.FULL}) {
            DateFormat formatter = DateFormat.getDateInstance(formatId);
            assertEquals(time,
                    Converter.getDate(formatter.format(time), null));
        }
    }

    /**
     * Test {@link Converter#getDate(Object, Date)} with an invalid
     * string and <code>null</code> as default value
     */
    @Test
    public void testGetDateObjectDateWithInvalidStringAndNullDefault() {
        assertNull(Converter.getDate("invalid date", null));
    }

}
