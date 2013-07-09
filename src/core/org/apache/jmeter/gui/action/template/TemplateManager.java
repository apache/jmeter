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

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.XStream;
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
        private final LinkedHashMap<String, Template> templates = new LinkedHashMap<String, Template>();
    }
    private static final String TEMPLATE_FILES = JMeterUtils.getPropDefault("template.files", // $NON-NLS-1$
            "/bin/templates/templates.xml");

    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final TemplateManager SINGLETON = new TemplateManager();
    
    private final Map<String, Template> templates;

    private final XStream xstream = initXStream();

    public static final TemplateManager getInstance() {
        return SINGLETON;
    }
    
    private TemplateManager()  {
        templates = readTemplates();            
    }
    
    private XStream initXStream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("template", Template.class);
        xstream.alias("templates", Templates.class);
        
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
        templates.put(template.getName(), template);
    }

    /**
     * @return the templates names
     */
    public String[] getTemplateNames() {
        return templates.keySet().toArray(new String[templates.size()]);
    }

    private Map<String, Template> readTemplates() {
        Map<String, Template> templates = new LinkedHashMap<String, Template>();
       
        String[] templateFiles = TEMPLATE_FILES.split(",");
        for (String templateFile : templateFiles) {
            if(!StringUtils.isEmpty(templateFile)) {
                File f = new File(JMeterUtils.getJMeterHome(), templateFile); 
                try {
                    if(f.exists() && f.canRead()) {
                        log.info("Reading templates from:"+f.getAbsolutePath());
                        templates.putAll(((Templates) xstream.fromXML(f)).templates);
                    } else {
                        log.warn("Ignoring template file:'"+f.getAbsolutePath()+"' as it does not exist or is not readable");
                    }
                } catch(Exception ex) {                    
                    log.warn("Ignoring template file:'"+f.getAbsolutePath()+"', an error occured parsing the file", ex);
                } 
            }
        }
        return templates;
    }

    /**
     * @param selectedTemplate Template name
     * @return {@link Template}
     */
    public Template getTemplateByName(String selectedTemplate) {
        return templates.get(selectedTemplate);
    }
}
