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

package org.apache.jmeter.junit.categories;

import org.junit.experimental.categories.Category;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * Junit Filter that excludes test annotated with a given marker interface
 * @since 3.0
 */
public class ExcludeCategoryFilter extends Filter {

    private Class<?> excludedClass;
    
    public ExcludeCategoryFilter(Class<?> excludedClass) {
        super();
        this.excludedClass = excludedClass;
    }

    @Override
    public String describe() {
        return "JMeter ExcludeCategoryFilter";
    }

    @Override
    public boolean shouldRun(Description description) {
        //TODO : check the class hierarchy and not only the current class
        Category cat = description.getAnnotation(Category.class);
        if(cat != null) {
            Class<?>[] categories = cat.value();
            for (Class<?> class1 : categories) {
                if(excludedClass.isAssignableFrom(class1)) {
                    return false;
                }
            }            
        }
        
        return true;
    }

}
