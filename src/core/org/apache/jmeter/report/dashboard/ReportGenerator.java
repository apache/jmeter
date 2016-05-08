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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.config.ExporterConfiguration;
import org.apache.jmeter.report.config.GraphConfiguration;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.core.ControllerSamplePredicate;
import org.apache.jmeter.report.core.ConvertException;
import org.apache.jmeter.report.core.Converters;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.report.core.SampleException;
import org.apache.jmeter.report.core.SamplePredicate;
import org.apache.jmeter.report.core.SampleSelector;
import org.apache.jmeter.report.core.StringConverter;
import org.apache.jmeter.report.processor.AbstractSampleConsumer;
import org.apache.jmeter.report.processor.AggregateConsumer;
import org.apache.jmeter.report.processor.ApdexSummaryConsumer;
import org.apache.jmeter.report.processor.ApdexThresholdsInfo;
import org.apache.jmeter.report.processor.CsvFileSampleSource;
import org.apache.jmeter.report.processor.ErrorsSummaryConsumer;
import org.apache.jmeter.report.processor.FilterConsumer;
import org.apache.jmeter.report.processor.MaxAggregator;
import org.apache.jmeter.report.processor.MinAggregator;
import org.apache.jmeter.report.processor.NormalizerSampleConsumer;
import org.apache.jmeter.report.processor.RequestsSummaryConsumer;
import org.apache.jmeter.report.processor.SampleContext;
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
 * @since 3.0
 */
public class ReportGenerator {
    private static final String REPORTGENERATOR_PROPERTIES = "reportgenerator.properties";

    private static final Logger LOG = LoggingManager.getLoggerForClass();

    private static final boolean CSV_OUTPUT_FORMAT = "csv"
            .equalsIgnoreCase(JMeterUtils.getPropDefault(
                    "jmeter.save.saveservice.output_format", "csv"));

    private static final char CSV_DEFAULT_SEPARATOR =
            JMeterUtils.getPropDefault("jmeter.save.saveservice.default_delimiter", ",").charAt(0); //$NON-NLS-1$ //$NON-NLS-2$

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

    private static final Pattern POTENTIAL_CAMEL_CASE_PATTERN = Pattern.compile("_(.)");

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
     * @throws ConfigurationException when loading configuration from file fails
     */
    public ReportGenerator(String resultsFile, ResultCollector resultCollector)
            throws ConfigurationException {
        if (!CSV_OUTPUT_FORMAT) {
            throw new IllegalArgumentException(
                    "Report generation requires csv output format, check 'jmeter.save.saveservice.output_format' property");
        }

        LOG.info("ReportGenerator will use for Parsing the separator:'"+CSV_DEFAULT_SEPARATOR+"'");

        File file = new File(resultsFile);
        if (resultCollector == null) {
            if (!(file.isFile() && file.canRead())) {
                throw new IllegalArgumentException(String.format(
                        "Cannot read test results file : %s", file));
            }
            LOG.info("Will only generate report from results file:"
                    + resultsFile);
        } else {
            if (file.exists() && file.length() > 0) {
                throw new IllegalArgumentException("Results file:"
                        + resultsFile + " is not empty");
            }
            LOG.info("Will generate report at end of test from  results file:"
                    + resultsFile);
        }
        this.resultCollector = resultCollector;
        this.testFile = file;
        final Properties merged = new Properties();
        File rgp = new File(JMeterUtils.getJMeterBinDir(), REPORTGENERATOR_PROPERTIES);
        if(LOG.isInfoEnabled()) {
            LOG.info("Reading report generator properties from:"+rgp.getAbsolutePath());
        }
        merged.putAll(loadProps(rgp));
        if(LOG.isInfoEnabled()) {
            LOG.info("Merging with JMeter properties");
        }
        merged.putAll(JMeterUtils.getJMeterProperties());
        configuration = ReportGeneratorConfiguration.loadFromProperties(merged);
    }

    private static Properties loadProps(File file) {
        final Properties props = new Properties();
        try (FileInputStream inStream = new FileInputStream(file)) {
            props.load(inStream);
        } catch (IOException e) {
            LOG.error("Problem loading properties from file ", e);
            System.err.println("Problem loading properties " + e);
        }
        return props;
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
        Matcher matcher = POTENTIAL_CAMEL_CASE_PATTERN.matcher(propertyKey);
        StringBuffer buffer = new StringBuffer(); // Unfortunately Matcher does not support StringBuilder
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
            LOG.info("Flushing result collector before report Generation");
            resultCollector.flushFile();
        }
        LOG.debug("Start report generation");

        File tmpDir = configuration.getTempDirectory();
        boolean tmpDirCreated = createTempDir(tmpDir);

