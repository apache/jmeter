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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import jodd.props.Props;

/**
 * The class ReportGeneratorConfiguration describes the configuration of the
 * report generator.
 *
 * @since 2.14
 */
public class ReportGeneratorConfiguration {

    private static final Logger log = LoggingManager.getLoggerForClass();

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

    // Apdex Satified Threshold
    private static final String REPORT_GENERATOR_KEY_APDEX_SATISFIED_THRESHOLD = REPORT_GENERATOR_KEY_PREFIX
	    + KEY_DELIMITER + "apdex_statisfied_threshold";
    private static final long REPORT_GENERATOR_KEY_APDEX_SATISFIED_THRESHOLD_DEFAULT = 500L;

    // Apdex Tolerated Threshold
    private static final String REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD = REPORT_GENERATOR_KEY_PREFIX
	    + KEY_DELIMITER + "apdex_tolerated_threshold";
    private static final long REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD_DEFAULT = 1500L;

    // Sample Filter
    private static final String REPORT_GENERATOR_KEY_SAMPLE_FILTER = REPORT_GENERATOR_KEY_PREFIX
	    + KEY_DELIMITER + "sample_filter";

    private static final String LOAD_EXPORTER_FMT = "Load configuration for exporter \"%s\"";
    private static final String LOAD_GRAPH_FMT = "Load configuration for graph \"%s\"";
    private static final String INVALID_KEY_FMT = "Invalid property \"%s\", skip it.";
    private static final String NOT_FOUND_PROPERTY_FMT = "Property \"%s\" not found, using default value \"%s\" instead.";

    // Optional graph properties
    public static final String GRAPH_KEY_ABSCISSA_MIN_KEY = "abscissa_min";
    public static final String GRAPH_KEY_ABSCISSA_MAX_KEY = "abscissa_max";
    public static final String GRAPH_KEY_ORDINATE_MIN_KEY = "ordinate_min";
    public static final String GRAPH_KEY_ORDINATE_MAX_KEY = "ordinate_max";

    // Required graph properties
    // Exclude controllers
    public static final String GRAPH_KEY_EXCLUDE_CONTROLLERS = "exclude_controllers";
    public static final boolean GRAPH_KEY_EXCLUDE_CONTROLLERS_DEFAULT = false;

    // Title
    public static final String GRAPH_KEY_TITLE = "title";
    public static final String GRAPH_KEY_TITLE_DEFAULT = "Generic graph title";

    // Sub configuration keys
    public static final String SUBCONF_KEY_CLASSNAME = "classname";
    public static final String SUBCONF_KEY_PROPERTY = "property";

    private static final String START_LOADING_MSG = "Report generator properties loading";
    private static final String END_LOADING_MSG = "End of report generator properties loading";
    private static final String REQUIRED_PROPERTY_FMT = "Use \"%s\" value for required property \"%s\"";
    private static final String OPTIONAL_PROPERTY_FMT = "Use \"%s\" value for optional property \"%s\"";

    /**
     * A factory for creating SubConfiguration objects.
     *
     * @param <TSubConfiguration>
     *            the generic type
     */
    private interface SubConfigurationFactory<TSubConfiguration> {
	void createSubConfiguration(String name);
    }

    private String sampleFilter;
    private File tempDirectory;
    private long apdexSatisfiedThreshold;
    private long apdexToleratedThreshold;
    private ArrayList<String> filteredSamples = new ArrayList<String>();
    private HashMap<String, SubConfiguration> exportConfigurations = new HashMap<String, SubConfiguration>();
    private HashMap<String, GraphConfiguration> graphConfigurations = new HashMap<String, GraphConfiguration>();

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
	if (Objects.equals(this.sampleFilter, sampleFilter) == false) {
	    this.sampleFilter = sampleFilter;
	    filteredSamples.clear();
	    if (sampleFilter != null) {
		String[] items = sampleFilter.split(",");
		int count = items.length;
		for (int index = 0; index < count; index++) {
		    filteredSamples.add(items[index].trim());
		}
	    }
	}
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
     * Gets the filtered samples.
     *
     * @return the filteredSamples
     */
    public final List<String> getFilteredSamples() {
	return filteredSamples;
    }

