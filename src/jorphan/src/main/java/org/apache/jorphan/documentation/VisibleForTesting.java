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

package org.apache.jorphan.documentation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes that the class, method or field has its visibility relaxed so
 * that unit tests can access it.
 * This means that this API is not-public.
 * The <code>visibility</code> argument can be used to specific what the original
 * visibility should have been if it had not been made public or package-private for testing.
 * The default is to consider the element private.
 *
 * Copy of this class under ASL license:
 * https://github.com/aosp-mirror/platform_frameworks_base/blob/master/core/java/com/android/internal/annotations/VisibleForTesting.java
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface VisibleForTesting {
    /**
     * Intended visibility if the element had not been made public or package-private for
     * testing.
     */
    enum Visibility {
        /** The element should be considered protected. */
        PROTECTED,
        /** The element should be considered package-private. */
        PACKAGE,
        /** The element should be considered private. */
        PRIVATE
    }

    /**
     * If no visibility specified, one should assume the element originally intended to be private.
     * @return {@link Visibility} Intended visibility if the element had not been made public or package-private for testing.
     */
    Visibility visibility() default Visibility.PRIVATE;
}
