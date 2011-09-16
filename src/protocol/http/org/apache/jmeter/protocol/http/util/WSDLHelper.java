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

package org.apache.jmeter.protocol.http.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.jmeter.protocol.http.control.AuthManager;

/**
 * For now I use DOM for WSDLHelper, but it would be more efficient to use JAXB
 * to generate an object model for WSDL and use it to perform serialization and
 * deserialization. It also makes it easier to traverse the WSDL to get
 * necessary information.
 * <p>
 * Created on: Jun 3, 2003<br>
 *
 */
public class WSDLHelper {
    
    private static int GET_WDSL_TIMEOUT = 5000; // timeout to retrieve wsdl when server not response
    
    /**
     * -------------------------------------------- The members used by the
     * class to do its work --------------------------------------------
     */

    private URL WSDLURL = null;

    private URLConnection CONN = null;

    private Document WSDLDOC = null;

    private String SOAPBINDING = null;

    private URL bindingURL = null;

    private Object[] SOAPOPS = null;

    private final Map<String, String> ACTIONS = new HashMap<String, String>();

    private final AuthManager AUTH;

    /**
     * Default constructor takes a string URL
     */
    public WSDLHelper(String url) throws MalformedURLException {
        this(url, null);
    }

    public WSDLHelper(String url, AuthManager auth) throws MalformedURLException {
        WSDLURL = new URL(url);
        this.AUTH = auth;
    }

    /**
     * Returns the URL
     *
     * @return the URL
     */
    public URL getURL() {
        return this.WSDLURL;
    }

    /**
     * Return the protocol from the URL. this is needed, so that HTTPS works
     * as expected.
     */
    public String getProtocol() {
        return this.bindingURL.getProtocol();
    }

    /**
     * Return the host in the WSDL binding address
     */
    public String getBindingHost() {
        return this.bindingURL.getHost();
    }

    /**
     * Return the path in the WSDL for the binding address
     */
    public String getBindingPath() {
        return this.bindingURL.getPath();
    }

    /**
     * Return the port for the binding address
     */
    public int getBindingPort() {
        return this.bindingURL.getPort();
    }

