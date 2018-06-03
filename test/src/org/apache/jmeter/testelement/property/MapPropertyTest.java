package org.apache.jmeter.testelement.property;

import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class MapPropertyTest {

    @Test
    public void testBug62281MapProperty() {
        MapProperty props = new MapProperty();
        props.addProperty("Foo", new BooleanProperty());
    }

    @Test
    public void testGetPropertyTypeOfEmptyMap() {
        MapProperty props = new MapProperty("foo", new HashMap<Object, Object>());
        assertThat(props.getPropertyType(), CoreMatchers.equalTo(NullProperty.class));
    }
    
    public void testGetPropertyTypeOfStringElements() {
        Map<String, Integer> numberMap = new HashMap<>();
        numberMap.put("One", Integer.valueOf(1));
        MapProperty props = new MapProperty("foo", numberMap);
        assertThat(props.getPropertyType(), CoreMatchers.equalTo(IntegerProperty.class));
    }
}
