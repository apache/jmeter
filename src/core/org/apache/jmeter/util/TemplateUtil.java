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
    
    /**
     * Give a basic templateConfiguration
     * @return a Configuration
     */
    public static Configuration getTemplateConfig() {
        if(templateConfiguration == null) {
            templateConfiguration = new Configuration(Configuration.getVersion());
            templateConfiguration.setDefaultEncoding("UTF-8");
            templateConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        }
        return templateConfiguration;
    }
    
    /**
     * Process a given freemarker template and put its result in a new folder.
     * 
     * @param template : file that contains the freemarker template to process
     * @param createdFileName name of the result file
     * @param createdFileDirectory path of the directory where to put the result. If it does not exist, the directory is created.
     * @param templateConfig Configuration of the template
     * @param data to inject in the template
     * @throws IOException if an I/O exception occurs during writing to the writer
     * @throws TemplateException if an exception occurs during template processing
     */
    public static void processTemplate(File template, String createdFileName, String createdFileDirectory,
            Configuration templateConfig, Map<String, String> data) throws IOException, TemplateException {
        templateConfig.setDirectoryForTemplateLoading(template.getParentFile());
        freemarker.template.Template temp = templateConfig.getTemplate(template.getName());
        
        File dir = new File (createdFileDirectory);
        dir.mkdir();
        
        try (FileOutputStream stream = new FileOutputStream(createdFileDirectory+File.separator+createdFileName);
                Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)){
            temp.process(data, bufferedWriter);
        }
    }
    
}