    /**
     * Returns the binding point for the webservice. Right now it naively
     * assumes there's only one binding point with numerous soap operations.
     *
     * @return String
     */
    public String getBinding() {
        try {
            NodeList services = this.WSDLDOC.getElementsByTagName("service");
            if (services.getLength() == 0) {
                services = this.WSDLDOC.getElementsByTagName("wsdl:service");
            }
            // the document should only have one service node
            // if it doesn't it may not work!
            Element node = (Element) services.item(0);
            NodeList ports = node.getElementsByTagName("port");
            if (ports.getLength() == 0) {
                ports = node.getElementsByTagName("wsdl:port");
            }
            for (int idx = 0; idx < ports.getLength(); idx++) {
                Element pnode = (Element) ports.item(idx);
                // NOTUSED String portname = pnode.getAttribute("name");
                // used to check binding, but now it doesn't. it was
                // failing when wsdl did not using binding as expected
                NodeList servlist = pnode.getElementsByTagName("soap:address");
                // check wsdlsoap
                if (servlist.getLength() == 0) {
                    servlist = pnode.getElementsByTagName("wsdlsoap:address");
                }
                if (servlist.getLength() == 0) {
                    servlist = pnode.getElementsByTagName("SOAP:address");
                }
                Element addr = (Element) servlist.item(0);
                this.SOAPBINDING = addr.getAttribute("location");
                this.bindingURL = new URL(this.SOAPBINDING);
                return this.SOAPBINDING;
            }
            return null;
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Method is used internally to connect to the URL. It's protected;
     * therefore external classes should use parse to get the resource at the
     * given location.
     *
     * @throws IOException
     */
    protected void connect() throws IOException {
        try {
            CONN = WSDLURL.openConnection();
            CONN.setConnectTimeout(GET_WDSL_TIMEOUT);
            CONN.setReadTimeout(GET_WDSL_TIMEOUT);
            // in the rare case the WSDL is protected and requires
            // authentication, use the AuthManager to set the
            // authorization. Basic and Digest authorization are
            // pretty weak and don't provide real security.
            if (CONN instanceof HttpURLConnection && this.AUTH != null && this.AUTH.getAuthHeaderForURL(this.WSDLURL) != null) {
                CONN.setRequestProperty("Authorization", this.AUTH.getAuthHeaderForURL(this.WSDLURL));
            }
        } catch (IOException exception) {
            throw exception;
        }
    }

    /**
     * We try to close the connection to make sure it doesn't hang around.
     */
    protected void close() {
        try {
            if (CONN != null) {
                CONN.getInputStream().close();
            }
        } catch (IOException ignored) {
            // do nothing
        }
    }

    /**
     * Method is used internally to parse the InputStream and build the document
     * using javax.xml.parser API.
     */
    protected void buildDocument() throws ParserConfigurationException, IOException, SAXException {
        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docbuild = dbfactory.newDocumentBuilder();
            WSDLDOC = docbuild.parse(CONN.getInputStream());
        } catch (ParserConfigurationException exception) {
            throw exception;
        } catch (IOException exception) {
            throw exception;
        } catch (SAXException exception) {
            throw exception;
        }
    }

    /**
     * Call this method to retrieve the WSDL. This method must be called,
     * otherwise a connection to the URL won't be made and the stream won't be
     * parsed.
     */
    public void parse() throws WSDLException {
        try {
            this.connect();
            this.buildDocument();
            SOAPOPS = this.getOperations();
        } catch (IOException exception) {
            throw (new WSDLException(exception));
        } catch (SAXException exception) {
            throw (new WSDLException(exception));
        } catch (ParserConfigurationException exception) {
            throw (new WSDLException(exception));
        } finally {
            this.close();
        }
    }

    /**
     * Get a list of the web methods as a string array.
     */
    public String[] getWebMethods() {
        for (int idx = 0; idx < SOAPOPS.length; idx++) {
            // get the node
            Node act = (Node) SOAPOPS[idx];
            // get the soap:operation
            NodeList opers = ((Element) act).getElementsByTagName("soap:operation");
            if (opers.getLength() == 0) {
                opers = ((Element) act).getElementsByTagName("wsdlsoap:operation");
            }
            if (opers.getLength() == 0) {
                opers = ((Element) act).getElementsByTagName("wsdl:operation");
            }
            if (opers.getLength() == 0) {
                opers = ((Element) act).getElementsByTagName("operation");
            }

            // there should only be one soap:operation node per operation
            Element op = (Element) opers.item(0);
            String value;
            if (op != null) {
                value = op.getAttribute("soapAction");
            } else {
                value = "";
            }
            String key = ((Element) act).getAttribute("name");
            this.ACTIONS.put(key, value);
        }
        Set<String> keys = this.ACTIONS.keySet();
        String[] stringmeth = new String[keys.size()];
        Object[] stringKeys = keys.toArray();
        System.arraycopy(stringKeys, 0, stringmeth, 0, keys.size());
        return stringmeth;
    }

    /**
     * Return the soap action matching the operation name.
     */
    public String getSoapAction(String key) {
        return this.ACTIONS.get(key);
    }

    /**
     * Get the wsdl document.
     */
    public Document getWSDLDocument() {
        return WSDLDOC;
    }

    /**
     * Method will look at the binding nodes and see if the first child is a
     * soap:binding. If it is, it adds it to an array.
     *
     * @return Node[]
     */
    public Object[] getSOAPBindings() {
        ArrayList<Element> list = new ArrayList<Element>();
        NodeList bindings = WSDLDOC.getElementsByTagName("binding");
        String soapBind = "soap:binding";
        if (bindings.getLength() == 0) {
            bindings = WSDLDOC.getElementsByTagName("wsdl:binding");
        }
        if (WSDLDOC.getElementsByTagName(soapBind).getLength() == 0) {
            soapBind = "wsdlsoap:binding";
        }
        if (WSDLDOC.getElementsByTagName(soapBind).getLength() == 0) {
            soapBind = "SOAP:binding";
        }

        for (int idx = 0; idx < bindings.getLength(); idx++) {
            Element nd = (Element) bindings.item(idx);
            NodeList slist = nd.getElementsByTagName(soapBind);
            if (slist.getLength() > 0) {
                nd.getAttribute("name");
                list.add(nd);
            }
        }
        if (list.size() > 0) {
            return list.toArray();
        }
        return new Object[0];
    }

    /**
     * Look at the bindings with soap operations and get the soap operations.
     * Since WSDL may describe multiple bindings and each binding may have
     * multiple soap operations, we iterate through the binding nodes with a
     * first child that is a soap binding. If a WSDL doesn't use the same
     * formatting convention, it is possible we may not get a list of all the
     * soap operations. If that is the case, getSOAPBindings() will need to be
     * changed. I should double check the WSDL spec to see what the official
     * requirement is. Another option is to get all operation nodes and check to
     * see if the first child is a soap:operation. The benefit of not getting
     * all operation nodes is WSDL could contain duplicate operations that are
     * not SOAP methods. If there are a large number of methods and half of them
     * are HTTP operations, getting all operations could slow things down.
     *
     * @return Node[]
     */
    public Object[] getOperations() {
        Object[] res = this.getSOAPBindings();
        ArrayList<Element> ops = new ArrayList<Element>();
        // first we iterate through the bindings
        for (int idx = 0; idx < res.length; idx++) {
            Element one = (Element) res[idx];
            NodeList opnodes = one.getElementsByTagName("operation");
            String soapOp = "soap:operation";
            if (opnodes.getLength() == 0) {
                opnodes = one.getElementsByTagName("wsdl:operation");
            }
            if (one.getElementsByTagName(soapOp).getLength() == 0) {
                soapOp = "wsdlsoap:operation";
            }
            // now we iterate through the operations
            for (int idz = 0; idz < opnodes.getLength(); idz++) {
                // if the first child is soap:operation
                // we add it to the array
                Element child = (Element) opnodes.item(idz);

                // TODO - the following code looks wrong - it does the same in both cases
                NodeList soapnode = child.getElementsByTagName(soapOp);
                if (soapnode.getLength() > 0) {
                    ops.add(child);
                } else {
                    ops.add(child);
                }
            }
        }
        return ops.toArray();
    }

    /**
     * Simple test for the class uses bidbuy.wsdl from Apache's soap driver
     * examples.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            WSDLHelper help =
            // new WSDLHelper("http://localhost/WSTest/WSTest.asmx?WSDL");
            // new WSDLHelper("http://localhost/AxisWSDL.xml");
            new WSDLHelper("http://localhost:8080/ServiceGateway.wsdl");
            // new WSDLHelper("http://services.bio.ifi.lmu.de:1046/prothesaurus/services/BiologicalNameService?wsdl");
            long start = System.currentTimeMillis();
            help.parse();
            String[] methods = help.getWebMethods();
            System.out.println("el: " + (System.currentTimeMillis() - start));
            for (int idx = 0; idx < methods.length; idx++) {
                System.out.println("method name: " + methods[idx]);
            }
            System.out.println("service url: " + help.getBinding());
            System.out.println("protocol: " + help.getProtocol());
            System.out.println("port=" + help.getURL().getPort());
        } catch (Exception exception) {
            System.out.println("main method catch:");
            exception.printStackTrace();
        }
    }
}
