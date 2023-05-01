/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.report.dashboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.report.config.ExporterConfiguration;
import org.apache.jmeter.report.config.GraphConfiguration;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.config.SubConfiguration;
import org.apache.jmeter.report.core.DataContext;
import org.apache.jmeter.report.core.TimeHelper;
import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ResultData;
import org.apache.jmeter.report.processor.ResultDataVisitor;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.ValueResultData;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

/**
 * The class HtmlTemplateExporter provides a data exporter that generates and
 * processes template files using freemarker.
 *
 * @since 3.0
 */
public class HtmlTemplateExporter extends AbstractDataExporter {
    private static final String CUSTOM_GRAPH_PREFIX = "custom_";

    /** Format used for non null check of parameters. */
    private static final String MUST_NOT_BE_NULL = "%s must not be null";

    private static final Logger log = LoggerFactory.getLogger(HtmlTemplateExporter.class);

    public static final String DATA_CTX_REPORT_TITLE = "reportTitle";
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

    // Template directory
    private static final String TEMPLATE_DIR = "template_dir";
    private static final String TEMPLATE_DIR_NAME_DEFAULT = "report-template";

    // Output directory
    static final String OUTPUT_DIR = "output_dir";
    // Default output folder name
    static final String OUTPUT_DIR_NAME_DEFAULT = "report-output";

    /**
     * Adds to context the value surrounding it with quotes
     * @param key Key
     * @param value Value
     * @param context {@link DataContext}
     */
    private static void addToContext(String key, Object value, DataContext context) {
        if (value instanceof String) {
            value = '"' + (String) value + '"';
        }
        context.put(key, value);
    }

    /**
     * This class allows to customize data before exporting them
     */
    private interface ResultCustomizer {
        ResultData customizeResult(ResultData result);
    }

    /**
     * This class allows to inject graph_options properties to the exported data
     */
    private static class ExtraOptionsResultCustomizer implements ResultCustomizer {
        private SubConfiguration extraOptions;

        /**
         * Sets the extra options to inject in the result data
         *
         * @param extraOptions to inject
         */
        public final void setExtraOptions(SubConfiguration extraOptions) {
            this.extraOptions = extraOptions;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.apache.jmeter.report.dashboard.HtmlTemplateExporter.
         * ResultCustomizer#customizeResult(org.apache.jmeter.report.processor.
         * ResultData)
         */
        @Override
        public ResultData customizeResult(ResultData result) {
            MapResultData customizedResult = new MapResultData();
            customizedResult.setResult(DATA_CTX_RESULT, result);

            if (extraOptions == null) {
                return customizedResult;
            }

            MapResultData extraResult = new MapResultData();
            extraOptions.getProperties().forEach((key, value) ->
                    extraResult.setResult(key, new ValueResultData(value)));
            customizedResult.setResult(DATA_CTX_EXTRA_OPTIONS, extraResult);

            return customizedResult;
        }
    }

    /** This class allows to check exported data */
    private interface ResultChecker {
        boolean checkResult(DataContext dataContext, ResultData result);
    }

    /** This class allows to detect empty graphs */
    private static class EmptyGraphChecker implements ResultChecker {

        private final boolean filtersOnlySampleSeries;
        private final boolean showControllerSeriesOnly;
        private final Pattern filterPattern;

        private boolean excludesControllers;
        private String graphId;

        public final void setExcludesControllers(boolean excludesControllers) {
            this.excludesControllers = excludesControllers;
        }

        public final void setGraphId(String graphId) {
            this.graphId = graphId;
        }

        /**
         * Instantiates a new EmptyGraphChecker.
         *
         * @param filtersOnlySampleSeries flag to control filter for samples
         * @param showControllerSeriesOnly flag to control visibility of controller
         * @param filterPattern to use
         */
        public EmptyGraphChecker(boolean filtersOnlySampleSeries,
                boolean showControllerSeriesOnly, Pattern filterPattern) {
            this.filtersOnlySampleSeries = filtersOnlySampleSeries;
            this.showControllerSeriesOnly = showControllerSeriesOnly;
            this.filterPattern = filterPattern;
        }

