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
 */
package org.apache.jmeter.gui.util

import org.apache.jmeter.junit.spock.JMeterSpec

class MenuFactorySpec extends JMeterSpec {

    def "ensure each menu has something in it"() {
        expect:
            MenuFactory.menuMap.size() == 12
            MenuFactory.menuMap.every { !it.value.isEmpty() }
    }

    def "default add menu has expected item count"() {
        expect:
            MenuFactory.createDefaultAddMenu().itemCount == 6 + 3 // items + separators
    }
}

