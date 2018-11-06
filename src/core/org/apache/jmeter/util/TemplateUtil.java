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
 */
public class TemplateUtil {
    
    private static Configuration templateConfiguration;
    
    public static Configuration getTemplateConfig() {
        if(templateConfiguration == null) {
            templateConfiguration = new Configuration(Configuration.getVersion());
            templateConfiguration.setDefaultEncoding("UTF-8");
            templateConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }
        return templateConfiguration;
    }
    
    public static void processTemplate(String templateDirectory, String templateName, String createdFileName, String createdFileDirectory,
            Configuration templateConfig, Map<String, String> data) throws IOException, TemplateException {
        File templateDirectoryFile = new File(JMeterUtils.getJMeterBinDir());
        templateConfig.setDirectoryForTemplateLoading(templateDirectoryFile);
        freemarker.template.Template temp = templateConfig.getTemplate(templateName);
        
        try (FileOutputStream stream = new FileOutputStream(templateDirectory+File.separator+createdFileName);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)){
            temp.process(data, bufferedWriter);
        }
    }
    
}
