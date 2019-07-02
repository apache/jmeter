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

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.config.SubConfiguration;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ResultData;
import org.apache.jmeter.report.processor.ValueResultData;

/**
 * The Class AbstractDataExporter provides a base class for DataExporter.
 */
public abstract class AbstractDataExporter implements DataExporter {
    private static final String INVALID_PROPERTY_CONFIG_FMT = "Wrong property \"%s\" in \"%s\" export configuration";

    private String name;

    /**
     * Instantiates a new abstract data exporter.
     */
    protected AbstractDataExporter() {
    }

    /**
     * Finds a value matching the specified data name in a ResultData tree.
     * Supports only MapResultData walking.
     *
     * @param clazz
     *            the type of the value
     * @param data
     *            the name of the data containing the value
     * @param root
     *            the root of the tree
     * @param <T>
     *            type of value to be found
     * @return the value matching the data name
     */
    protected static <T> T findValue(Class<T> clazz, String data,
            ResultData root) {
        T value = null;
        ResultData result = findData(data, root);
        if (result instanceof ValueResultData) {
            ValueResultData valueResult = (ValueResultData) result;
            Object object = valueResult.getValue();
            if (object != null && clazz.isAssignableFrom(object.getClass())) {
                value = clazz.cast(object);
            }
        }
        return value;
    }

    /**
     * Finds a inner ResultData matching the specified data name in a ResultData
     * tree. Supports only MapResultData walking.
     *
     * @param data
     *            the name of the data containing the value
     * @param root
     *            the root of the tree
     * @return the ResultData matching the data name
     */
    protected static ResultData findData(String data, ResultData root) {
        ResultData result = null;
        String[] pathItems = StringUtils.split(data, '.');
        if (pathItems != null) {
            if (root instanceof MapResultData) {
                int count = pathItems.length;
                int index = 0;
                MapResultData map = (MapResultData) root;
                while (index < count && result == null) {
                    ResultData current = map.getResult(pathItems[index]);
                    if (index == count - 1) {
                        result = current;
                    } else {
                        if (current instanceof MapResultData) {
                            map = (MapResultData) current;
                            index++;
                        }
                    }
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.report.dashboard.DataExporter#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.jmeter.report.dashboard.DataExporter#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    protected <TProperty> TProperty getPropertyFromConfig(SubConfiguration cfg,
            String property, TProperty defaultValue, Class<TProperty> clazz)
                    throws ExportException {
        try {
            return cfg.getProperty(property, defaultValue, clazz);
        } catch (ConfigurationException ex) {
            throw new ExportException(String.format(INVALID_PROPERTY_CONFIG_FMT,
                    property, getName()), ex);
        }
    }
}
