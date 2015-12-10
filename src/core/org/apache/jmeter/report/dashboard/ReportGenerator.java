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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.config.ConvertException;
import org.apache.jmeter.report.config.Converters;
import org.apache.jmeter.report.config.ExporterConfiguration;
import org.apache.jmeter.report.config.GraphConfiguration;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.config.StringConverter;
import org.apache.jmeter.report.core.ControllerSamplePredicate;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SamplePredicate;
import org.apache.jmeter.report.core.SampleSelector;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;
import org.apache.jmeter.report.processor.AggregateConsumer;
import org.apache.jmeter.report.processor.ApdexSummaryConsumer;
import org.apache.jmeter.report.processor.ApdexThresholdsInfo;
import org.apache.jmeter.report.processor.ErrorsSummaryConsumer;
import org.apache.jmeter.report.processor.FilterConsumer;
import org.apache.jmeter.report.processor.MaxAggregator;
import org.apache.jmeter.report.processor.MinAggregator;
import org.apache.jmeter.report.processor.NormalizerSampleConsumer;
import org.apache.jmeter.report.processor.RequestsSummaryConsumer;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.CsvFileSampleSource;
import org.apache.jmeter.report.processor.SampleSource;
import org.apache.jmeter.report.processor.StatisticsSummaryConsumer;
import org.apache.jmeter.report.processor.ThresholdSelector;
import org.apache.jmeter.report.processor.graph.AbstractGraphConsumer;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The class ReportGenerator provides a way to generate all the templated files
 * of the plugin.
 * 
 * @since 2.14
 */
public class ReportGenerator {
    private static final Logger log = LoggingManager.getLoggerForClass();

    // /** A properties file indicator for true. * */
    // private static final String TRUE = "true"; // $NON_NLS-1$
    //
    // /** A properties file indicator for false. * */
    // private static final String FALSE = "false"; // $NON_NLS-1$

    // private static final boolean PRINT_FIELD_NAMES = TRUE
    // .equalsIgnoreCase(JMeterUtils.getPropDefault(
    // "jmeter.save.saveservice.print_field_names", FALSE));

    private static final boolean CSV_OUTPUT_FORMAT = "csv"
            .equalsIgnoreCase(JMeterUtils.getPropDefault(
                    "jmeter.save.saveservice.output_format", "csv"));

    private static final String INVALID_CLASS_FMT = "Class name \"%s\" is not valid.";
    private static final String INVALID_EXPORT_FMT = "Data exporter \"%s\" is unable to export data.";
    private static final String NOT_SUPPORTED_CONVERTION_FMT = "Not supported conversion to \"%s\"";

    public static final String NORMALIZER_CONSUMER_NAME = "normalizer";
    public static final String BEGIN_DATE_CONSUMER_NAME = "beginDate";
    public static final String END_DATE_CONSUMER_NAME = "endDate";
    public static final String NAME_FILTER_CONSUMER_NAME = "nameFilter";
    public static final String APDEX_SUMMARY_CONSUMER_NAME = "apdexSummary";
    public static final String ERRORS_SUMMARY_CONSUMER_NAME = "errorsSummary";
    public static final String REQUESTS_SUMMARY_CONSUMER_NAME = "requestsSummary";
    public static final String STATISTICS_SUMMARY_CONSUMER_NAME = "statisticsSummary";
    public static final String START_INTERVAL_CONTROLLER_FILTER_CONSUMER_NAME = "startIntervalControlerFilter";

    private final File testFile;
    private final ReportGeneratorConfiguration configuration;

    /**
     * ResultCollector used
     */
    private final ResultCollector resultCollector;

    /**
     * Instantiates a new report generator.
     *
     * @param resultsFile
     *            the test results file
     * @param resultCollector
     *            Can be null, used if generation occurs at end of test
     */
    public ReportGenerator(String resultsFile, ResultCollector resultCollector)
            throws ConfigurationException {
        if (!CSV_OUTPUT_FORMAT) {
            throw new IllegalArgumentException(
                    "Report generation requires csv output format, check 'jmeter.save.saveservice.output_format' property");
        }
        // if (!PRINT_FIELD_NAMES) {
        // throw new IllegalArgumentException(
        // "Report generation requires csv to print field names, check 'jmeter.save.saveservice.print_field_names' property");
        // }

        File file = new File(resultsFile);
        if (resultCollector == null) {
            if (!(file.isFile() && file.canRead())) {
                throw new IllegalArgumentException(String.format(
                        "Invalid test results file : %s", file));
            }
            log.info("Will only generate report from results file:"
                    + resultsFile);
        } else {
            if (file.exists() && file.length() > 0) {
                throw new IllegalArgumentException("Results file:"
                        + resultsFile + " is not empty");
            }
            log.info("Will generate report at end of test from  results file:"
                    + resultsFile);
        }
        this.resultCollector = resultCollector;
        this.testFile = file;
        configuration = ReportGeneratorConfiguration
                .LoadFromProperties(JMeterUtils.getJMeterProperties());
    }

