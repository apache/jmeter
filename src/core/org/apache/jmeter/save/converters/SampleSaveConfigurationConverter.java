/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/*
 * Allow new fields to be added to the SampleSaveConfiguration without
 * changing the output JMX file unless it is necessary. 
 * 
 * TODO work out how to make shouldSerializeMember() conditionally return true.
 */
public class SampleSaveConfigurationConverter  extends ReflectionConverter {

	private static final ReflectionProvider rp = new JVM().bestReflectionProvider();

    // N.B. These must agree with the names in SampleSaveConfiguration
    private static final String TRUE = "true"; // $NON-NLS-1$
	private static final String NODE_FILENAME = "fileName"; // $NON-NLS-1$
	private static final String NODE_URL = "url"; // $NON-NLS-1$
	private static final String NODE_BYTES = "bytes"; // $NON-NLS-1$
    private static final String NODE_THREAD_COUNT = "threadCounts"; // $NON-NLS-1$

	static class MyWrapper extends MapperWrapper{

        public MyWrapper(ClassMapper wrapped) {
            super(wrapped);
        }
        
        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            if (SampleSaveConfiguration.class != definedIn) return true;
            if (fieldName.equals(NODE_BYTES)) return false; 
            if (fieldName.equals(NODE_URL)) return false; 
            if (fieldName.equals(NODE_FILENAME)) return false; 
            if (fieldName.equals(NODE_THREAD_COUNT)) return false; 
            return true;
        }
    }
	public SampleSaveConfigurationConverter(ClassMapper arg0) {
        super(new MyWrapper(arg0),rp);
    }

    /**
	 * Returns the converter version; used to check for possible
	 * incompatibilities
	 */
	public static String getVersion() {
		return "$Revision$"; // $NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
	 */
	public boolean canConvert(Class arg0) {
		return arg0.equals(SampleSaveConfiguration.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object,
	 *      com.thoughtworks.xstream.io.HierarchicalStreamWriter,
	 *      com.thoughtworks.xstream.converters.MarshallingContext)
	 */
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(obj, writer, context); // Save most things
        
        SampleSaveConfiguration prop = (SampleSaveConfiguration) obj;
        
        // Save the new fields - but only if they are not the default
        createNode(writer,prop.saveBytes(),NODE_BYTES);
        createNode(writer,prop.saveUrl(),NODE_URL);
        createNode(writer,prop.saveFileName(),NODE_FILENAME);
        createNode(writer,prop.saveThreadCounts(),NODE_THREAD_COUNT);
	}

    // Helper method to simplify marshall routine
    private void createNode(HierarchicalStreamWriter writer, boolean save, String node) {
        if (!save) return;
        writer.startNode(node);
        writer.setValue(TRUE);
        writer.endNode();
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader,
	 *      com.thoughtworks.xstream.converters.UnmarshallingContext)
	 */
//	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext arg1) {
//        SampleSaveConfiguration prop = new SampleSaveConfiguration();
//		return prop;
//	}
}
