package tc.oc.pgm.development;

import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.pgm.map.MapDefinition;
import tc.oc.pgm.map.MapLogRecord;

/**
 * Log handler that collects map-related errors and indexes them by map
 */
@Singleton
public class MapErrorTracker extends Handler implements PluginFacet {

    private final MapdevLogger mapdevLogger;

    private Multimap<MapDefinition, MapLogRecord> errors = ArrayListMultimap.create();

    @Inject MapErrorTracker(MapdevLogger mapdevLogger) {
        this.mapdevLogger = mapdevLogger;
        this.setLevel(Level.WARNING);
    }

    @Override
    public void enable() {
        mapdevLogger.addHandler(this);
    }

    public Multimap<MapDefinition, MapLogRecord> getErrors() {
        return errors;
    }

    public void clearAllErrors() {
        errors.clear();
    }

    public void clearErrors(MapDefinition map) {
        errors.removeAll(map);
    }

    /**
     * Clear errors for any maps NOT in the given set
     */
    public void clearErrorsExcept(Collection<? extends MapDefinition> excepted) {
        errors.keySet().retainAll(excepted);
    }

    @Override
    public void publish(LogRecord record) {
        if(record instanceof MapLogRecord && isLoggable(record)) {
            MapLogRecord mapRecord = (MapLogRecord) record;
            errors.put(mapRecord.getMap(), mapRecord);
        }
    }

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }
}
