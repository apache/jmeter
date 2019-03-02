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

package org.apache.jmeter.gui.action.template;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Template Bean
 * @since 2.10 
 */
public class Template {
    private boolean isTestPlan;
    private String name;
    private String fileName;
    private String description;
    private transient File parent; // for relative links
    private Map<String, String> parameters;
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the relativeFileName
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * @param relativeFileName the relativeFileName to set
     */
    public void setFileName(String relativeFileName) {
        fileName = relativeFileName;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isTestPlan() {
        return isTestPlan;
    }
    public void setTestPlan(boolean isTestPlan) {
        this.isTestPlan = isTestPlan;
    }
    public File getParent() {
        return parent;
    }
    public void setParent(File parent) {
        this.parent = parent;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + (isTestPlan ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Template other = (Template) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (isTestPlan != other.isTestPlan) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (!mapsEquals(parameters, other.parameters)) {
            return false;
        }
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }
    
    private boolean mapsEquals(Map<String, String> map1, Map<String, String> map2) {
        if(map1 == null) {
            return map2 == null;
        }else if(map2 == null) {
            return false;
        }
        
        if(map1.size() != map2.size()) {
            return false;
        }
        
        for(Entry<String, String> entry : map1.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(map2.containsKey(key)) {
                if(!map2.get(key).equals(value)) {
                    return false;
                }
            }else {
                return false;
            }
        }
        
        return true;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Template [isTestPlan=");
        builder.append(isTestPlan);
        builder.append(", name=");
        builder.append(name);
        builder.append(", fileName=");
        builder.append(fileName);
        builder.append(", description=");
        builder.append(description);
        builder.append(", parameters=");
        builder.append(parameters);
        builder.append("]");
        return builder.toString();
    }
}
