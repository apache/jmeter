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
package org.apache.jmeter.report.dashboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.TimeZone;

import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.config.ExporterConfiguration;
import org.apache.jmeter.report.config.SubConfiguration;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.core.ArgumentNullException;
import org.apache.jmeter.report.core.DataContext;
import org.apache.jmeter.report.core.TimeHelper;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ResultData;
import org.apache.jmeter.report.processor.ResultDataVisitor;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.ValueResultData;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * The class HtmlTemplateExporter provides a data exporter that generates and
 * processes template files using freemarker.
 * 
 * @since 2.14
 */
public class HtmlTemplateExporter extends AbstractDataExporter {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String DATA_CTX_TESTFILE = "testFile";
    public static final String DATA_CTX_BEGINDATE = "beginDate";
    public static final String DATA_CTX_ENDDATE = "endDate";
    public static final String DATA_CTX_TIMEZONE = "timeZone";
    public static final String DATA_CTX_TIMEZONE_OFFSET = "timeZoneOffset";
    public static final String DATA_CTX_OVERALL_FILTER = "overallFilter";
    public static final String DATA_CTX_SHOW_CONTROLLERS_ONLY = "showControllersOnly";
    public static final String DATA_CTX_RESULT = "result";
    public static final String DATA_CTX_EXTRA_OPTIONS = "extraOptions";
    public static final String DATA_CTX_SERIES_FILTER = "seriesFilter";
    public static final String DATA_CTX_FILTERS_ONLY_SAMPLE_SERIES = "filtersOnlySampleSeries";

    public static final String TIMESTAMP_FORMAT_MS = "ms";
    private static final String INVALID_TEMPLATE_DIRECTORY_FMT = "\"%s\" is not a valid template directory";
    private static final String INVALID_PROPERTY_CONFIG_FMT = "Wrong property \"%s\" in \"%s\" export configuration";

    // Template directory
    private static final String TEMPLATE_DIR = "template_dir";
    private static final File TEMPLATE_DIR_DEFAULT = new File("report-template");

    // Output directory
    private static final String OUTPUT_DIR = "output_dir";
    private static final File OUTPUT_DIR_DEFAULT = new File("report-output");

    private void addToContext(String key, Object value, DataContext context) {
        if (value instanceof String) {
            value = '"' + (String) value + '"';
        }
        context.put(key, value);
    }

    private interface ResultCustomizer {
        ResultData customizeResult(ResultData result);
    }

    private <TVisit> void addResultToContext(String resultKey,
            Map<String, Object> storage, DataContext dataContext,
            ResultDataVisitor<TVisit> visitor) {
        addResultToContext(resultKey, storage, dataContext, visitor, null);
    }

    private <TVisit> void addResultToContext(String resultKey,
            Map<String, Object> storage, DataContext dataContext,
            ResultDataVisitor<TVisit> visitor, ResultCustomizer customizer) {
        Object data = storage.get(resultKey);
        if (data instanceof ResultData) {
            ResultData result = (ResultData) data;
            if (customizer != null) {
                result = customizer.customizeResult(result);
            }
            dataContext.put(resultKey, result.accept(visitor));
        }
    }

    private long formatTimestamp(String key, DataContext context) {
        // FIXME Why convert to double then long (rounding ?)
        double result = Double.parseDouble((String) context.get(key));
        long timestamp = (long) result;
        // Quote the string to respect Json spec.
        context.put(key, '"' + TimeHelper.formatTimeStamp(timestamp) + '"');
        return timestamp;
    }

