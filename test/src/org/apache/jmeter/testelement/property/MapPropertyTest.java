package org.apache.jmeter.testelement.property;

import org.junit.Test;

public class MapPropertyTest {

    @Test
    public void testBug62281MapProperty() {
        MapProperty props = new MapProperty();
        props.addProperty("Foo", new BooleanProperty());
    }

}
