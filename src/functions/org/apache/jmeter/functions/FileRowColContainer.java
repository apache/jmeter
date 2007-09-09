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

package org.apache.jmeter.functions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * File data container for CSV (and similar delimited) files Data is accessible
 * via row and column number
 * 
 * @version $Revision$
 */
public class FileRowColContainer {

	private static final Logger log = LoggingManager.getLoggerForClass();

	private ArrayList fileData; // Lines in the file, split into columns

	private String fileName; // name of the file

	public static final String DELIMITER 
        = JMeterUtils.getPropDefault("csvread.delimiter",  // $NON-NLS-1$
                ","); // $NON-NLS-1$

	/** Keeping track of which row is next to be read. */
	private int nextRow;

	/** Delimiter for this file */
	private String delimiter;

	private FileRowColContainer()// Not intended to be called directly
	{
	}

	public FileRowColContainer(String file, String delim) throws IOException, FileNotFoundException {
		log.debug("FRCC(" + file + "," + delim + ")");
		fileName = file;
		delimiter = delim;
		nextRow = 0;
		load();
	}

	public FileRowColContainer(String file) throws IOException, FileNotFoundException {
		log.debug("FRCC(" + file + ")[" + DELIMITER + "]");
		fileName = file;
		delimiter = DELIMITER;
		nextRow = 0;
		load();
	}

	private void load() throws IOException, FileNotFoundException {
		fileData = new ArrayList();

		BufferedReader myBread = null;
		try {
			FileReader fis = new FileReader(fileName);
			myBread = new BufferedReader(fis);
			String line = myBread.readLine();
			/*
			 * N.B. Stop reading the file if we get a blank line: This allows
			 * for trailing comments in the file
			 */
			while (line != null && line.length() > 0) {
				fileData.add(splitLine(line, delimiter));
				line = myBread.readLine();
			}
		} catch (FileNotFoundException e) {
			fileData = null;
			log.warn(e.toString());
			throw e;
		} catch (IOException e) {
			fileData = null;
			log.warn(e.toString());
			throw e;
		} finally {
			if (myBread != null)
				myBread.close();
		}
	}

	/**
	 * Get the string for the column from the current row
	 * 
	 * @param row
	 *            row number (from 0)
	 * @param col
	 *            column number (from 0)
	 * @return the string (empty if out of bounds)
	 * @throws IndexOutOfBoundsException
	 *             if the column number is out of bounds
	 */
	public String getColumn(int row, int col) throws IndexOutOfBoundsException {
		String colData;
		colData = (String) ((ArrayList) fileData.get(row)).get(col);
		log.debug(fileName + "(" + row + "," + col + "): " + colData);
		return colData;
	}

	/**
	 * Returns the next row to the caller, and updates it, allowing for wrap
	 * round
	 * 
	 * @return the first free (unread) row
	 * 
	 */
	public int nextRow() {
		int row = nextRow;
		nextRow++;
		if (nextRow >= fileData.size())// 0-based
		{
			nextRow = 0;
		}
		log.debug("Row: " + row);
		return row;
	}

	/**
	 * Splits the line according to the specified delimiter
	 * 
	 * @return an ArrayList of Strings containing one element for each value in
	 *         the line
	 */
	private static ArrayList splitLine(String theLine, String delim) {
		ArrayList result = new ArrayList();
		StringTokenizer tokener = new StringTokenizer(theLine, delim, true);
		/*
		 * the beginning of the line is a "delimiter" so that ,a,b,c returns ""
		 * "a" "b" "c"
		 */
		boolean lastWasDelim = true;
		while (tokener.hasMoreTokens()) {
			String token = tokener.nextToken();
			if (token.equals(delim)) {
				if (lastWasDelim) {
					// two delimiters in a row; add an empty String
					result.add("");
				}
				lastWasDelim = true;
			} else {
				lastWasDelim = false;
				result.add(token);
			}
		}
		if (lastWasDelim) // Catch the trailing delimiter
		{
			result.add(""); // $NON-NLS-1$
		}
		return result;
	}

	/**
	 * @return the file name for this class
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return Returns the delimiter.
	 */
	final String getDelimiter() {
		return delimiter;
	}
    
    // Added to support external testing
    public int getSize(){
        return fileData.size();
    }
}