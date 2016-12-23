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
 */

package org.apache.jorphan.gui;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class TableModelEventBacker implements TableModelListener {

    public class EventAssertion {
        private List<ObjIntConsumer<TableModelEvent>> assertions = new ArrayList<>();

        public EventAssertion add(ObjIntConsumer<TableModelEvent> assertion) {
            assertions.add(assertion);
            return this;
        }

        public EventAssertion addInt(String name, int expected, ToIntFunction<TableModelEvent> f) {
            return add((e,i) -> assertEquals(format("%s[%d]", name, i), expected, f.applyAsInt(e)));
        }

        public EventAssertion source(Object expected) {
            return add((e,i) -> assertSame(format("source[%d]",i), expected, e.getSource()));
        }

        public EventAssertion type(int expected) {
            return addInt("type", expected, TableModelEvent::getType);
        }

        public EventAssertion column(int expected) {
            return addInt("column", expected, TableModelEvent::getColumn);
        }

        public EventAssertion firstRow(int expected) {
            return addInt("firstRow", expected, TableModelEvent::getFirstRow);
        }

        public EventAssertion lastRow(int expected) {
            return addInt("lastRow", expected, TableModelEvent::getLastRow);
        }

        protected void assertEvent(TableModelEvent e, int i) {
            assertions.forEach(a -> a.accept(e, i));
        }
    }

    private Deque<TableModelEvent> events = new LinkedList<>();

    @Override
    public void tableChanged(TableModelEvent e) {
        events.add(e);
    }

    public Deque<TableModelEvent> getEvents() {
        return events;
    }

    public EventAssertion assertEvent() {
        return new EventAssertion();
    }

    public void assertEvents(EventAssertion... assertions) {
        assertEquals("event count", assertions.length, events.size());

        int i = 0;
        for (TableModelEvent event : events) {
            assertions[i].assertEvent(event, i++);
        }

        events.clear();
    }


}
