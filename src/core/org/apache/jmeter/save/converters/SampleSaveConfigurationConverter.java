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

package org.apache.jmeter.save.converters;

import org.apache.jmeter.samplers.SampleSaveConfiguration;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/*
 * Allow new fields to be added to the SampleSaveConfiguration without
 * changing the output JMX file unless it is necessary.
 *
 * TODO work out how to make shouldSerializeMember() conditionally return true.
 */
public class SampleSaveConfigurationConverter  extends ReflectionConverter {

    private static final ReflectionProvider rp;

    static {
        ReflectionProvider tmp;
        try {
            tmp = JVM.newReflectionProvider();
        } catch (NullPointerException e) {// Bug in above method
            tmp = new PureJavaReflectionProvider();
        }
        rp = tmp;
    }

    private static final String TRUE = "true"; // $NON-NLS-1$

    // N.B. These must agree with the new member names in SampleSaveConfiguration
    private static final String NODE_FILENAME = "fileName"; // $NON-NLS-1$
    private static final String NODE_HOSTNAME = "hostname"; // $NON-NLS-1$
    private static final String NODE_URL = "url"; // $NON-NLS-1$
    private static final String NODE_BYTES = "bytes"; // $NON-NLS-1$
    private static final String NODE_SENT_BYTES = "sentBytes"; // $NON-NLS-1$
    private static final String NODE_THREAD_COUNT = "threadCounts"; // $NON-NLS-1$
    private static final String NODE_SAMPLE_COUNT = "sampleCount"; // $NON-NLS-1$
    private static final String NODE_IDLE_TIME = "idleTime"; // $NON-NLS-1$
    private static final String NODE_CONNECT_TIME = "connectTime"; // $NON-NLS-1$

    // Additional member names which are currently not written out
    private static final String NODE_DELIMITER = "delimiter"; // $NON-NLS-1$
    private static final String NODE_PRINTMS = "printMilliseconds"; // $NON-NLS-1$


    static class MyWrapper extends MapperWrapper {

        public MyWrapper(Mapper wrapped) {
            super(wrapped);
        }

        /** {@inheritDoc} */
        @Override
        public boolean shouldSerializeMember(
                @SuppressWarnings("rawtypes") // superclass does not use types
                Class definedIn, String fieldName) {
            if (SampleSaveConfiguration.class != definedIn) {
                return true;
            }
            // These are new fields; not saved unless true
            // This list MUST agree with the list in the marshall() method below
            switch (fieldName) {
                case NODE_BYTES:
                case NODE_SENT_BYTES:
                case NODE_URL:
                case NODE_FILENAME:
                case NODE_HOSTNAME:
                case NODE_THREAD_COUNT:
                case NODE_SAMPLE_COUNT:
                case NODE_IDLE_TIME:
                case NODE_CONNECT_TIME:
                // The two fields below are not currently saved or restored
                case NODE_DELIMITER:
                case NODE_PRINTMS:
                    return false;
                default:
                    return true;
            }
        }
    }

    public SampleSaveConfigurationConverter(Mapper arg0) {
        super(new MyWrapper(arg0),rp);
    }

    /**
     * Returns the converter version; used to check for possible
     * incompatibilities
     *
     * @return the version of this converter
     */
    public static String getVersion() {
        return "$Revision$"; // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) {
        return SampleSaveConfiguration.class.equals(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(obj, writer, context); // Save most things

        SampleSaveConfiguration prop = (SampleSaveConfiguration) obj;

        // Save the new fields - but only if they are true
        // This list MUST agree with the list in MyWrapper#shouldSerializeMember()
        createNode(writer,prop.saveBytes(),NODE_BYTES);
        createNode(writer,prop.saveSentBytes(),NODE_SENT_BYTES);
        createNode(writer,prop.saveUrl(),NODE_URL);
        createNode(writer,prop.saveFileName(),NODE_FILENAME);
        createNode(writer,prop.saveHostname(),NODE_HOSTNAME);
        createNode(writer,prop.saveThreadCounts(),NODE_THREAD_COUNT);
        createNode(writer,prop.saveSampleCount(),NODE_SAMPLE_COUNT);
        createNode(writer,prop.saveIdleTime(),NODE_IDLE_TIME);
        createNode(writer, prop.saveConnectTime(), NODE_CONNECT_TIME);
    }

    // Helper method to simplify marshall routine. Save if and only if true.
    private void createNode(HierarchicalStreamWriter writer, boolean save, String node) {
        if (!save) {
            return;
        }
        writer.startNode(node);
        writer.setValue(TRUE);
        writer.endNode();
    }

    /** {@inheritDoc} */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final Class<SampleSaveConfiguration> thisClass = SampleSaveConfiguration.class;
        final Class<?> requiredType = context.getRequiredType();
        if (requiredType != thisClass) {
            throw new IllegalArgumentException("Unexpected class: "+requiredType.getName());
        }
        // The default for missing tags is false, so preset all the fields accordingly
        SampleSaveConfiguration result = new SampleSaveConfiguration(false);
        // Now pick up any tags from the input file
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nn = reader.getNodeName();
            if (!"formatter".equals(nn)){// Skip formatter (if present) bug 42674 $NON-NLS-1$
                String fieldName = mapper.realMember(thisClass, nn);
                java.lang.reflect.Field field = reflectionProvider.getField(thisClass,fieldName);
                Class<?> type = field.getType();
                Object value = unmarshallField(context, result, type, field);
                reflectionProvider.writeField(result, nn, value, thisClass);
            }
            reader.moveUp();
        }
        return result;
    }
}
