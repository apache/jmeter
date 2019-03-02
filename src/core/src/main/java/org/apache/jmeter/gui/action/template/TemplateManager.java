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

package org.apache.jmeter.gui.action.template;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Manages Test Plan templates
 * @since 2.10
 */
public class TemplateManager {
    private static final String TEMPLATE_FILES = JMeterUtils.getPropDefault("template.files", // $NON-NLS-1$
            "/bin/templates/templates.xml");

    private static final Logger log = LoggerFactory.getLogger(TemplateManager.class);

    private static final TemplateManager SINGLETON = new TemplateManager();

    private final Map<String, Template> allTemplates;

    public static TemplateManager getInstance() {
        return SINGLETON;
    }

    private TemplateManager()  {
        allTemplates = readTemplates();
    }

    public void addTemplate(Template template) {
        allTemplates.put(template.getName(), template);
    }

    /**
     * Resets the template Map by re-reading the template files.
     *
     * @return this
     */
    public TemplateManager reset() {
        allTemplates.clear();
        allTemplates.putAll(readTemplates());
        return this;
    }

    /**
     * @return the templates names sorted in alphabetical order
     */
    public String[] getTemplateNames() {
        return allTemplates.keySet().toArray(new String[allTemplates.size()]);
    }

    private Map<String, Template> readTemplates() {
        final Map<String, Template> temps = new TreeMap<>();

        final String[] templateFiles = TEMPLATE_FILES.split(",");
        for (String templateFile : templateFiles) {
            if(!StringUtils.isEmpty(templateFile)) {
                final File file = new File(JMeterUtils.getJMeterHome(), templateFile);
                try {
                    if(file.exists() && file.canRead()) {
                        if (log.isInfoEnabled()) {
                            log.info("Reading templates from: {}", file.getAbsolutePath());
                        }
                        Map<String, Template> templates = parseTemplateFile(file);
                        final File parent = file.getParentFile();
                        for(Template t : templates.values()) {
                            if (!t.getFileName().startsWith("/")) {
                                t.setParent(parent);
                            }
                        }
                        temps.putAll(templates);
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Ignoring template file:'{}' as it does not exist or is not readable",
                                    file.getAbsolutePath());
                        }
                    }
                } catch(Exception ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Ignoring template file:'{}', an error occurred parsing the file", file.getAbsolutePath(),
                                ex);
                    }
                }
            }
        }
        return temps;
    }

    public final class LoggingErrorHandler implements ErrorHandler {
        private Logger logger;
        private File file;

        public LoggingErrorHandler(Logger logger, File file) {
            this.logger = logger;
            this.file = file;
        }
        @Override
        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        @Override
        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

        @Override
        public void warning(SAXParseException ex) throws SAXException {
            logger.warn("Warning parsing file {}", file, ex);
        }
    }

    public static class DefaultEntityResolver implements EntityResolver {
        public DefaultEntityResolver() {
            super();
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if(systemId.endsWith("templates.dtd")) {
                return new InputSource(TemplateManager.class.getResourceAsStream("/org/apache/jmeter/gui/action/template/templates.dtd"));
            } else {
                return null;
            }
        }
    }

    public Map<String, Template> parseTemplateFile(File file) throws IOException, SAXException, ParserConfigurationException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder bd = dbf.newDocumentBuilder();
        bd.setEntityResolver(new DefaultEntityResolver());
        LoggingErrorHandler errorHandler = new LoggingErrorHandler(log, file);
        bd.setErrorHandler(errorHandler);
        Document document = bd.parse(file.getAbsolutePath());
        document.getDocumentElement().normalize();
        Map<String, Template> templates = new TreeMap<>();
        NodeList templateNodes = document.getElementsByTagName("template");
        for (int i = 0; i < templateNodes.getLength(); i++) {
            Node node = templateNodes.item(i);
            parseTemplateNode(templates, node);
        }
        return templates;
    }

    /**
     * @param templates Map of {@link Template} referenced by name
     * @param templateNode {@link Node} the xml template node
     */
    void parseTemplateNode(Map<String, Template> templates, Node templateNode) {
        if (templateNode.getNodeType() == Node.ELEMENT_NODE) {
            Template template = new Template();
            Element element =  (Element) templateNode;
            template.setTestPlan("true".equals(element.getAttribute("isTestPlan")));
            template.setName(textOfFirstTag(element, "name"));
            template.setDescription(textOfFirstTag(element, "description"));
            template.setFileName(textOfFirstTag(element, "fileName"));
            NodeList nl = element.getElementsByTagName("parameters");
            if(nl.getLength()>0) {
                NodeList parameterNodes = ((Element) nl.item(0)).getElementsByTagName("parameter");
                Map<String, String> parameters = parseParameterNodes(parameterNodes);
                template.setParameters(parameters);
            }
            templates.put(template.getName(), template);
        }
    }

    private String textOfFirstTag(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getTextContent();
    }

    private Map<String, String> parseParameterNodes(NodeList parameterNodes) {
        Map<String, String> parametersMap = new HashMap<>();
        for (int i = 0; i < parameterNodes.getLength(); i++) {
            Element element =  (Element) parameterNodes.item(i);
            parametersMap.put(element.getAttribute("key"), element.getAttribute("defaultValue"));
        }
        return parametersMap;
    }

    /**
     * @param selectedTemplate Template name
     * @return {@link Template}
     */
    public Template getTemplateByName(String selectedTemplate) {
        return allTemplates.get(selectedTemplate);
    }
}
