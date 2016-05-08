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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import jodd.props.Props;

import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The class ReportGeneratorConfiguration describes the configuration of the
 * report generator.
 *
 * @since 3.0
 */
public class ReportGeneratorConfiguration {

    private static final Logger LOG = LoggingManager.getLoggerForClass();

    public static final char KEY_DELIMITER = '.';
    public static final String REPORT_GENERATOR_KEY_PREFIX = "jmeter.reportgenerator";
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

    // Sample Filter
    private static final String REPORT_GENERATOR_KEY_SAMPLE_FILTER = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "sample_filter";

    // report title
    private static final String REPORT_GENERATOR_KEY_REPORT_TITLE = REPORT_GENERATOR_KEY_PREFIX
            + KEY_DELIMITER + "report_title";

    private static final String LOAD_EXPORTER_FMT = "Load configuration for exporter \"%s\"";
    private static final String LOAD_GRAPH_FMT = "Load configuration for graph \"%s\"";
    private static final String INVALID_KEY_FMT = "Invalid property \"%s\", skip it.";
    private static final String NOT_FOUND_PROPERTY_FMT = "Property \"%s\" not found, using default value \"%s\" instead.";

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

    private static final String START_LOADING_MSG = "Report generator properties loading";
    private static final String END_LOADING_MSG = "End of report generator properties loading";
    private static final String REQUIRED_PROPERTY_FMT = "Use \"%s\" value for required property \"%s\"";
    private static final String OPTIONAL_PROPERTY_FMT = "Use \"%s\" value for optional property \"%s\"";

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
            LOG.debug(String.format(LOAD_EXPORTER_FMT, exportId));

            // Get the property defining the class name
            String className = getRequiredProperty(
                    props,
                    getExporterPropertyKey(exportId,
                            SUBCONF_KEY_CLASSNAME), "",
                    String.class);
            if(LOG.isDebugEnabled()) {
                LOG.debug("Using class:'"+className+"'"+" for exporter:'"+exportId+"'");
            }
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
            LOG.debug(String.format(LOAD_GRAPH_FMT, graphId));

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
            if(LOG.isDebugEnabled()) {
                LOG.debug("Using class:'"+className+"' for graph:'"+title+"' with id:'"+graphId+"'");
            }
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
    private String sampleFilter;
    private File tempDirectory;
    private long apdexSatisfiedThreshold;
    private long apdexToleratedThreshold;
    private Pattern filteredSamplesPattern;
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
            LOG.info(String.format(NOT_FOUND_PROPERTY_FMT, key,
                    defaultValue));
            return defaultValue;
        }
        return ConfigurationUtils.convert(value, clazz);
    }

    private static <TProperty> TProperty getOptionalProperty(Props props,
            String key, Class<TProperty> clazz) throws ConfigurationException {
        TProperty property = getProperty(props, key, null, clazz);
        if (property != null) {
            LOG.debug(String.format(OPTIONAL_PROPERTY_FMT, property, key));
        }
        return property;
    }

    private static <TProperty> TProperty getRequiredProperty(Props props,
            String key, TProperty defaultValue, Class<TProperty> clazz)
            throws ConfigurationException {
        TProperty property = getProperty(props, key, defaultValue, clazz);
        LOG.debug(String.format(REQUIRED_PROPERTY_FMT, property, key));
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
                LOG.warn(String.format(INVALID_KEY_FMT, key));
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

        LOG.debug(START_LOADING_MSG);

        ReportGeneratorConfiguration configuration = new ReportGeneratorConfiguration();

        // Use jodd.Props to ease property handling
        final Props props = new Props();
        if(LOG.isDebugEnabled()) {
            LOG.debug("Loading properties:\r\n"+properties);
        }
        props.load(properties);

        // Load temporary directory property
        final File tempDirectory = getRequiredProperty(props,
                REPORT_GENERATOR_KEY_TEMP_DIR,
                REPORT_GENERATOR_KEY_TEMP_DIR_DEFAULT, File.class);
        configuration.setTempDirectory(tempDirectory);

        // Load apdex statified threshold
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

        // Load sample filter
        final String sampleFilter = getOptionalProperty(props,
                REPORT_GENERATOR_KEY_SAMPLE_FILTER, String.class);
        configuration.setSampleFilter(sampleFilter);

        final String reportTitle = getOptionalProperty(props,
                REPORT_GENERATOR_KEY_REPORT_TITLE, String.class);
        configuration.setReportTitle(reportTitle);

        // Find graph identifiers and load a configuration for each
        final Map<String, GraphConfiguration> graphConfigurations = configuration
                .getGraphConfigurations();
        loadSubConfiguration(graphConfigurations, props,
                REPORT_GENERATOR_GRAPH_KEY_PREFIX, false,
                new GraphConfigurationFactory(props));

        if (graphConfigurations.isEmpty()) {
            LOG.info("No graph configuration found.");
        }

        // Find exporter identifiers and load a configuration for each
        final Map<String, ExporterConfiguration> exportConfigurations = configuration
                .getExportConfigurations();
        loadSubConfiguration(exportConfigurations, props,
                REPORT_GENERATOR_EXPORTER_KEY_PREFIX, false,
                new ExporterConfigurationFactory(props));

        if (exportConfigurations.isEmpty()) {
            LOG.warn("No export configuration found. No report will be generated.");
        }

        LOG.debug(END_LOADING_MSG);

        return configuration;
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
}
