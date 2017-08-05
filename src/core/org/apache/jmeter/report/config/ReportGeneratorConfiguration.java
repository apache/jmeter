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

package org.apache.jmeter.report.config;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jodd.props.Props;

/**
 * The class ReportGeneratorConfiguration describes the configuration of the
 * report generator.
 *
 * @since 3.0
 */
public class ReportGeneratorConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ReportGeneratorConfiguration.class);

    private static final String RANGE_DATE_FORMAT_DEFAULT = "yyyyMMddHHmmss"; //$NON-NLS-1$

    public static final char KEY_DELIMITER = '.';
    public static final String REPORT_GENERATOR_KEY_PREFIX = "jmeter.reportgenerator";
    
    public static final String REPORT_GENERATOR_KEY_RANGE_DATE_FORMAT = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "date_format";
    
    public static final String REPORT_GENERATOR_GRAPH_KEY_PREFIX = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "graph";
    public static final String REPORT_GENERATOR_EXPORTER_KEY_PREFIX = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "exporter";

    // Temporary directory
    private static final String REPORT_GENERATOR_KEY_TEMP_DIR = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "temp_dir";
    private static final File REPORT_GENERATOR_KEY_TEMP_DIR_DEFAULT = new File(
            "temp");

    // Apdex Satisfied Threshold
    private static final String REPORT_GENERATOR_KEY_APDEX_SATISFIED_THRESHOLD = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "apdex_satisfied_threshold";
    private static final Long REPORT_GENERATOR_KEY_APDEX_SATISFIED_THRESHOLD_DEFAULT = Long.valueOf(500L);

    // Apdex Tolerated Threshold
    private static final String REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "apdex_tolerated_threshold";
    private static final Long REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD_DEFAULT = Long.valueOf(1500L);
    
    // Apdex per transaction Thresholds
    private static final String REPORT_GENERATOR_KEY_APDEX_PER_TRANSACTION = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "apdex_per_transaction";

    // Exclude Transaction Controller from Top5 Errors by Sampler consumer
    private static final String REPORT_GENERATOR_KEY_EXCLUDE_TC_FROM_TOP5_ERRORS_BY_SAMPLER = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "exclude_tc_from_top5_errors_by_sampler";

    // Sample Filter
    private static final String REPORT_GENERATOR_KEY_SAMPLE_FILTER = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "sample_filter";

    // report title
    private static final String REPORT_GENERATOR_KEY_REPORT_TITLE = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "report_title";
    
    // start date for which report must be generated
    private static final String REPORT_GENERATOR_KEY_START_DATE = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "start_date";
    
    // end date for which report must be generated
    private static final String REPORT_GENERATOR_KEY_END_DATE = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "end_date";


    // Required graph properties
    // Exclude controllers
    public static final String GRAPH_KEY_EXCLUDE_CONTROLLERS = "exclude_controllers";
    public static final Boolean GRAPH_KEY_EXCLUDE_CONTROLLERS_DEFAULT = Boolean.FALSE;

    // Title
    public static final String GRAPH_KEY_TITLE = "title";
    public static final String GRAPH_KEY_TITLE_DEFAULT = "";

    // Required exporter properties
    // Filters only sample series ?
    public static final String EXPORTER_KEY_FILTERS_ONLY_SAMPLE_SERIES = "filters_only_sample_series";
    public static final Boolean EXPORTER_KEY_FILTERS_ONLY_SAMPLE_SERIES_DEFAULT = Boolean.TRUE;

    // Series filter
    public static final String EXPORTER_KEY_SERIES_FILTER = "series_filter";
    public static final String EXPORTER_KEY_SERIES_FILTER_DEFAULT = "";

    // Show controllers only
    public static final String EXPORTER_KEY_SHOW_CONTROLLERS_ONLY = "show_controllers_only";
    public static final Boolean EXPORTER_KEY_SHOW_CONTROLLERS_ONLY_DEFAULT = Boolean.FALSE;

    // Optional exporter properties
    public static final String EXPORTER_KEY_GRAPH_EXTRA_OPTIONS = "graph_options";

    // Sub configuration keys
    public static final String SUBCONF_KEY_CLASSNAME = "classname";
    public static final String SUBCONF_KEY_PROPERTY = "property";

    private static final class ExporterConfigurationFactory implements
            SubConfigurationFactory<ExporterConfiguration> {
        private final Props props;

        private ExporterConfigurationFactory(Props props) {
            this.props = props;
        }

        @Override
        public ExporterConfiguration createSubConfiguration() {
            return new ExporterConfiguration();
        }

        @Override
        public void initialize(String exportId,
                ExporterConfiguration exportConfiguration)
                throws ConfigurationException {
            log.debug("Load configuration for exporter '{}'", exportId);

            // Get the property defining the class name
            String className = getRequiredProperty(
                    props,
                    getExporterPropertyKey(exportId,
                            SUBCONF_KEY_CLASSNAME), "",
                    String.class);
            log.debug("Using class:'{}' for exporter:'{}'", className, exportId);
            exportConfiguration.setClassName(className);

            // Get the property defining whether only sample series
            // are filtered
            boolean filtersOnlySampleSeries = getRequiredProperty(
                    props,
                    getExporterPropertyKey(exportId,
                            EXPORTER_KEY_FILTERS_ONLY_SAMPLE_SERIES),
                    EXPORTER_KEY_FILTERS_ONLY_SAMPLE_SERIES_DEFAULT,
                    Boolean.class).booleanValue();
            exportConfiguration
                    .filtersOnlySampleSeries(filtersOnlySampleSeries);

            // Get the property defining the series filter
            String seriesFilter = getRequiredProperty(
                    props,
                    getExporterPropertyKey(exportId,
                            EXPORTER_KEY_SERIES_FILTER),
                    EXPORTER_KEY_SERIES_FILTER_DEFAULT,
                    String.class);
            exportConfiguration.setSeriesFilter(seriesFilter);

            // Get the property defining whether only controllers
            // series are shown
            boolean showControllerSeriesOnly = getRequiredProperty(
                    props,
                    getExporterPropertyKey(exportId,
                            EXPORTER_KEY_SHOW_CONTROLLERS_ONLY),
                    EXPORTER_KEY_SHOW_CONTROLLERS_ONLY_DEFAULT,
                    Boolean.class).booleanValue();
            exportConfiguration
                    .showControllerSeriesOnly(showControllerSeriesOnly);

            // Load graph extra properties
            Map<String, SubConfiguration> graphExtraConfigurations = exportConfiguration
                    .getGraphExtraConfigurations();
            loadSubConfiguration(
                    graphExtraConfigurations,
                    props,
                    getSubConfigurationPropertyKey(
                            REPORT_GENERATOR_EXPORTER_KEY_PREFIX,
                            exportId,
                            EXPORTER_KEY_GRAPH_EXTRA_OPTIONS),
                    true,
                    new SubConfigurationFactory<SubConfiguration>() {

                        @Override
                        public SubConfiguration createSubConfiguration() {
                            return new SubConfiguration();
                        }

                        @Override
                        public void initialize(String subConfId,
                                SubConfiguration subConfiguration) {
                            // do nothing
                        }
                    });
        }
    }

    private static final class GraphConfigurationFactory implements
            SubConfigurationFactory<GraphConfiguration> {
        private final Props props;

        private GraphConfigurationFactory(Props props) {
            this.props = props;
        }

        @Override
        public GraphConfiguration createSubConfiguration() {
            return new GraphConfiguration();
        }

        @Override
        public void initialize(String graphId,
                GraphConfiguration graphConfiguration)
                throws ConfigurationException {
            log.debug("Load configuration for graph '{}'", graphId);

            // Get the property defining whether the graph have to
            // filter controller samples
            boolean excludeControllers = getRequiredProperty(
                    props,
                    getGraphPropertyKey(graphId,
                            GRAPH_KEY_EXCLUDE_CONTROLLERS),
                    GRAPH_KEY_EXCLUDE_CONTROLLERS_DEFAULT,
                    Boolean.class).booleanValue();
            graphConfiguration
                    .setExcludeControllers(excludeControllers);

            // Get the property defining the title of the graph
            String title = getRequiredProperty(props,
                    getGraphPropertyKey(graphId, GRAPH_KEY_TITLE),
                    GRAPH_KEY_TITLE_DEFAULT, String.class);
            graphConfiguration.setTitle(title);

            // Get the property defining the class name
            String className = getRequiredProperty(
                    props,
                    getGraphPropertyKey(graphId,
                            SUBCONF_KEY_CLASSNAME), "",
                    String.class);
            log.debug("Using class:'{}' for graph:'{}' with id:'{}'", className, title, graphId);
            graphConfiguration.setClassName(className);

        }
    }

    /**
     * A factory for creating SubConfiguration objects.
     *
     * @param <T>
     *            the generic type
     */
    private interface SubConfigurationFactory<T extends SubConfiguration> {
        T createSubConfiguration();

        void initialize(String subConfId, T subConfiguration)
                throws ConfigurationException;
    }
    private String reportTitle;
    private Date startDate;
    private Date endDate;
    private String sampleFilter;
    private File tempDirectory;
    private long apdexSatisfiedThreshold;
    private long apdexToleratedThreshold;
    private Map<String, Long[]> apdexPerTransaction = new HashMap<>();
    private Pattern filteredSamplesPattern;
    private boolean ignoreTCFromTop5ErrorsBySampler;
    private Map<String, ExporterConfiguration> exportConfigurations = new HashMap<>();
    private Map<String, GraphConfiguration> graphConfigurations = new HashMap<>();

    /**
     * Gets the overall sample filter.
     *
     * @return the overall sample filter
     */
    public final String getSampleFilter() {
        return sampleFilter;
    }

    /**
     * Sets the overall sample filter.
     *
     * @param sampleFilter
     *            the new overall sample filter
     */
    public final void setSampleFilter(String sampleFilter) {
        this.sampleFilter = sampleFilter;
    }

    /**
     * Gets the temporary directory.
     *
     * @return the temporary directory
     */
    public final File getTempDirectory() {
        return tempDirectory;
    }

    /**
     * Sets the temporary directory.
     *
     * @param tempDirectory
     *            the temporary directory to set
     */
    public final void setTempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    /**
     * Gets the apdex satisfied threshold.
     *
     * @return the apdex satisfied threshold
     */
    public final long getApdexSatisfiedThreshold() {
        return apdexSatisfiedThreshold;
    }

    /**
     * Sets the apdex satisfied threshold.
     *
     * @param apdexSatisfiedThreshold
     *            the apdex satisfied threshold to set
     */
    public final void setApdexSatisfiedThreshold(long apdexSatisfiedThreshold) {
        this.apdexSatisfiedThreshold = apdexSatisfiedThreshold;
    }

    /**
     * Gets the apdex tolerated threshold.
     *
     * @return the apdex tolerated threshold
     */
    public final long getApdexToleratedThreshold() {
        return apdexToleratedThreshold;
    }

    /**
     * Sets the apdex tolerated threshold.
     *
     * @param apdexToleratedThreshold
     *            the apdex tolerated threshold to set
     */
    public final void setApdexToleratedThreshold(long apdexToleratedThreshold) {
        this.apdexToleratedThreshold = apdexToleratedThreshold;
    }
    
    /**
     * Gets the apdex per transaction map
     *
     * @return the apdex per transaction map
     */
    public Map<String, Long[]> getApdexPerTransaction() {
        return apdexPerTransaction;
    }

    /**
     * Sets the apdex per transaction map.
     *
     * @param apdexPerTransaction
     *            a map containing thresholds for one or more samples
     */
    public void setApdexPerTransaction(Map<String, Long[]> apdexPerTransaction) {
        this.apdexPerTransaction = apdexPerTransaction;
    }

    /**
     * Gets the export configurations.
     *
     * @return the export configurations
     */
    public final Map<String, ExporterConfiguration> getExportConfigurations() {
        return exportConfigurations;
    }

    /**
     * Gets the graph configurations.
     *
     * @return the graph configurations
     */
    public final Map<String, GraphConfiguration> getGraphConfigurations() {
        return graphConfigurations;
    }

    /**
     * Gets the sub configuration property prefix from the specified key
     * prefix and sub configuration identifier.
     *
     * @param keyPrefix
     *            the key prefix
     * @param subConfId
     *            the sub configuration identifier
     * @return the sub configuration property prefix
     */
    public static String getSubConfigurationPropertyPrefix(String keyPrefix,
            String subConfId) {
        return keyPrefix + KEY_DELIMITER + subConfId;
    }

    /**
     * Gets the sub configuration property key from the specified key
     * prefix, sub configuration identifier and property name.
     *
     * @param keyPrefix
     *            the key prefix
     * @param subConfId
     *            the sub configuration identifier
     * @param propertyName
     *            the property name
     * @return the sub configuration property key
     */
    public static String getSubConfigurationPropertyKey(String keyPrefix,
            String subConfId, String propertyName) {
        return getSubConfigurationPropertyPrefix(keyPrefix, subConfId)
                + KEY_DELIMITER + propertyName;
    }

    /**
     * Gets the exporter property key from the specified identifier and property
     * name.
     *
     * @param exporterId
     *            the exporter identifier
     * @param propertyName
     *            the property name
     * @return the exporter property key
     */
    public static String getExporterPropertyKey(String exporterId,
            String propertyName) {
        return getSubConfigurationPropertyPrefix(
                REPORT_GENERATOR_EXPORTER_KEY_PREFIX, exporterId)
                + KEY_DELIMITER + propertyName;
    }

    /**
     * Gets the graph property key from the specified identifier and property
     * name.
     *
     * @param graphId
     *            the graph identifier
     * @param propertyName
     *            the property name
     * @return the graph property key
     */
    public static String getGraphPropertyKey(String graphId, String propertyName) {
        return getSubConfigurationPropertyPrefix(
                REPORT_GENERATOR_GRAPH_KEY_PREFIX, graphId)
                + KEY_DELIMITER
                + propertyName;
    }

    /**
     * Gets the property matching the specified key in the properties and casts
     * it. Returns a default value is the key is not found.
     *
     * @param <TProperty>
     *            the target type
     * @param props
     *            the properties
     * @param key
     *            the key of the property
     * @param defaultValue
     *            the default value
     * @param clazz
     *            the target class
     * @return the property
     * @throws ConfigurationException
     *             thrown when the property cannot be cast to the specified type
     */
    private static <TProperty> TProperty getProperty(Props props, String key,
            TProperty defaultValue, Class<TProperty> clazz)
            throws ConfigurationException {
        String value = props.getValue(key);
        if (value == null) {
            log.info("Property '{}' not found, using default value '{}' instead.", key, defaultValue);
            return defaultValue;
        }
        return ConfigurationUtils.convert(value, clazz);
    }

    private static <TProperty> TProperty getOptionalProperty(Props props,
            String key, Class<TProperty> clazz) throws ConfigurationException {
        TProperty property = getProperty(props, key, null, clazz);
        if (property != null) {
            log.debug("Use '{}' value for optional property '{}'", property, key);
        }
        return property;
    }

    private static <TProperty> TProperty getRequiredProperty(Props props,
            String key, TProperty defaultValue, Class<TProperty> clazz)
            throws ConfigurationException {
        TProperty property = getProperty(props, key, defaultValue, clazz);
        log.debug("Use '{}' value for required property '{}'", property, key);
        return property;
    }

    /**
     * * Initialize sub configuration items. This function iterates over
     * properties and find each direct sub properties with the specified prefix
     * 
     * <p>
     * E.g. :
     * </p>
     * 
     * <p>
     * With properties :
     * <ul>
     * <li>jmeter.reportgenerator.graph.graph1.title</li>
     * <li>jmeter.reportgenerator.graph.graph1.min_abscissa</li>
     * <li>jmeter.reportgenerator.graph.graph2.title</li>
     * </ul>
     * </p>
     * <p>
     * And prefix : jmeter.reportgenerator.graph
     * </p>
     * 
     * <p>
     * The function creates 2 sub configuration items : graph1 and graph2
     * </p>
     *
     * @param <TSubConf>
     *            the generic type
     * @param subConfigurations
     *            the sub configurations
     * @param props
     *            the props
     * @param propertyPrefix
     *            the property prefix
     * @param factory
     *            the factory
     * @param noPropertyKey
     *            indicates whether extra properties are prefixed with the
     *            SUBCONF_KEY_PROPERTY
     * @throws ConfigurationException
     *             the configuration exception
     */
    private static <TSubConf extends SubConfiguration> void loadSubConfiguration(
            Map<String, TSubConf> subConfigurations, Props props,
            String propertyPrefix, boolean noPropertyKey,
            SubConfigurationFactory<TSubConf> factory)
            throws ConfigurationException {

        for (Map.Entry<String, Object> entry : props.innerMap(propertyPrefix)
                .entrySet()) {
            String key = entry.getKey();
            int index = key.indexOf(KEY_DELIMITER);
            if (index > 0) {
                String name = key.substring(0, index);
                TSubConf subConfiguration = subConfigurations.get(name);
                if (subConfiguration == null) {
                    subConfiguration = factory.createSubConfiguration();
                    subConfigurations.put(name, subConfiguration);
                }
            } else {
                log.warn("Invalid property '{}', skip it.", key);
            }
        }

        // Load sub configurations
        for (Map.Entry<String, TSubConf> entry : subConfigurations.entrySet()) {
            String subConfId = entry.getKey();
            final TSubConf subConfiguration = entry.getValue();

            // Load specific properties
            factory.initialize(subConfId, subConfiguration);

            // Load extra properties
            Map<String, Object> extraKeys = props
                    .innerMap(noPropertyKey ? getSubConfigurationPropertyPrefix(
                            propertyPrefix, subConfId)
                            : getSubConfigurationPropertyKey(propertyPrefix,
                                    subConfId, SUBCONF_KEY_PROPERTY));
            Map<String, String> extraProperties = subConfiguration
                    .getProperties();
            for (Map.Entry<String, Object> entryProperty : extraKeys.entrySet()) {
                extraProperties.put(entryProperty.getKey(),
                        (String) entryProperty.getValue());
            }
        }
    }

    /**
     * Load a configuration from the specified properties.
     *
     * @param properties
     *            the properties
     * @return the report generator configuration
     * @throws ConfigurationException
     *             when mandatory properties are missing
     */
    public static ReportGeneratorConfiguration loadFromProperties(
            Properties properties) throws ConfigurationException {

        log.debug("Report generator properties loading");

        ReportGeneratorConfiguration configuration = new ReportGeneratorConfiguration();

        // Use jodd.Props to ease property handling
        final Props props = new Props();
        log.debug("Loading properties:\r\n{}", properties);
        props.load(properties);

        // Load temporary directory property
        final File tempDirectory = getRequiredProperty(props,
                REPORT_GENERATOR_KEY_TEMP_DIR,
                REPORT_GENERATOR_KEY_TEMP_DIR_DEFAULT, File.class);
        configuration.setTempDirectory(tempDirectory);

        // Load apdex satisfied threshold
        final long apdexSatisfiedThreshold = getRequiredProperty(props,
                REPORT_GENERATOR_KEY_APDEX_SATISFIED_THRESHOLD,
                REPORT_GENERATOR_KEY_APDEX_SATISFIED_THRESHOLD_DEFAULT,
                long.class).longValue();
        configuration.setApdexSatisfiedThreshold(apdexSatisfiedThreshold);

        // Load apdex tolerated threshold
        final long apdexToleratedThreshold = getRequiredProperty(props,
                REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD,
                REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD_DEFAULT,
                long.class).longValue();
        configuration.setApdexToleratedThreshold(apdexToleratedThreshold);
        
        // Load apdex per transactions, overridden by user
        final String apdexPerTransaction = getOptionalProperty(props, 
                REPORT_GENERATOR_KEY_APDEX_PER_TRANSACTION, 
                String.class);
        configuration.setApdexPerTransaction(getApdexPerTransactionParts(apdexPerTransaction));

        final boolean ignoreTCFromTop5ErrorsBySampler = getRequiredProperty(
                props, 
                REPORT_GENERATOR_KEY_EXCLUDE_TC_FROM_TOP5_ERRORS_BY_SAMPLER,
                Boolean.TRUE,
                Boolean.class).booleanValue();
        configuration.setIgnoreTCFromTop5ErrorsBySampler(ignoreTCFromTop5ErrorsBySampler);
        
        // Load sample filter
        final String sampleFilter = getOptionalProperty(props,
                REPORT_GENERATOR_KEY_SAMPLE_FILTER, String.class);
        configuration.setSampleFilter(sampleFilter);

        final String reportTitle = getOptionalProperty(props,
                REPORT_GENERATOR_KEY_REPORT_TITLE, String.class);
        configuration.setReportTitle(reportTitle);

        Date reportStartDate = null;
        Date reportEndDate = null;
        final String startDateValue = getOptionalProperty(props,
                REPORT_GENERATOR_KEY_START_DATE, String.class);
        final String endDateValue = getOptionalProperty(props,
                REPORT_GENERATOR_KEY_END_DATE, String.class);

        String rangeDateFormat = getOptionalProperty(props, REPORT_GENERATOR_KEY_RANGE_DATE_FORMAT, String.class);
        if (StringUtils.isEmpty(rangeDateFormat)) {
            rangeDateFormat = RANGE_DATE_FORMAT_DEFAULT;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(rangeDateFormat, Locale.ENGLISH);

        try {
            if(!StringUtils.isEmpty(startDateValue)) {
                reportStartDate = dateFormat.parse(startDateValue);
                configuration.setStartDate(reportStartDate);
            }
        } catch (ParseException e) {
            log.error("Error parsing property {} with value: {} using format: {}", REPORT_GENERATOR_KEY_START_DATE,
                    startDateValue, rangeDateFormat, e);
        }
        try {
            if(!StringUtils.isEmpty(endDateValue)) {
                reportEndDate = dateFormat.parse(endDateValue);
                configuration.setEndDate(reportEndDate);
            }
        } catch (ParseException e) {
            log.error("Error parsing property {} with value: {} using format: {}", REPORT_GENERATOR_KEY_END_DATE,
                    endDateValue, rangeDateFormat, e);
        }
        
        log.info("Will use date range start date: {}, end date: {}", startDateValue, endDateValue);

        // Find graph identifiers and load a configuration for each
        final Map<String, GraphConfiguration> graphConfigurations = configuration
                .getGraphConfigurations();
        loadSubConfiguration(graphConfigurations, props,
                REPORT_GENERATOR_GRAPH_KEY_PREFIX, false,
                new GraphConfigurationFactory(props));

        if (graphConfigurations.isEmpty()) {
            log.info("No graph configuration found.");
        }

        // Find exporter identifiers and load a configuration for each
        final Map<String, ExporterConfiguration> exportConfigurations = configuration
                .getExportConfigurations();
        loadSubConfiguration(exportConfigurations, props,
                REPORT_GENERATOR_EXPORTER_KEY_PREFIX, false,
                new ExporterConfigurationFactory(props));

        if (exportConfigurations.isEmpty()) {
            log.warn("No export configuration found. No report will be generated.");
        }

        log.debug("End of report generator properties loading");

        return configuration;
    }
    
    /**
     * Parses a string coming from properties to fill a map containing
     * sample names as keys and an array of 2 longs [satisfied, tolerated] as values.
     * The sample name can be a regex supplied by the user.
     * @param apdexPerTransaction the string coming from properties
     * @return {@link Map} containing for each sample name or sample name regex an array of Long corresponding to satisfied and tolerated apdex thresholds.
     */
    public static Map<String, Long[]> getApdexPerTransactionParts(String apdexPerTransaction) {
        Map <String, Long[]> specificApdexes = new HashMap<>();
        if (StringUtils.isEmpty(apdexPerTransaction) || 
                apdexPerTransaction.trim().length()==0) {
            log.info(
                    "apdex_per_transaction : {} is empty, not APDEX per transaction customization");
        } else {
            // data looks like : sample(\d+):1000|2000;samples12:3000|4000;scenar01-12:5000|6000
            String[] parts = apdexPerTransaction.split("[;]");
            for (String chunk : parts) {
                int colonSeparator = chunk.lastIndexOf(':');
                int pipeSeparator = chunk.lastIndexOf('|');
                if (colonSeparator == -1 || pipeSeparator == -1 ||
                        pipeSeparator <= colonSeparator) {
                    log.error(
                        "error parsing property apdex_per_transaction around chunk {}. "
                        + "Wrong format, should have been: 'sample:satisfiedMs|toleratedMS', ignoring", chunk);
                    continue;
                }
                String key = chunk.substring(0, colonSeparator).trim();
                Long satisfied = Long.valueOf(chunk.substring(colonSeparator + 1, pipeSeparator).trim());
                Long tolerated = Long.valueOf(chunk.substring(pipeSeparator + 1).trim());
                specificApdexes.put(key, new Long[] {satisfied, tolerated});
            }
        }
        return specificApdexes;
    }

    /**
     * @return the reportTitle
     */
    public String getReportTitle() {
        return reportTitle;
    }

    /**
     * @param reportTitle the reportTitle to set
     */
    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    /**
     * @return the filteredSamplesPattern
     */
    public Pattern getFilteredSamplesPattern() {
        if(StringUtils.isEmpty(sampleFilter)) {
            return null;
        }
        if(filteredSamplesPattern == null) {
            filteredSamplesPattern = Pattern.compile(sampleFilter);
        }
        return filteredSamplesPattern;
    }

    /**
     * @return the start date to use to generate the report
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the start date to use to generate the report
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the end date to use to generate the report
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the end date to use to generate the report
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the ignoreTCFromTop5ErrorsBySampler
     */
    public boolean isIgnoreTCFromTop5ErrorsBySampler() {
        return ignoreTCFromTop5ErrorsBySampler;
    }

    /**
     * @param ignoreTCFromTop5ErrorsBySampler the ignoreTCFromTop5ErrorsBySampler to set
     */
    public void setIgnoreTCFromTop5ErrorsBySampler(
            boolean ignoreTCFromTop5ErrorsBySampler) {
        this.ignoreTCFromTop5ErrorsBySampler = ignoreTCFromTop5ErrorsBySampler;
    }
}
