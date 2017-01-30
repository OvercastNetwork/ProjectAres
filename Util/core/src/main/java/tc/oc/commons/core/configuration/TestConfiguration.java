package tc.oc.commons.core.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import tc.oc.commons.core.util.Numbers;
import tc.oc.minecraft.api.configuration.AbstractConfigurationSection;
import tc.oc.minecraft.api.configuration.Configuration;
import tc.oc.minecraft.api.configuration.ConfigurationSection;

public class TestConfiguration extends AbstractConfigurationSection implements Configuration {

    private static final TestConfiguration EMPTY = new TestConfiguration();

    private final String path;
    private final Map<String, Object> values;

    @Inject public TestConfiguration() {
        this("", new LinkedHashMap<>());
    }

    public TestConfiguration(String path, Map<String, Object> values) {
        this.path = path;
        this.values = values;
    }

    private String[] split(Object key) {
        return String.valueOf(key).split("\\.");
    }

    private Object getLocal(String key) {
        final Object o = values.get(key);
        return o instanceof Map ? new TestConfiguration(resolvePath(key), (Map<String, Object>) o) : o;
    }

    private Object getPath(String[] path, int i) {
        if(path.length <= i) {
            throw new IllegalArgumentException();
        } else {
            final Object o = getLocal(path[i]);
            if(path.length == i + 1) {
                return o;
            } else if(o instanceof TestConfiguration) {
                return ((TestConfiguration) o).getPath(path, i + 1);
            } else {
                return null;
            }
        }
    }

    @Override
    public void set(String path, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCurrentPath() {
        return path;
    }

    @Override
    public @Nullable ConfigurationSection getSection(String path) {
        final Object o = getPath(split(path), 0);
        if(o instanceof ConfigurationSection) {
            return (ConfigurationSection) o;
        } else {
            return null;
        }
    }

    @Override
    public Collection<String> getKeys() {
        return values.keySet();
    }

    @Override
    public Object get(String path) {
        return getPath(split(path), 0);
    }

    @Override
    public Object get(String path, Object def) {
        final Object v = get(path);
        return v != null ? v : def;
    }

    private <T> T get(String path, Class<T> type, T def) {
        final Object o = get(path);
        return type.isInstance(o) ? type.cast(o) : def;
    }

    private <T> T get(String path, Class<T> type) {
        return get(path, type, null);
    }

    @Override
    public int getInt(String path, int def) {
        return get(path, Integer.class, def);
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return get(path, Boolean.class, def);
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public String getString(String path, String def) {
        return get(path, String.class, def);
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public long getLong(String path, long def) {
        return get(path, Long.class, def);
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0);
    }

    @Override
    public double getDouble(String path, double def) {
        return get(path, Double.class, def);
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return get(path, List.class, def);
    }

    @Override
    public List<?> getList(String path) {
        return getList(path, null);
    }

    @Override
    public List<String> getStringList(String path) {
        final List<?> list = getList(path);
        return list == null ? Collections.emptyList()
                            : Lists.transform(list, Object::toString);
    }

    private <T extends Number> List<T> getNumberList(String path, Class<T> type) {
        final List<?> list = getList(path);
        return list == null ? Collections.emptyList()
                            : Lists.transform(list, n -> Numbers.coerce(n, type, false));
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        final List<?> list = getList(path);
        return list == null ? Collections.emptyList()
                            : Lists.transform(list, b -> b instanceof Boolean && (Boolean) b);
    }

    @Override
    public List<Byte> getByteList(String path) {
        return getNumberList(path, Byte.class);
    }

    @Override
    public List<Short> getShortList(String path) {
        return getNumberList(path, Short.class);
    }

    @Override
    public List<Long> getLongList(String path) {
        return getNumberList(path, Long.class);
    }

    @Override
    public List<Float> getFloatList(String path) {
        return getNumberList(path, Float.class);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        return getNumberList(path, Double.class);
    }

    @Override
    public Configuration getDefaults() {
        return EMPTY;
    }
}