        // Build consumers chain
        SampleContext sampleContext = new SampleContext();
        sampleContext.setWorkingDirectory(tmpDir);
        SampleSource source = new CsvFileSampleSource(testFile, JMeterUtils
                .getPropDefault("jmeter.save.saveservice.default_delimiter",
                        ",").charAt(0));
        source.setSampleContext(sampleContext);

        NormalizerSampleConsumer normalizer = new NormalizerSampleConsumer();
        normalizer.setName(NORMALIZER_CONSUMER_NAME);

        normalizer.addSampleConsumer(createBeginDateConsumer());
        normalizer.addSampleConsumer(createEndDateConsumer());

        FilterConsumer nameFilter = createNameFilter();

        FilterConsumer excludeControllerFilter = createExcludeControllerFilter();

        nameFilter.addSampleConsumer(excludeControllerFilter);

        normalizer.addSampleConsumer(nameFilter);

        source.addSampleConsumer(normalizer);

        // Get graph configurations
        Map<String, GraphConfiguration> graphConfigurations = configuration
                .getGraphConfigurations();

        // Process configuration to build graph consumers
        for (Map.Entry<String, GraphConfiguration> entryGraphCfg : graphConfigurations
                .entrySet()) {
            addGraphConsumer(nameFilter, excludeControllerFilter,
                    entryGraphCfg);
        }

        // Generate data
        LOG.debug("Start samples processing");
        try {
            source.run();
        } catch (SampleException ex) {
            throw new GenerationException("Error while processing samples:"+ex.getMessage(), ex);
        }
        LOG.debug("End of samples processing");

        LOG.debug("Start data exporting");

        // Process configuration to build data exporters
        for (Map.Entry<String, ExporterConfiguration> entry : configuration
                .getExportConfigurations().entrySet()) {
            LOG.info("Exporting data using exporter:'"
                +entry.getKey()+"' of className:'"+entry.getValue().getClassName()+"'");
            exportData(sampleContext, entry.getKey(), entry.getValue());
        }

        LOG.debug("End of data exporting");

        removeTempDir(tmpDir, tmpDirCreated);

