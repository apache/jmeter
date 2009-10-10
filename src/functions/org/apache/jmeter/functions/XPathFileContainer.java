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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//@see org.apache.jmeter.functions.PackageTest for unit tests

/**
 * File data container for XML files Data is accessible via XPath
 *
 */
public class XPathFileContainer {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final NodeList nodeList;

    private final String fileName; // name of the file

    private final String xpath;

    /** Keeping track of which row is next to be read. */
    private int nextRow;// probably does not need to be synch (always accessed through ThreadLocal?)
    int getNextRow(){// give access to Test code
        return nextRow;
    }

    public XPathFileContainer(String file, String xpath) throws FileNotFoundException, IOException,
            ParserConfigurationException, SAXException, TransformerException {
        log.debug("XPath(" + file + ") xpath " + xpath + "");
        fileName = file;
        this.xpath = xpath;
        nextRow = 0;
        nodeList=load();
    }

    private NodeList load() throws IOException, FileNotFoundException, ParserConfigurationException, SAXException,
            TransformerException {
        InputStream fis = null;
        NodeList nl = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            fis = new FileInputStream(fileName);
            nl = XPathAPI.selectNodeList(builder.parse(fis), xpath);
            log.debug("found " + nl.getLength());

        } catch (FileNotFoundException e) {
            log.warn(e.toString());
            throw e;
        } catch (IOException e) {
            log.warn(e.toString());
            throw e;
        } catch (ParserConfigurationException e) {
            log.warn(e.toString());
            throw e;
        } catch (SAXException e) {
            log.warn(e.toString());
            throw e;
        } catch (TransformerException e) {
            log.warn(e.toString());
            throw e;
        } finally {
            JOrphanUtils.closeQuietly(fis);
        }
        return nl;
    }

    public String getXPathString(int num) {
        return nodeList.item(num).getNodeValue();
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
        if (nextRow >= size())// 0-based
        {
            nextRow = 0;
        }
        log.debug(new StringBuilder("Row: ").append(row).toString());
        return row;
    }

    public int size() {
        return (nodeList == null) ? -1 : nodeList.getLength();
    }

    /**
     * @return the file name for this class
     */
    public String getFileName() {
        return fileName;
    }

}