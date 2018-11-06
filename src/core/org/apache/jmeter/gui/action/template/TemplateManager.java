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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
     * @return the templates names
     */
    public String[] getTemplateNames() {
        return allTemplates.keySet().toArray(new String[allTemplates.size()]);
    }

    private Map<String, Template> readTemplates() {
        final Map<String, Template> temps = new LinkedHashMap<>();
       
        final String[] templateFiles = TEMPLATE_FILES.split(",");
        for (String templateFile : templateFiles) {
            if(!StringUtils.isEmpty(templateFile)) {
                final File f = new File(JMeterUtils.getJMeterHome(), templateFile); 
                try {
                    if(f.exists() && f.canRead()) {
                        if (log.isInfoEnabled()) {
                            log.info("Reading templates from: {}", f.getAbsolutePath());
                        }
                        
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        SAXParser parser = factory.newSAXParser();
                        SaxHandler saxHandler = new SaxHandler();
                        parser.parse(f, saxHandler);
                        Map<String, Template> templates = saxHandler.getTemplatesMap();
                        
                        final File parent = f.getParentFile();
                        for(Template t : templates.values()) {
                            if (!t.getFileName().startsWith("/")) {
                                t.setParent(parent);
                            }
                        }
                        temps.putAll(templates);
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Ignoring template file:'{}' as it does not exist or is not readable",
                                    f.getAbsolutePath());
                        }
                    }
                } catch(Exception ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Ignoring template file:'{}', an error occurred parsing the file", f.getAbsolutePath(),
                                ex);
                    }
                } 
            }
        }
        return temps;
    }
    
    // used to parse the templates.xml document
    private class SaxHandler extends DefaultHandler {
        LinkedHashMap<String, Template> templatesMap = new LinkedHashMap<>();
        
        private Template template;
        private String node;
        private StringBuilder nodeBuffer = new StringBuilder();
        private Map<String, String> parameters = new LinkedHashMap<>();
        
        @Override
        public void characters(char[] data, int start, int end){
            if(!node.equals("description")) {
                nodeBuffer = new StringBuilder();
            }
            nodeBuffer.append(data, start, end);
            
            //need this line to avoid getting wrong nodeBuffer when the node is closed 
            if(!nodeBuffer.toString().equals("\n        ")) {
                if(node.equals("name")) {
                    template.setName(nodeBuffer.toString());
                }else if(node.equals("fileName")) {
                    template.setFileName(nodeBuffer.toString());
                }else if(node.equals("description")) {
                    template.setDescription(nodeBuffer.toString());
                }
            }
         }
        
        @Override
        public void startElement(String namespaceURI, String lname,
            String qname, Attributes attrs) throws SAXException {

            node = qname;
            if(qname.equals("template")) {
                if(template != null) {
                    template.setParameters(parameters);
                    templatesMap.put(template.getName(), template);
                    parameters = new LinkedHashMap<>();
                }
                template = new Template();
                template.setTestPlan(Boolean.valueOf(attrs.getValue("isTestPlan")));
            }else if(qname.equals("parameter")) {
                String keyParam = attrs.getValue("key");
                String valueParam = attrs.getValue("defaultValue");
                parameters.put(keyParam, valueParam);
            }
          }

        // need this method to put the last template in the map
        @Override
        public void endDocument() throws SAXException {
            if(template != null) {
                templatesMap.put(template.getName(), template);
            }
        }

        public Map<String, Template> getTemplatesMap(){
            return templatesMap;
        }
    }

    /**
     * @param selectedTemplate Template name
     * @return {@link Template}
     */
    public Template getTemplateByName(String selectedTemplate) {
        return allTemplates.get(selectedTemplate);
    }
}
