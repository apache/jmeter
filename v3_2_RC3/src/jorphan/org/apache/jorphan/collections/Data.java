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

package org.apache.jorphan.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;

/**
 * Use this class to store database-like data. This class uses rows and columns
 * to organize its data. It has some convenience methods that allow fast loading
 * and retrieval of the data into and out of string arrays. It is also handy for
 * reading CSV files.
 *
 * WARNING: the class assumes that column names are unique, but does not enforce this.
 * 
 */
public class Data implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(Data.class);

    private static final long serialVersionUID = 240L;

    private final Map<String, List<Object>> data;

    private List<String> header;

    // saves current position in data List
    private int currentPos; 
    private int size;

    /**
     * Constructor - takes no arguments.
     */
    public Data() {
        header = new ArrayList<>();
        data = new HashMap<>();
        currentPos = -1;
        size = currentPos + 1;
    }

    /**
     * Replaces the given header name with a new header name.
     *
     * @param oldHeader
     *            Old header name.
     * @param newHeader
     *            New header name.
     */
    public void replaceHeader(String oldHeader, String newHeader) {
        List<Object> tempList;
        int index = header.indexOf(oldHeader);
        header.set(index, newHeader);
        tempList = data.remove(oldHeader);
        data.put(newHeader, tempList);
    }

    /**
     * Adds the rows of the given Data object to this Data object.
     *
     * @param d
     *            data object to be appended to this one
     */
    public void append(Data d) {
        boolean valid = true;
        String[] headers = getHeaders();
        String[] dHeaders = d.getHeaders();
        if (headers.length != dHeaders.length) {
            valid = false;
        } else {
            for (String dHeader : dHeaders) {
                if (!header.contains(dHeader)) {
                    valid = false;
                    break;
                }
            }
        }

        if (valid) {
            currentPos = size;
            d.reset();
            while (d.next()) {
                for (String aHeader : headers) {
                    addColumnValue(aHeader, d.getColumnValue(aHeader));
                }
            }
        }
    }

    /**
     * Get the number of the current row.
     *
     * @return integer representing the current row
     */
    public int getCurrentPos() {
        return currentPos;
    }

    /**
     * Removes the current row.
     */
    public void removeRow() {
        List<Object> tempList;
        Iterator<String> it = data.keySet().iterator();
        log.debug("removing row, size = " + size);
        if (currentPos > -1 && currentPos < size) {
            log.debug("got to here");
            while (it.hasNext()) {
                tempList = data.get(it.next());
                tempList.remove(currentPos);
            }
            if (currentPos > 0) {
                currentPos--;
            }
            size--;
        }
    }

    public void removeRow(int index) {
        log.debug("Removing row: " + index);
        if (index < size) {
            setCurrentPos(index);
            log.debug("Setting currentpos to " + index);
            removeRow();
        }
    }

    public void addRow() {
        String[] headers = getHeaders();
        List<Object> tempList = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            if ((tempList = data.get(header.get(i))) == null) {
                tempList = new ArrayList<>();
                data.put(headers[i], tempList);
            }
            tempList.add("");
        }
        size = tempList.size();
        setCurrentPos(size - 1);
    }

    /**
     * Sets the current pos. If value sent to method is not a valid number, the
     * current position is set to one higher than the maximum.
     *
     * @param r
     *            position to set to.
     */
    public void setCurrentPos(int r) {
        currentPos = r;
    }

    /**
     * Sorts the data using a given row as the sorting criteria. A boolean value
     * indicates whether to sort ascending or descending.
     *
     * @param column
     *            name of column to use as sorting criteria.
     */
    public void sort(String column) {
        sortData(column, 0, size);
    }

    private void swapRows(int row1, int row2) {
        for (Map.Entry<String, List<Object>> entry : data.entrySet()) {
            List<Object> temp = entry.getValue();
            Object o = temp.get(row1);
            temp.set(row1, temp.get(row2));
            temp.set(row2, o);
        }
    }

    /**
     * Private method that implements the quicksort algorithm to sort the rows
     * of the Data object.
     *
     * @param column
     *            name of column to use as sorting criteria.
     * @param start
     *            starting index (for quicksort algorithm).
     * @param end
     *            ending index (for quicksort algorithm).
     */
    private void sortData(String column, int start, int end) {
        int x = start;
        int y = end - 1;
        String basis = data.get(column).get((x + y) / 2).toString();
        if (x == y) {
            return;
        }

        while (x <= y) {
            while (x < end && data.get(column).get(x).toString().compareTo(basis) < 0) {
                x++;
            }

            while (y >= (start - 1) && data.get(column).get(y).toString().compareTo(basis) > 0) {
                y--;
            }

            if (x <= y) {
                swapRows(x, y);
                x++;
                y--;
            }
        }

        if (x == y) {
            x++;
        }

        y = end - x;

        if (x > 0) {
            sortData(column, start, x);
        }

        if (y > 0) {
            sortData(column, x, end);
        }
    }

    /**
     * Gets the number of rows in the Data object.
     *
     * @return number of rows in Data object.
     */
    public int size() {
        return size;
    } // end method

    /**
     * Adds a value into the Data set at the current row, using a column name to
     * find the column in which to insert the new value.
     *
     * @param column
     *            the name of the column to set.
     * @param value
     *            value to set into column.
     */
    public void addColumnValue(String column, Object value) {
        List<Object> tempList;
        if ((tempList = data.get(column)) == null) {
            tempList = new ArrayList<>();
            data.put(column, tempList);
        }
        int s = tempList.size();
        if (currentPos == -1) {
            currentPos = size;
        }

        if (currentPos >= size) {
            size = currentPos + 1;
        }

        while (currentPos > s) {
            s++;
            tempList.add(null);
        }

        if (currentPos == s) {
            tempList.add(value);
        } else {
            tempList.set(currentPos, value);
        }
    }

    /**
     * Returns the row number where a certain value is.
     *
     * @param column
     *            column to be searched for value.
     * @param value
     *            object in Search of.
     * @return row # where value exists.
     */
    public int findValue(String column, Object value) {
        return data.get(column).indexOf(value);
    }

    /**
     * Sets the value in the Data set at the current row, using a column name to
     * find the column in which to insert the new value.
     *
     * @param column
     *            the name of the column to set.
     * @param value
     *            value to set into column.
     */
    public void setColumnValue(String column, Object value) {
        List<Object> tempList;
        if ((tempList = data.get(column)) == null) {
            tempList = new ArrayList<>();
            data.put(column, tempList);
        }

        if (currentPos == -1) {
            currentPos = 0;
        }

        if (currentPos >= size) {
            size++;
            tempList.add(value);
        } else if (currentPos >= tempList.size()) {
            tempList.add(value);
        } else {
            tempList.set(currentPos, value);
        }
    }

    /**
     * Checks to see if a column exists in the Data object.
     *
     * @param column
     *            Name of column header to check for.
     * @return True or False depending on whether the column exists.
     */
    public boolean hasHeader(String column) {
        return data.containsKey(column);
    }

    /**
     * Sets the current position of the Data set to the next row.
     *
     * @return True if there is another row. False if there are no more rows.
     */
    public boolean next() {
        return ++currentPos < size;
    }

    /**
     * Sets the current position of the Data set to the previous row.
     *
     * @return True if there is another row. False if there are no more rows.
     */
    public boolean previous() {
        return --currentPos >= 0;
    }

    /**
     * Resets the current position of the data set to just before the first
     * element.
     */
    public void reset() {
        currentPos = -1;
    }

    /**
     * Gets the value in the current row of the given column.
     *
     * @param column
     *            name of the column.
     * @return an Object which holds the value of the column.
     */
    public Object getColumnValue(String column) {
        try {
            if (currentPos < size) {
                return data.get(column).get(currentPos);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the value in the current row of the given column.
     *
     * @param column
     *            index of the column (starts at 0).
     * @return an Object which holds the value of the column.
     */
    public Object getColumnValue(int column) {
        String columnName = header.get(column);
        return getColumnValue(columnName);
    }

    public Object getColumnValue(int column, int row) {
        setCurrentPos(row);
        return getColumnValue(column);
    }

    public void removeColumn(int col) {
        String columnName = header.get(col);
        data.remove(columnName);
        header.remove(columnName);
    }

    /**
     * Sets the headers for the data set. Each header represents a column of
     * data. Each row's data can be gotten with the column header name, which
     * will always be a string.
     *
     * @param h
     *            array of strings representing the column headers.
     *            these must be distinct - duplicates will cause incorrect behaviour
     */
    public void setHeaders(String[] h) {
        header = new ArrayList<>(h.length);
        for (int x = 0; x < h.length; x++) {
            header.add(h[x]);
            data.put(h[x], new ArrayList<>());
        }
    }

    /**
     * Returns a String array of the column headers.
     *
     * @return array of strings of the column headers.
     */
    public String[] getHeaders() {
        String[] r = new String[header.size()];
        if (r.length > 0) {
            r = header.toArray(r);
        }
        return r;
    }

    public int getHeaderCount(){
        return header.size();
    }

    /**
     * This method will retrieve every entry in a certain column. It returns an
     * array of Objects from the column.
     *
     * @param columnName
     *            name of the column.
     * @return array of Objects representing the data.
     */
    public List<Object> getColumnAsObjectArray(String columnName) {
        return data.get(columnName);
    }

    /**
     * This method will retrieve every entry in a certain column. It returns an
     * array of strings from the column. Even if the data are not strings, they
     * will be returned as strings in this method.
     *
     * @param columnName
     *            name of the column.
     * @return array of Strings representing the data.
     */
    public String[] getColumn(String columnName) {
        String[] returnValue;
        List<?> temp = data.get(columnName);
        if (temp != null) {
            returnValue = new String[temp.size()];
            int index = 0;
            for (Object o : temp) {
                if (o != null) {
                    if (o instanceof String) {
                        returnValue[index++] = (String) o;
                    } else {
                        returnValue[index++] = o.toString();
                    }
                }
            }
        } else {
            returnValue = new String[0];
        }
        return returnValue;
    }

    /**
     * Use this method to set the entire data set. It takes an array of strings.
     * It uses the first row as the headers, and the next rows as the data
     * elements. Delimiter represents the delimiting character(s) that separate
     * each item in a data row.
     *
     * @param contents
     *            array of strings, the first element is a list of the column
     *            headers, the next elements each represent a single row of
     *            data.
     * @param delimiter
     *            the delimiter character that separates columns within the
     *            string array.
     */
    public void setData(String[] contents, String delimiter) {
        setHeaders(JOrphanUtils.split(contents[0], delimiter));
        int x = 1;
        while (x < contents.length) {
            setLine(JOrphanUtils.split(contents[x++], delimiter));
        }
    }

    /**
     * Sets the data for every row in the column.
     *
     * @param colName
     *            name of the column
     * @param value
     *            value to be set
     */
    public void setColumnData(String colName, Object value) {
        List<Object> list = this.getColumnAsObjectArray(colName);
        while (list.size() < size()) {
            list.add(value);
        }
    }

    public void setColumnData(int col, List<?> data) {
        reset();
        Iterator<?> iter = data.iterator();
        String columnName = header.get(col);
        while (iter.hasNext()) {
            next();
            setColumnValue(columnName, iter.next());
        }
    }

    /**
     * Adds a header name to the Data object.
     *
     * @param s
     *            name of header.
     */
    public void addHeader(String s) {
        header.add(s);
        data.put(s, new ArrayList<>(Math.max(size(), 100)));
    }

    /**
     * Sets a row of data using an array of strings as input. Each value in the
     * array represents a column's value in that row. Assumes the order will be
     * the same order in which the headers were added to the data set.
     *
     * @param line
     *            array of strings representing column values.
     */
    public void setLine(String[] line) {
        List<Object> tempList;
        String[] h = getHeaders();
        for (int count = 0; count < h.length; count++) {
            tempList = data.get(h[count]);
            if (count < line.length && line[count].length() > 0) {
                tempList.add(line[count]);
            } else {
                tempList.add("N/A");
            }
        }
        size++;
    }

    /**
     * Sets a row of data using an array of strings as input. Each value in the
     * array represents a column's value in that row. Assumes the order will be
     * the same order in which the headers were added to the data set.
     *
     * @param line
     *            array of strings representing column values.
     * @param deflt
     *            default value to be placed in data if line is not as long as
     *            headers.
     */
    public void setLine(String[] line, String deflt) {
        List<Object> tempList;
        String[] h = getHeaders();
        for (int count = 0; count < h.length; count++) {
            tempList = data.get(h[count]);
            if (count < line.length && line[count].length() > 0) {
                tempList.add(line[count]);
            } else {
                tempList.add(deflt);
            }
        }
        size++;
    }

    /**
     * Returns all the data in the Data set as an array of strings. Each array
     * gives a row of data, each column separated by tabs.
     *
     * @return array of strings.
     */
    public String[] getDataAsText() {
        StringBuilder temp = new StringBuilder("");
        String[] line = new String[size + 1];
        String[] elements = getHeaders();
        for (int count = 0; count < elements.length; count++) {
            temp.append(elements[count]);
            if (count + 1 < elements.length) {
                temp.append("\t");
            }
        }
        line[0] = temp.toString();
        reset();
        int index = 1;
        temp = new StringBuilder();
        while (next()) {
            temp.setLength(0);
            for (int count = 0; count < elements.length; count++) {
                temp.append(getColumnValue(count));
                if (count + 1 < elements.length) {
                    temp.append("\t");
                }
            }
            line[index++] = temp.toString();
        }
        return line;
    }

    @Override
    public String toString() {
        String[] contents = getDataAsText();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String content : contents) {
            if (!first) {
                sb.append("\n");
            } else {
                first = false;
            }
            sb.append(content);
        }
        return sb.toString();
    }
}
