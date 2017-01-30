package tc.oc.pgm.map;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.jdom2.input.JDOMParseException;
import tc.oc.commons.bukkit.logging.ChatLogRecord;
import tc.oc.pgm.module.ModuleLoadException;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;

public class MapLogRecord extends ChatLogRecord {

    private final MapDefinition map;
    private final String location;

    protected MapLogRecord(MapDefinition map, Level level, @Nullable String location, @Nullable String message, @Nullable Throwable thrown) {
        super(level, message != null ? message : thrown != null ? thrown.getMessage() : null);
        this.map = map;
        if(location == null) {
            if(thrown instanceof InvalidXMLException) {
                location = ((InvalidXMLException) thrown).getFullLocation();
            }

            if(location == null) {
                location = map.getFolder().getRelativeDescriptionFilePath().toString();
            }
        }

        this.location = location;

        if(thrown != null) setThrown(thrown);
        setLoggerName(map.getLogger().getName());
    }

    public MapLogRecord(MapDefinition map, Level level, Node node, @Nullable String message) {
        this(map, level, node.describeWithDocumentAndLocation(), message, null);
    }

    public MapLogRecord(MapDefinition map, ModuleLoadException thrown) {
        this(map, Level.SEVERE, null, null, thrown);
    }

    protected MapLogRecord(MapDefinition map, LogRecord record) {
        this(map, record.getLevel(), null, record.getMessage(), record.getThrown());
    }

    @Override
    public String getLegacyFormattedMessage() {
        String message = ChatColor.AQUA + getLocation() + ": " + ChatColor.RED + getMessage();

        Throwable thrown = getThrown();
        if(thrown != null && thrown.getCause() != null && thrown.getCause().getMessage() != null) {
            message += ", caused by: " + thrown.getCause().getMessage();
        }

        return message;
    }

    @Override
    public boolean suppressStackTrace() {
        // Don't dump the stack if the root cause is just a parse error
        Throwable thrown = getThrown();
        while(thrown instanceof InvalidXMLException) thrown = thrown.getCause();
        return thrown == null ||
               (thrown instanceof JDOMParseException) ||
               super.suppressStackTrace();
    }

    public MapDefinition getMap() {
        return map;
    }

    public String getLocation() {
        return location;
    }
}
