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

/**
 * Listener implementation that stores {@link TableModelEvent} and can make assertions against them.
 */
public class TableModelEventBacker implements TableModelListener {

    /**
     * Makes assertions for a single {@link TableModelEvent}.
     */
    public class EventAssertion {
        private List<ObjIntConsumer<TableModelEvent>> assertions = new ArrayList<>();

        /**
         * Adds an assertion first args is table model event, second one is event index.
         * @param assertion assertion to add
         * @return <code>this</code>
         */
        public EventAssertion add(ObjIntConsumer<TableModelEvent> assertion) {
            assertions.add(assertion);
            return this;
        }

        /**
         * Adds assertion based on a {@link ToIntFunction to-int} transformation (examples: <code>TableModelEvent::getType</code>).
         * @param name Label for assertion reason
         * @param expected Expected value.
         * @param f {@link ToIntFunction to-int} transformation (examples: <code>TableModelEvent::getType</code>).
         * @return <code>this</code>
         */
        public EventAssertion addInt(String name, int expected, ToIntFunction<TableModelEvent> f) {
            return add((e,i) -> assertEquals(format("%s[%d]", name, i), expected, f.applyAsInt(e)));
        }

        /**
         * Adds {@link TableModelEvent#getSource()} assertion.
         * @param expected Object to compare against the source of the event
         * @return <code>this</code>
         */
        public EventAssertion source(Object expected) {
            return add((e,i) -> assertSame(format("source[%d]",i), expected, e.getSource()));
        }

        /**
         * Adds {@link TableModelEvent#getType()} assertion.
         * @param expected int value of the type to compare against
         * @return <code>this</code>
         */
        public EventAssertion type(int expected) {
            return addInt("type", expected, TableModelEvent::getType);
        }

        /**
         * Adds {@link TableModelEvent#getColumn()} assertion.
         * @param expected int value of the column to compare against
         * @return <code>this</code>
         */
        public EventAssertion column(int expected) {
            return addInt("column", expected, TableModelEvent::getColumn);
        }

        /**
         * Adds {@link TableModelEvent#getFirstRow()} assertion.
         * @param expected int value of the first row that should have changed
         * @return <code>this</code>
         */
        public EventAssertion firstRow(int expected) {
            return addInt("firstRow", expected, TableModelEvent::getFirstRow);
        }

        /**
         * Adds {@link TableModelEvent#getLastRow()} assertion.
         * @param expected int value of the last row that should have changed
         * @return <code>this</code>
         */
        public EventAssertion lastRow(int expected) {
            return addInt("lastRow", expected, TableModelEvent::getLastRow);
        }

        /**
         * Check assertion against provided value.
         * @param event Event to check
         * @param index Index.
         */
        protected void assertEvent(TableModelEvent event, int index) {
            assertions.forEach(a -> a.accept(event, index));
        }
    }

    private Deque<TableModelEvent> events = new LinkedList<>();

    /**
     * Stores event.
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        events.add(e);
    }

    public Deque<TableModelEvent> getEvents() {
        return events;
    }

    /**
     * Creates a new event assertion.
     * @return a newly created {@link EventAssertion}
     * @see #assertEvents(EventAssertion...)
     */
    public EventAssertion assertEvent() {
        return new EventAssertion();
    }

    /**
     * Checks each event assertion against each backed event in order. Event storage is cleared after it.
     * @param assertions a collection if {@link EventAssertion}s to check
     */
    public void assertEvents(EventAssertion... assertions) {
        try {
            assertEquals("event count", assertions.length, events.size());

            int i = 0;
            for (TableModelEvent event : events) {
                assertions[i].assertEvent(event, i++);
            }
        } finally {
            events.clear();
        }
    }


}
