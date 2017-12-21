package org.apache.jmeter.gui.util

import org.apache.jmeter.junit.spock.JMeterSpec

class MenuFactorySpec extends JMeterSpec {

    def "ensure each menu has something in it"() {
        expect:
            MenuFactory.menuMap.size() == 12
            MenuFactory.menuMap.every {!it.value.isEmpty()}
    }

    def "default add menu has expected item count"() {
        expect:
            MenuFactory.createDefaultAddMenu().itemCount == 6 + 3 // items + seperators
    }
}
