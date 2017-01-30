package tc.oc.pgm.map;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import tc.oc.commons.bukkit.logging.MapdevLogger;
import tc.oc.pgm.xml.Node;

public class MapLogger extends Logger {

    public interface Factory {
        MapLogger create(MapDefinition map);
    }

    private final MapDefinition map;

    @Inject MapLogger(@Assisted MapDefinition map, MapdevLogger mapdevLogger) {
        super(mapdevLogger.getName() + "." + map.getDottedPath(), null);
        this.map = map;
        setParent(mapdevLogger);
        setUseParentHandlers(true);
    }

    @Override
    public void log(LogRecord record) {
        super.log(record instanceof MapLogRecord ? record
                                                 : new MapLogRecord(map, record));
    }

    public void log(Level level, Node node, String message) {
        log(new MapLogRecord(map, level, node, message));
    }

    public void fine(Node node, String message)     { log(Level.FINE, node, message); }
    public void info(Node node, String message)     { log(Level.INFO, node, message); }
    public void warning(Node node, String message)  { log(Level.WARNING, node, message); }
    public void severe(Node node, String message)   { log(Level.SEVERE, node, message); }
}