        /** @see ResultChecker#checkResult(DataContext, ResultData) */
        @Override
        public boolean checkResult(DataContext dataContext, ResultData result) {
            boolean supportsControllerDiscrimination = findValue(Boolean.class,
                    AbstractGraphConsumer.RESULT_SUPPORTS_CONTROLLERS_DISCRIMINATION,
                    result);

            if (supportsControllerDiscrimination
                    && showControllerSeriesOnly
                    && excludesControllers) {
                // Exporter shows controller series only
                // whereas the current graph support controller
                // discrimination and excludes controllers
                log.warn("{} is set while the graph {} excludes controllers.",
                        ReportGeneratorConfiguration.EXPORTER_KEY_SHOW_CONTROLLERS_ONLY, graphId);
                return false;
            }

            if (filterPattern == null) {
                return true;
            }

            // Detect whether none series matches the series filter.
            ResultData seriesResult = findData(AbstractGraphConsumer.RESULT_SERIES, result);
            if (!(seriesResult instanceof ListResultData)) {
                return true;
            }

            // Try to find at least one pattern matching
            ListResultData seriesList = (ListResultData) seriesResult;
            int count = seriesList.getSize();
            int index = 0;
            boolean matches = false;
            while (index < count && !matches) {
                ResultData currentResult = seriesList.get(index);
                if (currentResult instanceof MapResultData) {
                    MapResultData seriesData = (MapResultData) currentResult;
                    String name = findValue(String.class,
                            AbstractGraphConsumer.RESULT_SERIES_NAME,
                            seriesData);

                    // Is the current series a controller series ?
                    boolean isController = findValue(Boolean.class,
                            AbstractGraphConsumer.RESULT_SERIES_IS_CONTROLLER,
                            seriesData);

                    matches = filterPattern.matcher(name).matches();
                    if (matches) {
                        // If the name matches pattern, other
                        // properties can discard the series
                        matches = !filtersOnlySampleSeries
                                || !supportsControllerDiscrimination
                                || isController
                                || !showControllerSeriesOnly;
                        if(log.isDebugEnabled()) {
                            log.debug(
                                    "name:{} matches pattern:{}, supportsControllerDiscrimination:{}, "
                                    + "isController:{}, showControllerSeriesOnly:{}",
                                    name, filterPattern.pattern(),
                                    supportsControllerDiscrimination,
                                    isController,
                                    showControllerSeriesOnly);
                        }
                    } else {
                        // If the name does not match the pattern,
                        // other properties can hold the series
                        matches = filtersOnlySampleSeries
                                && !supportsControllerDiscrimination;
                        if (log.isDebugEnabled()) {
                            log.debug("name:{} does not match pattern:{}, filtersOnlySampleSeries:{},"
                                + " supportsControllerDiscrimination:{}",
                                name, filterPattern.pattern(),
                                filtersOnlySampleSeries,
                                supportsControllerDiscrimination);
                        }
                    }
                }
                index++;
            }
            if (!matches) {
                log.warn("No series matches the series_filter: {} in graph: {}",
                        ReportGeneratorConfiguration.EXPORTER_KEY_SERIES_FILTER, graphId);
                return false;
            }
            return true;
        }
    }

    private static boolean htmlReportFileFilter(File file) {
        String fileName = file.getName();
        boolean isIndexHtmlFile = file.isFile() && fileName.equals("index.html");
        boolean isContentOrAdmin = fileName.equals("content") || fileName.startsWith("sbadmin2-");

        return isIndexHtmlFile || (file.isDirectory() && isContentOrAdmin);
    }