    /**
     * Gets the export configurations.
     *
     * @return the export configurations
     */
    public final Map<String, SubConfiguration> getExportConfigurations() {
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
     * Gets the exporter property prefix from the specified exporter identifier.
     *
     * @param exporterId
     *            the exporter identifier
     * @return the exporter property prefix
     */
    public static String getExporterPropertyPrefix(String exporterId) {
	return REPORT_GENERATOR_EXPORTER_KEY_PREFIX + KEY_DELIMITER
	        + exporterId;
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
	return getExporterPropertyPrefix(exporterId) + KEY_DELIMITER
	        + propertyName;
    }

    /**
     * Gets the graph property prefix from the specified graph identifier.
     *
     * @param graphId
     *            the graph identifier
     * @return the graph property prefix
     */
    public static String getGraphPropertyPrefix(String graphId) {
	return REPORT_GENERATOR_GRAPH_KEY_PREFIX + KEY_DELIMITER + graphId;
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
	return getGraphPropertyPrefix(graphId) + KEY_DELIMITER + propertyName;
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
	TProperty property = null;
	String value = props.getValue(key);
	if (value == null) {
	    if (defaultValue != null) {
		property = defaultValue;
		log.info(String.format(NOT_FOUND_PROPERTY_FMT, key,
		        defaultValue));
	    }
	} else {
	    property = ConfigurationUtils.convert(value, clazz);
	}
	return property;
    }

    private static <TProperty> TProperty getOptionalProperty(Props props,
	    String key, Class<TProperty> clazz) throws ConfigurationException {
	TProperty property = getProperty(props, key, null, clazz);
	if (property != null) {
	    log.debug(String.format(OPTIONAL_PROPERTY_FMT, property, key));
	}
	return property;
    }

    private static <TProperty> TProperty getRequiredProperty(Props props,
	    String key, TProperty defaultValue, Class<TProperty> clazz)
	    throws ConfigurationException {
	TProperty property = getProperty(props, key, defaultValue, clazz);
	log.debug(String.format(REQUIRED_PROPERTY_FMT, property, key));
	return property;
    }

    /**
     * Initialize sub configuration items. This function iterates over
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
     *            the type of the sub configuration item
     * @param props
     *            the properties
     * @param propertyPrefix
     *            the property prefix
     * @param factory
     *            the sub configuration item factory
     * @param invalidKeyFormat
     *            the invalid key format
     */
    private static <TSubConf> void initializeSubConfiguration(Props props,
	    String propertyPrefix, SubConfigurationFactory<TSubConf> factory) {
	for (Map.Entry<String, Object> entry : props.innerMap(propertyPrefix)
	        .entrySet()) {
	    String key = entry.getKey();
	    int index = key.indexOf(KEY_DELIMITER);
	    if (index > 0) {
		factory.createSubConfiguration(key.substring(0, index));
	    } else {
		log.warn(String.format(INVALID_KEY_FMT, key));
	    }
	}
    }

    /**
     * Load a configuration from the specified properties.
     *
     * @param properties
     *            the properties
     * @return the report generator configuration
     */
    public static ReportGeneratorConfiguration LoadFromProperties(
	    Properties properties) throws ConfigurationException {

	log.debug(START_LOADING_MSG);

	ReportGeneratorConfiguration configuration = new ReportGeneratorConfiguration();

	// Use jodd.Props to ease property handling
	Props props = new Props();
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
	        long.class);
	configuration.setApdexSatisfiedThreshold(apdexSatisfiedThreshold);

	// Load apdex tolerated threshold
	final long apdexToleratedThreshold = getRequiredProperty(props,
	        REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD,
	        REPORT_GENERATOR_KEY_APDEX_TOLERATED_THRESHOLD_DEFAULT,
	        long.class);
	configuration.setApdexToleratedThreshold(apdexToleratedThreshold);

	// Load sample filter
	final String sampleFilter = getOptionalProperty(props,
	        REPORT_GENERATOR_KEY_SAMPLE_FILTER, String.class);
	configuration.setSampleFilter(sampleFilter);

	// Find graph identifiers and create a configuration for each
	final Map<String, GraphConfiguration> graphConfigurations = configuration
	        .getGraphConfigurations();
	initializeSubConfiguration(props, REPORT_GENERATOR_GRAPH_KEY_PREFIX,
	        new SubConfigurationFactory<GraphConfiguration>() {

		    @Override
		    public void createSubConfiguration(String name) {
		        GraphConfiguration graphConfiguration = graphConfigurations
		                .get(name);
		        if (graphConfiguration == null) {
			    graphConfiguration = new GraphConfiguration();
			    graphConfigurations.put(name, graphConfiguration);
		        }

		    }
	        });

	// Load graph configuration
	for (Map.Entry<String, GraphConfiguration> entry : graphConfigurations
	        .entrySet()) {
	    String graphId = entry.getKey();
	    final GraphConfiguration graphConfiguration = entry.getValue();

	    log.debug(String.format(LOAD_GRAPH_FMT, graphId));

	    // Get the property defining the minimum abscissa
	    Double minAbscissa = getOptionalProperty(props,
		    getGraphPropertyKey(graphId, GRAPH_KEY_ABSCISSA_MIN_KEY),
		    Double.class);
	    if (minAbscissa != null)
		graphConfiguration.setAbscissaMin(minAbscissa);

	    // Get the property defining the maximum abscissa
	    Double maxAbscissa = getOptionalProperty(props,
		    getGraphPropertyKey(graphId, GRAPH_KEY_ABSCISSA_MAX_KEY),
		    Double.class);
	    if (maxAbscissa != null)
		graphConfiguration.setAbscissaMax(maxAbscissa);

	    // Get the property defining the minimum ordinate
	    Double minOrdinate = getOptionalProperty(props,
		    getGraphPropertyKey(graphId, GRAPH_KEY_ORDINATE_MIN_KEY),
		    Double.class);
	    if (minOrdinate != null)
		graphConfiguration.setOrdinateMin(minOrdinate);

	    // Get the property defining the maximum ordinate
	    Double maxOrdinate = getOptionalProperty(props,
		    getGraphPropertyKey(graphId, GRAPH_KEY_ORDINATE_MAX_KEY),
		    Double.class);
	    if (maxOrdinate != null)
		graphConfiguration.setOrdinateMax(maxOrdinate);

	    // Get the property defining whether the graph have to filter
	    // controller
	    // samples
	    boolean excludeControllers = getRequiredProperty(
		    props,
		    getGraphPropertyKey(graphId, GRAPH_KEY_EXCLUDE_CONTROLLERS),
		    GRAPH_KEY_EXCLUDE_CONTROLLERS_DEFAULT, Boolean.class);
	    graphConfiguration.setExcludeControllers(excludeControllers);

	    // Get the property defining the title of the graph
	    String title = getRequiredProperty(props,
		    getGraphPropertyKey(graphId, GRAPH_KEY_TITLE),
		    GRAPH_KEY_TITLE_DEFAULT, String.class);
	    graphConfiguration.setTitle(title);

	    // Get the property defining the class name
	    String className = getRequiredProperty(props,
		    getGraphPropertyKey(graphId, SUBCONF_KEY_CLASSNAME), "",
		    String.class);
	    graphConfiguration.setClassName(className);

	    // Load graph properties
	    Map<String, Object> graphKeys = props.innerMap(getGraphPropertyKey(
		    graphId, SUBCONF_KEY_PROPERTY));
	    Map<String, String> graphProperties = graphConfiguration
		    .getProperties();
	    for (Map.Entry<String, Object> entryProperty : graphKeys.entrySet()) {
		graphProperties.put(entryProperty.getKey(),
		        (String) entryProperty.getValue());
	    }
	}

	// Find exporter identifiers and create a configuration for each
	final Map<String, SubConfiguration> exportConfigurations = configuration
	        .getExportConfigurations();
	initializeSubConfiguration(props, REPORT_GENERATOR_EXPORTER_KEY_PREFIX,
	        new SubConfigurationFactory<SubConfiguration>() {

		    @Override
		    public void createSubConfiguration(String name) {
		        SubConfiguration exportConfiguration = exportConfigurations
		                .get(name);
		        if (exportConfiguration == null) {
			    exportConfiguration = new SubConfiguration();
			    exportConfigurations.put(name, exportConfiguration);
		        }

		    }
	        });

	// Load export configuration
	for (Map.Entry<String, SubConfiguration> entry : exportConfigurations
	        .entrySet()) {
	    String exportId = entry.getKey();
	    final SubConfiguration exportConfiguration = entry.getValue();

	    log.debug(String.format(LOAD_EXPORTER_FMT, exportId));

	    // Get the property defining the class name
	    String className = getRequiredProperty(props,
		    getExporterPropertyKey(exportId, SUBCONF_KEY_CLASSNAME),
		    "", String.class);
	    exportConfiguration.setClassName(className);

	    // Load exporter properties
	    Map<String, Object> exporterKeys = props
		    .innerMap(getExporterPropertyKey(exportId,
		            SUBCONF_KEY_PROPERTY));
	    Map<String, String> graphProperties = exportConfiguration
		    .getProperties();
	    for (Map.Entry<String, Object> entryProperty : exporterKeys
		    .entrySet()) {
		graphProperties.put(entryProperty.getKey(),
		        (String) entryProperty.getValue());
	    }
	}

	log.debug(END_LOADING_MSG);

	return configuration;
    }
}
