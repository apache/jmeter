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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Manages Test Plan templates
 * @since 2.10
 */
public class TemplateManager {
    // Created by XStream reading templates.xml
    private static class Templates {
        /*
         * N.B. Must use LinkedHashMap for field type
         * XStream creates a plain HashMap if one uses Map as the field type.
         */
        private final LinkedHashMap<String, Template> templates = new LinkedHashMap<>();
    }
    private static final String TEMPLATE_FILES = JMeterUtils.getPropDefault("template.files", // $NON-NLS-1$
            "/bin/templates/templates.xml");

    private static final Logger log = LoggerFactory.getLogger(TemplateManager.class);
    
    private static final TemplateManager SINGLETON = new TemplateManager();
    
    private final Map<String, Template> allTemplates;

    private final XStream xstream = initXStream();

    public static TemplateManager getInstance() {
        return SINGLETON;
    }
    
    private TemplateManager()  {
        allTemplates = readTemplates();            
    }
    
    private XStream initXStream() {
        XStream xstream = new XStream(new DomDriver(){
            /**
             * Create the DocumentBuilderFactory instance.
             * See https://blog.compass-security.com/2012/08/secure-xml-parser-configuration/
             * See https://github.com/x-stream/xstream/issues/25
             * @return the new instance
             */
            @Override
            protected DocumentBuilderFactory createDocumentBuilderFactory() {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                } catch (ParserConfigurationException e) {
                    throw new StreamException(e);
                }
                factory.setExpandEntityReferences(false);
                return factory;
            }
        });
        JMeterUtils.setupXStreamSecurityPolicy(xstream);
        xstream.alias("template", Template.class);
        xstream.alias("templates", Templates.class);
        xstream.useAttributeFor(Template.class, "isTestPlan");
        
        // templates i
        xstream.addImplicitMap(Templates.class, 
                // field TemplateManager#templates 
                "templates", // $NON-NLS-1$
                Template.class,     
                // field Template#name 
                "name" // $NON-NLS-1$
                );
                
        return xstream;
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
                        final File parent = f.getParentFile();
                        final LinkedHashMap<String, Template> templates = ((Templates) xstream.fromXML(f)).templates;
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

    /**
     * @param selectedTemplate Template name
     * @return {@link Template}
     */
    public Template getTemplateByName(String selectedTemplate) {
        return allTemplates.get(selectedTemplate);
    }
}
