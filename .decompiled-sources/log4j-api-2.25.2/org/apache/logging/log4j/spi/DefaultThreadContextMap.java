/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.internal.map.UnmodifiableArrayBackedMap;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.TriConsumer;

public class DefaultThreadContextMap
implements ThreadContextMap,
ReadOnlyStringMap {
    private static final long serialVersionUID = -2635197170958057849L;
    public static final String INHERITABLE_MAP = "isThreadContextMapInheritable";
    private ThreadLocal<Object[]> localState;

    public DefaultThreadContextMap() {
        this(PropertiesUtil.getProperties());
    }

    @Deprecated
    public DefaultThreadContextMap(boolean ignored) {
        this(PropertiesUtil.getProperties());
    }

    DefaultThreadContextMap(PropertiesUtil properties) {
        this.localState = properties.getBooleanProperty(INHERITABLE_MAP) ? new InheritableThreadLocal<Object[]>(){

            @Override
            protected Object[] childValue(Object[] parentValue) {
                return parentValue;
            }
        } : new ThreadLocal();
    }

    @Override
    public void put(String key, String value) {
        Object[] state = this.localState.get();
        this.localState.set(UnmodifiableArrayBackedMap.getMap(state).copyAndPut(key, value).getBackingArray());
    }

    public void putAll(Map<String, String> m) {
        Object[] state = this.localState.get();
        this.localState.set(UnmodifiableArrayBackedMap.getMap(state).copyAndPutAll(m).getBackingArray());
    }

    @Override
    public String get(String key) {
        Object[] state = this.localState.get();
        return state == null ? null : UnmodifiableArrayBackedMap.getMap(state).get(key);
    }

    @Override
    public void remove(String key) {
        Object[] state = this.localState.get();
        if (state != null) {
            this.localState.set(UnmodifiableArrayBackedMap.getMap(state).copyAndRemove(key).getBackingArray());
        }
    }

    public void removeAll(Iterable<String> keys) {
        Object[] state = this.localState.get();
        if (state != null) {
            this.localState.set(UnmodifiableArrayBackedMap.getMap(state).copyAndRemoveAll(keys).getBackingArray());
        }
    }

    @Override
    public void clear() {
        this.localState.remove();
    }

    @Override
    public Map<String, String> toMap() {
        return this.getCopy();
    }

    @Override
    public boolean containsKey(String key) {
        Object[] state = this.localState.get();
        return state != null && UnmodifiableArrayBackedMap.getMap(state).containsKey(key);
    }

    @Override
    public <V> void forEach(BiConsumer<String, ? super V> action) {
        Object[] state = this.localState.get();
        if (state == null) {
            return;
        }
        UnmodifiableArrayBackedMap.getMap(state).forEach(action);
    }

    @Override
    public <V, S> void forEach(TriConsumer<String, ? super V, S> action, S state) {
        Object[] localState = this.localState.get();
        if (localState == null) {
            return;
        }
        UnmodifiableArrayBackedMap.getMap(localState).forEach(action, state);
    }

    @Override
    public <V> V getValue(String key) {
        return (V)this.get(key);
    }

    @Override
    public Map<String, String> getCopy() {
        Object[] state = this.localState.get();
        if (state == null) {
            return new HashMap<String, String>(0);
        }
        return new HashMap<String, String>(UnmodifiableArrayBackedMap.getMap(state));
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        Object[] state = this.localState.get();
        return state == null ? null : UnmodifiableArrayBackedMap.getMap(state);
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public int size() {
        Object[] state = this.localState.get();
        return UnmodifiableArrayBackedMap.getMap(state).size();
    }

    public String toString() {
        Object[] state = this.localState.get();
        return state == null ? "{}" : UnmodifiableArrayBackedMap.getMap(state).toString();
    }

    public int hashCode() {
        return this.toMap().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof ReadOnlyStringMap) {
            if (this.size() != ((ReadOnlyStringMap)((Object)obj)).size()) {
                return false;
            }
            obj = ((ReadOnlyStringMap)((Object)obj)).toMap();
        }
        if (!(obj instanceof ThreadContextMap)) {
            return false;
        }
        ThreadContextMap other = (ThreadContextMap)((Object)obj);
        UnmodifiableArrayBackedMap map = UnmodifiableArrayBackedMap.getMap(this.localState.get());
        Map<String, String> otherMap = other.getImmutableMapOrNull();
        return Objects.equals(map, otherMap);
    }
}

