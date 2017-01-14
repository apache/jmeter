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
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.IntStream;

import javax.swing.event.TableModelEvent;

import org.apache.jorphan.reflect.Functor;
import org.junit.Before;
import org.junit.Test;

public class ObjectTableModelTest {

    public static class Dummy {
        String a;
        String b;
        String c;

        Dummy(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public String getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public String getC() {
            return c;
        }
    }

    ObjectTableModel model;
    TableModelEventBacker events;

    @Before
    public void init() {
        String[] headers = { "a", "b", "c" };
        Functor[] readFunctors = Arrays.stream(headers).map(name -> "get" + name.toUpperCase()).map(Functor::new).toArray(n -> new Functor[n]);
        Functor[] writeFunctors = new Functor[headers.length];
        Class<?>[] editorClasses = new Class<?>[headers.length];
        Arrays.fill(editorClasses, String.class);
        model = new ObjectTableModel(headers, readFunctors, writeFunctors, editorClasses);
        events = new TableModelEventBacker();
    }

    @Test
    public void checkAddRow() {
        model.addTableModelListener(events);

        assertModel();

        model.addRow(new Dummy("1", "1", "1"));
        assertModel("1");
        events.assertEvents(
                events.assertEvent()
                    .source(model)
                    .type(TableModelEvent.INSERT)
                    .column(TableModelEvent.ALL_COLUMNS)
                    .firstRow(0)
                    .lastRow(0)
        );

        model.addRow(new Dummy("2", "1", "1"));
        assertModel("1", "2");
        events.assertEvents(
                events.assertEvent()
                    .source(model)
                    .type(TableModelEvent.INSERT)
                    .column(TableModelEvent.ALL_COLUMNS)
                    .firstRow(1)
                    .lastRow(1)
        );
    }

    @Test
    public void checkClear() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            model.addRow(new Dummy("" + i, "" + i%2, "" + i%3));
        }
        assertModelRanges(range(0,5));

        // Act
        model.addTableModelListener(events);
        model.clearData();

        // Assert
        assertModelRanges();


        events.assertEvents(
                events.assertEvent()
                    .source(model)
                    .type(TableModelEvent.UPDATE)
                    .column(TableModelEvent.ALL_COLUMNS)
                    .firstRow(0)
                    .lastRow(Integer.MAX_VALUE)
        );
    }

    @Test
    public void checkInsertRow() {
        assertModel();
        model.addRow(new Dummy("3", "1", "1"));
        assertModel("3");
        model.addTableModelListener(events);

        model.insertRow(new Dummy("1", "1", "1"), 0);
        assertModel("1", "3");
        events.assertEvents(
                events.assertEvent()
                    .source(model)
                    .type(TableModelEvent.INSERT)
                    .column(TableModelEvent.ALL_COLUMNS)
                    .firstRow(0)
                    .lastRow(0)
       );

       model.insertRow(new Dummy("2", "1", "1"), 1);
       assertModel("1", "2", "3");
       events.assertEvents(
               events.assertEvent()
                   .source(model)
                   .type(TableModelEvent.INSERT)
                   .column(TableModelEvent.ALL_COLUMNS)
                   .firstRow(1)
                   .lastRow(1)
      );


    }

    @Test
    public void checkMoveRow_from_5_11_to_0() {
        // Arrange
        for (int i = 0; i < 20; i++) {
            model.addRow(new Dummy("" + i, "" + i%2, "" + i%3));
        }
        assertModelRanges(range(0, 20));

        // Act
        model.addTableModelListener(events);
        model.moveRow(5, 11, 0);

        // Assert
        assertModelRanges(range(5, 11), range(0, 5), range(11, 20));

        events.assertEvents(
                events.assertEvent()
                    .source(model)
                    .type(TableModelEvent.UPDATE)
                    .column(TableModelEvent.ALL_COLUMNS)
                    .firstRow(0)
                    .lastRow(Integer.MAX_VALUE)
        );
    }

    @Test
    public void checkMoveRow_from_0_6_to_0() {
        // Arrange
        for (int i = 0; i < 20; i++) {
            model.addRow(new Dummy("" + i, "" + i%2, "" + i%3));
        }
        assertModelRanges(range(0, 20));

        // Act
        model.addTableModelListener(events);
        model.moveRow(0, 6, 0);

        // Assert
        assertModelRanges(range(0, 20));

        events.assertEvents(
                events.assertEvent()
                    .source(model)
                    .type(TableModelEvent.UPDATE)
                    .column(TableModelEvent.ALL_COLUMNS)
                    .firstRow(0)
                    .lastRow(Integer.MAX_VALUE)
        );
    }

    @Test
    public void checkMoveRow_from_0_6_to_10() {
        // Arrange
        for (int i = 0; i < 20; i++) {
            model.addRow(new Dummy("" + i, "" + i%2, "" + i%3));
        }
        assertModelRanges(range(0, 20));

        // Act
        model.addTableModelListener(events);
        model.moveRow(0, 6, 10);

        // Assert
        assertModelRanges(range(6, 16), range(0, 6), range(16, 20));

        events.assertEvents(
                events.assertEvent()
                    .source(model)
                    .type(TableModelEvent.UPDATE)
                    .column(TableModelEvent.ALL_COLUMNS)
                    .firstRow(0)
                    .lastRow(Integer.MAX_VALUE)
        );
    }

    private void assertModelRanges(IntStream... ranges) {
        IntStream ints = IntStream.empty();
        for (IntStream range : ranges) {
            ints = IntStream.concat(ints, range);
        }
        assertModel(ints.mapToObj(i -> "" + i).toArray(n -> new String[n]));
    }

    private void assertModel(String... as) {
        assertEquals("model row count", as.length, model.getRowCount());

        for (int row = 0; row < as.length; row++) {
            assertEquals(format("model[%d,0]", row), as[row], model.getValueAt(row, 0));
        }
    }

}