        LOG.debug("End of report generation");

    }

    private void removeTempDir(File tmpDir, boolean tmpDirCreated) {
        if (tmpDirCreated) {
            try {
                FileUtils.deleteDirectory(tmpDir);
            } catch (IOException ex) {
                LOG.warn(String.format(
                        "Cannot delete created temporary directory \"%s\".",
                        tmpDir), ex);
            }
        }
    }

    private boolean createTempDir(File tmpDir) throws GenerationException {
        boolean tmpDirCreated = false;
        if (!tmpDir.exists()) {
            tmpDirCreated = tmpDir.mkdir();
            if (!tmpDirCreated) {
                String message = String.format(
                        "Cannot create temporary directory \"%s\".", tmpDir);
                LOG.error(message);
                throw new GenerationException(message);
            }
        }
        return tmpDirCreated;
    }

    private void addGraphConsumer(FilterConsumer nameFilter,
            FilterConsumer excludeControllerFilter,
            Map.Entry<String, GraphConfiguration> entryGraphCfg)
            throws GenerationException {
        String graphName = entryGraphCfg.getKey();
        GraphConfiguration graphConfiguration = entryGraphCfg.getValue();

        // Instantiate the class from the classname
        String className = graphConfiguration.getClassName();
        try {
            Class<?> clazz = Class.forName(className);
            Object obj = clazz.newInstance();
            AbstractGraphConsumer graph = (AbstractGraphConsumer) obj;
            graph.setName(graphName);
            
            // Set the graph title
            graph.setTitle(graphConfiguration.getTitle());

            // Set graph properties using reflection
            Method[] methods = clazz.getMethods();
            for (Map.Entry<String, String> entryProperty : graphConfiguration
                    .getProperties().entrySet()) {
                String propertyName = entryProperty.getKey();
                String propertyValue = entryProperty.getValue();
                String setterName = getSetterName(propertyName);

                setProperty(className, obj, methods, propertyName,
                        propertyValue, setterName);
            }

            // Choose which entry point to use to plug the graph
            AbstractSampleConsumer entryPoint = graphConfiguration
                    .excludesControllers() ? excludeControllerFilter
                    : nameFilter;
            entryPoint.addSampleConsumer(graph);
        } catch (ClassNotFoundException | IllegalAccessException
                | InstantiationException | ClassCastException ex) {
            String error = String.format(INVALID_CLASS_FMT, className);
            LOG.error(error, ex);
            throw new GenerationException(error, ex);
        }
    }

    private void exportData(SampleContext sampleContext, String exporterName,
            ExporterConfiguration exporterConfiguration)
            throws GenerationException {
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
            LOG.error(error, ex);
            throw new GenerationException(error, ex);
        } catch (ExportException ex) {
            String error = String.format(INVALID_EXPORT_FMT, exporterName);
            LOG.error(error, ex);
            throw new GenerationException(error, ex);
        }
    }

    private ErrorsSummaryConsumer createErrorsSummaryConsumer() {
        ErrorsSummaryConsumer errorsSummaryConsumer = new ErrorsSummaryConsumer();
        errorsSummaryConsumer.setName(ERRORS_SUMMARY_CONSUMER_NAME);
        return errorsSummaryConsumer;
    }

    private FilterConsumer createExcludeControllerFilter() {
        FilterConsumer excludeControllerFilter = new FilterConsumer();
        excludeControllerFilter
                .setName(START_INTERVAL_CONTROLLER_FILTER_CONSUMER_NAME);
        excludeControllerFilter
                .setSamplePredicate(new ControllerSamplePredicate());
        excludeControllerFilter.setReverseFilter(true);
        excludeControllerFilter.addSampleConsumer(createErrorsSummaryConsumer());
        return excludeControllerFilter;
    }

    private StatisticsSummaryConsumer createStatisticsSummaryConsumer() {
        StatisticsSummaryConsumer statisticsSummaryConsumer = new StatisticsSummaryConsumer();
        statisticsSummaryConsumer.setName(STATISTICS_SUMMARY_CONSUMER_NAME);
        statisticsSummaryConsumer.setHasOverallResult(true);
        return statisticsSummaryConsumer;
    }

    private RequestsSummaryConsumer createRequestsSummaryConsumer() {
        RequestsSummaryConsumer requestsSummaryConsumer = new RequestsSummaryConsumer();
        requestsSummaryConsumer.setName(REQUESTS_SUMMARY_CONSUMER_NAME);
        return requestsSummaryConsumer;
    }

    private ApdexSummaryConsumer createApdexSummaryConsumer() {
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
        return apdexSummaryConsumer;
    }

    private FilterConsumer createNameFilter() {
        FilterConsumer nameFilter = new FilterConsumer();
        nameFilter.setName(NAME_FILTER_CONSUMER_NAME);
        nameFilter.setSamplePredicate(new SamplePredicate() {

            @Override
            public boolean matches(Sample sample) {
                // Get filtered samples from configuration
                Pattern filteredSamplesPattern = configuration
                        .getFilteredSamplesPattern();
                // Sample is kept if no filter is set 
                // or if its name matches the filter pattern
                return filteredSamplesPattern == null 
                        || filteredSamplesPattern.matcher(sample.getName()).matches();
            }
        });
        nameFilter.addSampleConsumer(createApdexSummaryConsumer());
        nameFilter.addSampleConsumer(createRequestsSummaryConsumer());
        nameFilter.addSampleConsumer(createStatisticsSummaryConsumer());
        return nameFilter;
    }

    private AggregateConsumer createEndDateConsumer() {
        AggregateConsumer endDateConsumer = new AggregateConsumer(
                new MaxAggregator(), new SampleSelector<Double>() {

                    @Override
                    public Double select(Sample sample) {
                        return Double.valueOf(sample.getEndTime());
                    }
                });
        endDateConsumer.setName(END_DATE_CONSUMER_NAME);
        return endDateConsumer;
    }

    private AggregateConsumer createBeginDateConsumer() {
        AggregateConsumer beginDateConsumer = new AggregateConsumer(
                new MinAggregator(), new SampleSelector<Double>() {

                    @Override
                    public Double select(Sample sample) {
                        return Double.valueOf(sample.getStartTime());
                    }
                });
        beginDateConsumer.setName(BEGIN_DATE_CONSUMER_NAME);
        return beginDateConsumer;
    }

    /**
     * Try to set a property on an object by reflection.
     *
     * @param className
     *            name of the objects class
     * @param obj
     *            the object on which the property should be set
     * @param methods
     *            methods of the object which will be search for the property
     *            setter
     * @param propertyName
     *            name of the property to be set
     * @param propertyValue
     *            value to be set
     * @param setterName
     *            name of the property setter that should be used to set the
     *            property
     * @throws IllegalAccessException
     *             if reflection throws an IllegalAccessException
     * @throws GenerationException
     *             if conversion of the property value fails or reflection
     *             throws an InvocationTargetException
     */
    private void setProperty(String className, Object obj, Method[] methods,
            String propertyName, String propertyValue, String setterName)
            throws IllegalAccessException, GenerationException {
        try {
            int i = 0;
            while (i < methods.length) {
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
                        return;
                    }
                }
                i++;
            }
            LOG.warn(String
                        .format("\"%s\" is not a valid property for class \"%s\", skip it",
                                propertyName, className));
        } catch (InvocationTargetException | ConvertException ex) {
            String message = String
                    .format("Cannot assign \"%s\" to property \"%s\" (mapped as \"%s\"), skip it",
                            propertyValue, propertyName, setterName);
            LOG.error(message, ex);
            throw new GenerationException(message, ex);
        }
    }
}
