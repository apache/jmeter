package org.apache.jmeter.gui.util

import org.apache.jmeter.junit.categories.NeedGuiTests
import org.apache.jmeter.junit.spock.JMeterSpec
import org.junit.experimental.categories.Category

@Category(NeedGuiTests.class)
class MenuFactorySpec extends JMeterSpec {

    def "ensure each menu has something in it"() {
        expect:
            MenuFactory.menuMap.size() == 11
            MenuFactory.menuMap.values().stream().noneMatch({ it.isEmpty() })
            !MenuFactory.elementsToSkip.isEmpty()
    }
}
