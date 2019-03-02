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

package org.apache.jmeter.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Class used to process freemarkers templates
 * @since 5.1
 */
public final class TemplateUtil {

    private static Configuration templateConfiguration = init();

    private TemplateUtil() {
        super();
    }

    private static Configuration init() {
        Configuration templateConfiguration = new Configuration(Configuration.getVersion());
        templateConfiguration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        templateConfiguration.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        templateConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return templateConfiguration;
    }

    /**
     * Give a basic templateConfiguration
     * @return a Configuration
     */
    public static Configuration getTemplateConfig() {
        return templateConfiguration;
    }

    /**
     * Process a given freemarker template and put its result in a new folder.
     *
     * @param template file that contains the freemarker template to process
     * @param outputFile {@link File} created from template
     * @param templateConfig Configuration of the template
     * @param data to inject in the template
     * @throws IOException if an I/O exception occurs during writing to the writer
     * @throws TemplateException if an exception occurs during template processing
     */
    public static void processTemplate(File template,
            File outputFile,
            Configuration templateConfig, Map<String, String> data)
                    throws IOException, TemplateException {

        templateConfig.setDirectoryForTemplateLoading(template.getParentFile());
        freemarker.template.Template temp = templateConfig.getTemplate(template.getName());
        try (FileOutputStream stream = new FileOutputStream(outputFile);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)){
            temp.process(data, bufferedWriter);
        }
    }
}