    /**
     * @see DataExporter#export(SampleContext, File, ReportGeneratorConfiguration)
     */
    @Override
    public void export(SampleContext context, File file,
            ReportGeneratorConfiguration configuration) throws ExportException {
        Validate.notNull(context, MUST_NOT_BE_NULL, "context");
        Validate.notNull(file, MUST_NOT_BE_NULL, "file");
        Validate.notNull(configuration, MUST_NOT_BE_NULL, "configuration");

        log.debug("Start template processing");

        // Create data context and populate it
        DataContext dataContext = new DataContext();

        // Get the configuration of the current exporter
        final ExporterConfiguration exportCfg =
                configuration.getExportConfigurations().get(getName());

        // Get template directory property value
        File templateDirectory = getPropertyFromConfig(
                exportCfg,
                TEMPLATE_DIR,
                new File(JMeterUtils.getJMeterBinDir(), TEMPLATE_DIR_NAME_DEFAULT),
                File.class);
        if (!templateDirectory.isDirectory()) {
            String message = String.format(
                    "\"%s\" is not a valid template directory",
                    templateDirectory.getAbsolutePath());
            log.error(message);
            throw new ExportException(message);
        }

        // Get output directory property value
        File outputDir = getPropertyFromConfig(exportCfg, OUTPUT_DIR,
                new File(JMeterUtils.getJMeterBinDir(), OUTPUT_DIR_NAME_DEFAULT), File.class);
        String globallyDefinedOutputDir = JMeterUtils.getProperty(JMeter.JMETER_REPORT_OUTPUT_DIR_PROPERTY);
        if(!StringUtils.isEmpty(globallyDefinedOutputDir)) {
            outputDir = new File(globallyDefinedOutputDir);
        }

        JOrphanUtils.canSafelyWriteToFolder(outputDir, HtmlTemplateExporter::htmlReportFileFilter);

        if (log.isInfoEnabled()) {
            log.info("Will generate dashboard in folder: {}", outputDir.getAbsolutePath());
        }

        // Add a flag defining if only sample series are filtered to the context
        final boolean filtersOnlySampleSeries = exportCfg.filtersOnlySampleSeries();
        addToContext(
                DATA_CTX_FILTERS_ONLY_SAMPLE_SERIES,
                filtersOnlySampleSeries,
                dataContext);

        // Add the series filter to the context
        final String seriesFilter = exportCfg.getSeriesFilter();
        Pattern filterPattern = null;
        if (StringUtils.isNotBlank(seriesFilter)) {
            try {
                filterPattern = Pattern.compile(seriesFilter);
            } catch (PatternSyntaxException ex) {
                log.error("Invalid series filter: '{}', {}", seriesFilter, ex.getDescription());
            }
        }
        addToContext(DATA_CTX_SERIES_FILTER, seriesFilter, dataContext);

        // Add the flag defining whether only controller series are displayed
        final boolean showControllerSeriesOnly = exportCfg.showControllerSeriesOnly();
        addToContext(
                DATA_CTX_SHOW_CONTROLLERS_ONLY,
                showControllerSeriesOnly,
                dataContext);

        JsonizerVisitor jsonizer = new JsonizerVisitor();
        Map<String, Object> storedData = context.getData();

        // Add begin date consumer result to the data context
        addResultToContext(
                ReportGenerator.BEGIN_DATE_CONSUMER_NAME, storedData, dataContext, jsonizer);

        // Add end date summary consumer result to the data context
        addResultToContext(
                ReportGenerator.END_DATE_CONSUMER_NAME, storedData, dataContext, jsonizer);

        // Add Apdex summary consumer result to the data context
        addResultToContext(
                ReportGenerator.APDEX_SUMMARY_CONSUMER_NAME, storedData, dataContext, jsonizer);

        // Add errors summary consumer result to the data context
        addResultToContext(
                ReportGenerator.ERRORS_SUMMARY_CONSUMER_NAME, storedData, dataContext, jsonizer);

        // Add requests summary consumer result to the data context
        addResultToContext(
                ReportGenerator.REQUESTS_SUMMARY_CONSUMER_NAME, storedData, dataContext, jsonizer);

        // Add statistics summary consumer result to the data context
        addResultToContext(
                ReportGenerator.STATISTICS_SUMMARY_CONSUMER_NAME, storedData, dataContext, jsonizer);

        // Add Top 5 errors by sampler consumer result to the data context
        addResultToContext(
                ReportGenerator.TOP5_ERRORS_BY_SAMPLER_CONSUMER_NAME, storedData, dataContext, jsonizer);

        // Collect graph results from sample context and transform them into
        // Json strings to inject in the data context
        ExtraOptionsResultCustomizer customizer = new ExtraOptionsResultCustomizer();
        EmptyGraphChecker checker =
                new EmptyGraphChecker(filtersOnlySampleSeries, showControllerSeriesOnly, filterPattern);
        Map<String, GraphConfiguration> mapConfiguration = new HashMap<>();
        DataContext customGraphs = new DataContext();

        for (Map.Entry<String, GraphConfiguration> graphEntry : configuration.getGraphConfigurations().entrySet()) {
            final String graphId = graphEntry.getKey();
            final GraphConfiguration graphConfiguration = graphEntry.getValue();

            // Initialize customizer and checker
            customizer.setExtraOptions(exportCfg.getGraphExtraConfigurations().get(graphId));
            checker.setExcludesControllers(graphConfiguration.excludesControllers());
            checker.setGraphId(graphId);
            mapConfiguration.put(graphId, graphConfiguration);
            if (graphId.startsWith(CUSTOM_GRAPH_PREFIX)) {
                addResultToContext(
                        graphId, storedData, customGraphs, jsonizer, customizer, checker);
            } else {
                // Export graph data
                addResultToContext(
                        graphId, storedData, dataContext, jsonizer, customizer, checker);
            }
        }
        dataContext.put("graphConfigurations", mapConfiguration);
        dataContext.put("customsGraphsData", customGraphs);

        // Replace the begin date with its formatted string and store the old timestamp
        long oldTimestamp = formatTimestamp(
                ReportGenerator.BEGIN_DATE_CONSUMER_NAME, dataContext);

        // Replace the end date with its formatted string
        formatTimestamp(ReportGenerator.END_DATE_CONSUMER_NAME, dataContext);

        // Add time zone offset (that matches the begin date) to the context
        TimeZone timezone = TimeZone.getDefault();
        addToContext(
                DATA_CTX_TIMEZONE_OFFSET,
                timezone.getOffset(oldTimestamp),
                dataContext);

        // Add report title to the context
        if (StringUtils.isNotEmpty(configuration.getReportTitle())) {
            dataContext.put(DATA_CTX_REPORT_TITLE, StringEscapeUtils.escapeHtml4(configuration.getReportTitle()));
        }

        // Add the test file name to the context
        addToContext(DATA_CTX_TESTFILE, file.getName(), dataContext);

        // Add the overall filter property to the context
        addToContext(DATA_CTX_OVERALL_FILTER, configuration.getSampleFilter(), dataContext);

        // Walk template directory to copy files and process templated ones
        Configuration templateCfg = new Configuration(Configuration.VERSION_2_3_30);
        try {
            templateCfg.setDirectoryForTemplateLoading(templateDirectory);
            templateCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            if (log.isInfoEnabled()) {
                log.info("Report will be generated in: {}, creating folder structure", outputDir.getAbsolutePath());
            }
            FileUtils.forceMkdir(outputDir);
            TemplateVisitor visitor = new TemplateVisitor(
                    templateDirectory.toPath(),
                    outputDir.toPath(),
                    templateCfg,
                    dataContext);
            Files.walkFileTree(templateDirectory.toPath(), visitor);
        } catch (IOException ex) {
            throw new ExportException("Unable to process template files.", ex);
        }

        log.debug("End of template processing");
    }

    private static <T> void addResultToContext(
            String resultKey, Map<String, Object> storage,
            DataContext dataContext, ResultDataVisitor<T> visitor) {
        addResultToContext(resultKey, storage, dataContext, visitor, null, null);
    }

    private static <T> void addResultToContext(
            String resultKey, Map<String, Object> storage, DataContext dataContext,
            ResultDataVisitor<T> visitor, ResultCustomizer customizer, ResultChecker checker) {
        Object data = storage.get(resultKey);
        if (data instanceof ResultData) {
            ResultData result = (ResultData) data;
            if (checker != null) {
                checker.checkResult(dataContext, result);
            }
            if (customizer != null) {
                result = customizer.customizeResult(result);
            }
            dataContext.put(resultKey, result.accept(visitor));
        }
    }

    private static long formatTimestamp(String key, DataContext context) {
        // FIXME Why convert to double then long (rounding ?)
        double result = Double.parseDouble((String) context.get(key));
        long timestamp = (long) result;
        // Quote the string to respect Json spec.
        context.put(key, '"' + TimeHelper.formatTimeStamp(timestamp) + '"');
        return timestamp;
    }
}