    private <TProperty> TProperty getPropertyFromConfig(SubConfiguration cfg,
            String property, TProperty defaultValue, Class<TProperty> clazz)
            throws ExportException {
        try {
            return cfg.getProperty(property, defaultValue, clazz);
        } catch (ConfigurationException ex) {
            throw new ExportException(String.format(
                    INVALID_PROPERTY_CONFIG_FMT, property, getName()), ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.dashboard.DataExporter#Export(org.apache.jmeter
     * .report.processor.SampleContext,
     * org.apache.jmeter.report.config.ReportGeneratorConfiguration)
     */
    @Override
    public void export(SampleContext context, File file,
            ReportGeneratorConfiguration configuration) throws ExportException {
        if (context == null) {
            throw new ArgumentNullException("context");
        }
        if (file == null) {
            throw new ArgumentNullException("file");
        }
        if (configuration == null) {
            throw new ArgumentNullException("configuration");
        }

        log.debug("Start template processing");

        // Create data context and populate it
        DataContext dataContext = new DataContext();

        // Get the configuration of the current exporter
        final ExporterConfiguration exportCfg = configuration
                .getExportConfigurations().get(getName());

        // Get template directory property value
        File templateDirectory = getPropertyFromConfig(exportCfg, TEMPLATE_DIR,
                TEMPLATE_DIR_DEFAULT, File.class);
        if (!templateDirectory.isDirectory()) {
            String message = String.format(INVALID_TEMPLATE_DIRECTORY_FMT,
                    templateDirectory);
            log.error(message);
            throw new ExportException(message);
        }

        // Get output directory property value
        File outputDir = getPropertyFromConfig(exportCfg, OUTPUT_DIR,
                OUTPUT_DIR_DEFAULT, File.class);
        log.info("Will generate dashboard in folder:" + outputDir);

        // Add the flag defining whether only sample series are filtered to the
        // context
        addToContext(DATA_CTX_FILTERS_ONLY_SAMPLE_SERIES,
                Boolean.valueOf(exportCfg.filtersOnlySampleSeries()), dataContext);

        // Add the series filter to the context
        addToContext(DATA_CTX_SERIES_FILTER, exportCfg.getSeriesFilter(),
                dataContext);

        // Add the flag defining whether only controller series are displayed
        addToContext(DATA_CTX_SHOW_CONTROLLERS_ONLY,
                Boolean.valueOf(exportCfg.showControllerSeriesOnly()), dataContext);

        JsonizerVisitor jsonizer = new JsonizerVisitor();
        Map<String, Object> storedData = context.getData();

        // Add begin date consumer result to the data context
        addResultToContext(ReportGenerator.BEGIN_DATE_CONSUMER_NAME,
                storedData, dataContext, jsonizer);

        // Add end date summary consumer result to the data context
        addResultToContext(ReportGenerator.END_DATE_CONSUMER_NAME, storedData,
                dataContext, jsonizer);

        // Add Apdex summary consumer result to the data context
        addResultToContext(ReportGenerator.APDEX_SUMMARY_CONSUMER_NAME,
                storedData, dataContext, jsonizer);

        // Add errors summary consumer result to the data context
        addResultToContext(ReportGenerator.ERRORS_SUMMARY_CONSUMER_NAME,
                storedData, dataContext, jsonizer);

        // Add requests summary consumer result to the data context
        addResultToContext(ReportGenerator.REQUESTS_SUMMARY_CONSUMER_NAME,
                storedData, dataContext, jsonizer);

        // Add statistics summary consumer result to the data context
        addResultToContext(ReportGenerator.STATISTICS_SUMMARY_CONSUMER_NAME,
                storedData, dataContext, jsonizer);

        // Collect graph results from sample context and transform them into
        // Json strings to inject in the data context
        for (String graphId : configuration.getGraphConfigurations().keySet()) {
            final SubConfiguration extraOptions = exportCfg
                    .getGraphExtraConfigurations().get(graphId);
            addResultToContext(graphId, storedData, dataContext, jsonizer,
                    new ResultCustomizer() {

                        @Override
                        public ResultData customizeResult(ResultData result) {
                            MapResultData customizedResult = new MapResultData();
                            customizedResult.setResult(DATA_CTX_RESULT,result);
                            if (extraOptions != null) {
                                MapResultData extraResult = new MapResultData();
                                for (Map.Entry<String, String> extraEntry : extraOptions
                                        .getProperties().entrySet()) {
                                    extraResult.setResult(
                                            extraEntry.getKey(),
                                            new ValueResultData(extraEntry
                                                    .getValue()));
                                }
                                customizedResult.setResult(
                                        DATA_CTX_EXTRA_OPTIONS, extraResult);
                            }
                            return customizedResult;
                        }
                    });
        }

        // Replace the begin date with its formatted string and store the old
        // timestamp
        long oldTimestamp = formatTimestamp(
                ReportGenerator.BEGIN_DATE_CONSUMER_NAME, dataContext);

        // Replace the end date with its formatted string
        formatTimestamp(ReportGenerator.END_DATE_CONSUMER_NAME, dataContext);

        // Add time zone offset (that matches the begin date) to the context
        TimeZone timezone = TimeZone.getDefault();
        addToContext(DATA_CTX_TIMEZONE_OFFSET,
                Integer.valueOf(timezone.getOffset(oldTimestamp)), dataContext);

        // Add the test file name to the context
        addToContext(DATA_CTX_TESTFILE, file.getName(), dataContext);

        // Add the overall filter property to the context
        addToContext(DATA_CTX_OVERALL_FILTER, configuration.getSampleFilter(),
                dataContext);

        // Walk template directory to copy files and process templated ones
        Configuration templateCfg = new Configuration(
                Configuration.getVersion());
        try {
            templateCfg.setDirectoryForTemplateLoading(templateDirectory);
            templateCfg
                    .setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            TemplateVisitor visitor = new TemplateVisitor(
                    templateDirectory.toPath(), outputDir.toPath(),
                    templateCfg, dataContext);
            Files.walkFileTree(templateDirectory.toPath(), visitor);
        } catch (IOException ex) {
            throw new ExportException("Unable to process template files.", ex);
        }

        log.debug("End of template processing");

    }
}