    /**
     * <p>
     * Gets the name of property setter from the specified key.
     * </p>
     * <p>
     * E.g : with key set_granularity, returns setGranularity (camel case)
     * </p>
     * 
     * @param propertyKey
     *            the property key
     * @return the name of the property setter
     */
    private static String getSetterName(String propertyKey) {
        // TODO use jmeter regex cache
        Pattern pattern = Pattern.compile("_(.)");
        Matcher matcher = pattern.matcher(propertyKey);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Generate dashboard reports using the data from the specified CSV File.
     *
     * @throws GenerationException
     *             when the generation failed
     */
    public void generate() throws GenerationException {

        if (resultCollector != null) {
            log.info("Flushing result collector before report Generation");
            resultCollector.flushFile();
        }
        log.debug("Start report generation");

        File tmpDir = configuration.getTempDirectory();
        boolean tmpDirCreated = false;
        if (tmpDir.exists() == false) {
            tmpDirCreated = tmpDir.mkdir();
            if (tmpDirCreated == false) {
                String message = String.format(
                        "Cannot create temporary directory \"%s\".", tmpDir);
                log.error(message);
                throw new GenerationException(message);
            }
        }

        // Build consumers chain
        SampleContext sampleContext = new SampleContext();
        sampleContext.setWorkingDirectory(tmpDir);
        SampleSource source = new CsvFileSampleSource(testFile, JMeterUtils
                .getPropDefault("jmeter.save.saveservice.default_delimiter",
                        ",").charAt(0));
        source.setSampleContext(sampleContext);

        NormalizerSampleConsumer normalizer = new NormalizerSampleConsumer();
        normalizer.setName(NORMALIZER_CONSUMER_NAME);
        source.addSampleConsumer(normalizer);

        AggregateConsumer beginDateConsumer = new AggregateConsumer(
                new MinAggregator(), new SampleSelector<Double>() {

                    @Override
                    public Double select(Sample sample) {
                        return (double) sample.getStartTime();
                    }
                });
        beginDateConsumer.setName(BEGIN_DATE_CONSUMER_NAME);
        normalizer.addSampleConsumer(beginDateConsumer);

        AggregateConsumer endDateConsumer = new AggregateConsumer(
                new MaxAggregator(), new SampleSelector<Double>() {

                    @Override
                    public Double select(Sample sample) {
                        return (double) sample.getEndTime();
                    }
                });
        endDateConsumer.setName(END_DATE_CONSUMER_NAME);
        normalizer.addSampleConsumer(endDateConsumer);

        FilterConsumer nameFilter = new FilterConsumer();
        nameFilter.setName(NAME_FILTER_CONSUMER_NAME);
        nameFilter.setSamplePredicate(new SamplePredicate() {

            @Override
            public boolean matches(Sample sample) {
                // Get filtered samples from configuration
                List<String> filteredSamples = configuration
                        .getFilteredSamples();
                // Sample is kept if none filter is set or if the filter
                // contains its name
                return filteredSamples.size() == 0
                        || filteredSamples.contains(sample.getName());
            }
        });
        normalizer.setSampleConsumer(nameFilter);

        ApdexSummaryConsumer apdexSummaryConsumer = new ApdexSummaryConsumer();
        apdexSummaryConsumer.setName(APDEX_SUMMARY_CONSUMER_NAME);
        apdexSummaryConsumer.setHasOverallResult(true);
        apdexSummaryConsumer.setThresholdSelector(new ThresholdSelector() {

            @Override
            public ApdexThresholdsInfo select(String sampleName) {
                ApdexThresholdsInfo info = new ApdexThresholdsInfo();
                info.setSatisfiedThreshold(configuration
                        .getApdexSatisfiedThreshold());
                info.setToleratedThreshold(configuration
                        .getApdexToleratedThreshold());
                return info;
            }
        });
        nameFilter.setSampleConsumer(apdexSummaryConsumer);

        RequestsSummaryConsumer requestsSummaryConsumer = new RequestsSummaryConsumer();
        requestsSummaryConsumer.setName(REQUESTS_SUMMARY_CONSUMER_NAME);
        nameFilter.setSampleConsumer(requestsSummaryConsumer);

        StatisticsSummaryConsumer statisticsSummaryConsumer = new StatisticsSummaryConsumer();
        statisticsSummaryConsumer.setName(STATISTICS_SUMMARY_CONSUMER_NAME);
        statisticsSummaryConsumer.setHasOverallResult(true);
        nameFilter.setSampleConsumer(statisticsSummaryConsumer);

        FilterConsumer excludeControllerFilter = new FilterConsumer();
        excludeControllerFilter
                .setName(START_INTERVAL_CONTROLLER_FILTER_CONSUMER_NAME);
        excludeControllerFilter
                .setSamplePredicate(new ControllerSamplePredicate());
        excludeControllerFilter.setReverseFilter(true);
        nameFilter.setSampleConsumer(excludeControllerFilter);

        ErrorsSummaryConsumer errorsSummaryConsumer = new ErrorsSummaryConsumer();
        errorsSummaryConsumer.setName(ERRORS_SUMMARY_CONSUMER_NAME);
        excludeControllerFilter.setSampleConsumer(errorsSummaryConsumer);

        // Get graph configurations
        Map<String, GraphConfiguration> graphConfigurations = configuration
                .getGraphConfigurations();

        // Process configuration to build graph consumers
        HashMap<GraphConfiguration, AbstractGraphConsumer> graphMap = new HashMap<>();
        for (Map.Entry<String, GraphConfiguration> entryGraphCfg : graphConfigurations
                .entrySet()) {
            String graphName = entryGraphCfg.getKey();
            GraphConfiguration graphConfiguration = entryGraphCfg.getValue();

            // Instantiate the class from the classname
            String className = graphConfiguration.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.newInstance();
                AbstractGraphConsumer graph = (AbstractGraphConsumer) obj;
                graph.setName(graphName);

                // Set graph properties using reflection
                Method[] methods = clazz.getMethods();
                for (Map.Entry<String, String> entryProperty : graphConfiguration
                        .getProperties().entrySet()) {
                    String propertyName = entryProperty.getKey();
                    String propertyValue = entryProperty.getValue();
                    String setterName = getSetterName(propertyName);

                    try {
                        int i = 0;
                        boolean invoked = false;
                        while (i < methods.length && invoked == false) {
                            Method method = methods[i];
                            if (method.getName().equals(setterName)) {
                                Class<?>[] parameterTypes = method
                                        .getParameterTypes();
                                if (parameterTypes.length == 1) {
                                    Class<?> parameterType = parameterTypes[0];
                                    if (parameterType
                                            .isAssignableFrom(String.class)) {
                                        method.invoke(obj, propertyValue);
                                    } else {
                                        StringConverter<?> converter = Converters
                                                .getConverter(parameterType);
                                        if (converter == null) {
                                            throw new GenerationException(
                                                    String.format(
                                                            NOT_SUPPORTED_CONVERTION_FMT,
                                                            parameterType
                                                                    .getName()));
                                        }
                                        method.invoke(obj, converter
                                                .convert(propertyValue));
                                    }
                                    invoked = true;
                                }
                            }
                            i++;
                        }
                        if (invoked == false) {
                            log.warn(String
                                    .format("\"%s\" is not a valid property for class \"%s\", skip it",
                                            propertyName, className));
                        }
                    } catch (InvocationTargetException | ConvertException ex) {
                        String message = String
                                .format("Cannot assign \"%s\" to property \"%s\" (mapped as \"%s\"), skip it",
                                        propertyValue, propertyName, setterName);
                        log.error(message, ex);
                        throw new GenerationException(message, ex);
                    }
                }

                // Choose which entry point to use to plug the graph
                AbstractSampleConsumer entryPoint = graphConfiguration
                        .excludesControllers() ? excludeControllerFilter
                        : nameFilter;
                entryPoint.addSampleConsumer(graph);

                // Add to the map
                graphMap.put(graphConfiguration, graph);
            } catch (ClassNotFoundException | IllegalAccessException
                    | InstantiationException | ClassCastException ex) {
                String error = String.format(INVALID_CLASS_FMT, className);
                log.error(error, ex);
                throw new GenerationException(error, ex);
            }
        }

        // Generate data
        log.debug("Start samples processing");
        try {
            source.run();
        } catch (SampleException ex) {
            String message = "Error while processing samples";
            log.error(message, ex);
            throw new GenerationException(message, ex);
        }
        log.debug("End of samples processing");

        log.debug("Start data exporting");

        // Process configuration to build data exporters
        for (Map.Entry<String, ExporterConfiguration> entry : configuration
                .getExportConfigurations().entrySet()) {
            String exporterName = entry.getKey();
            ExporterConfiguration exporterConfiguration = entry.getValue();

            // Instantiate the class from the classname
            String className = exporterConfiguration.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.newInstance();
                DataExporter exporter = (DataExporter) obj;
                exporter.setName(exporterName);

                // Export data
                exporter.export(sampleContext, testFile, configuration);
            } catch (ClassNotFoundException | IllegalAccessException
                    | InstantiationException | ClassCastException ex) {
                String error = String.format(INVALID_CLASS_FMT, className);
                log.error(error, ex);
                throw new GenerationException(error, ex);
            } catch (ExportException ex) {
                String error = String.format(INVALID_EXPORT_FMT, exporterName);
                log.error(error, ex);
                throw new GenerationException(error, ex);
            }
        }

        log.debug("End of data exporting");

        if (tmpDirCreated == true) {
            try {
                FileUtils.deleteDirectory(tmpDir);
            } catch (IOException ex) {
                log.warn(String.format(
                        "Cannot delete created temporary directory \"%s\".",
                        tmpDir), ex);
            }
        }

        log.debug("End of report generation");

    }
}
