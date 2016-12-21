package org.apache.jmeter.protocol.jms.sampler.cache;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/** Caching a single value, if key change, cached value is thrown away. <code>null</code> key is supported. **/
class SimpleCache implements Cache, Serializable {
    /** Unique version identifier **/
    private static final long serialVersionUID = 1L;

    private transient Object value;
    private transient Object key;


    @Override
    public void put(Object key, Object value) {
        this.key   = key;
        this.value = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T,R> R get(T key, Function<T,R> get) {
        if (!Objects.equals(this.key, key)) {
            this.value = get.apply(key);
            this.key   = key;
        }
        return (R) this.value;
    }
}
