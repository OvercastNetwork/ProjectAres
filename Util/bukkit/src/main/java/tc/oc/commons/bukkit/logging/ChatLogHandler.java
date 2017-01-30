package tc.oc.commons.bukkit.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.bukkit.Bukkit;

/**
 * A log {@link Handler} that shows log messages to all players
 * with a given permission, and the console, if it also has that
 * permission.
 *
 * Can also generate colored messages from {@link ChatLogRecord}s,
 * and suppress stack traces for types of errors that don't need them.
 */
public class ChatLogHandler extends Handler {

    protected final String permission;

    public ChatLogHandler(String permission) {
        this.permission = permission;
    }

    protected String formatMessage(LogRecord record) {
        return record instanceof ChatLogRecord ? ((ChatLogRecord) record).getLegacyFormattedMessage()
                                               : record.getMessage();
    }

    @Override
    public void publish(LogRecord record) {
        Bukkit.broadcast(formatMessage(record), permission);

        if(Bukkit.getConsoleSender().hasPermission(permission) &&
           record.getThrown() != null &&
           !(record instanceof ChatLogRecord && ((ChatLogRecord) record).suppressStackTrace())) {

            record.getThrown().printStackTrace();
        }
    }

    @Override
    public void flush() { }

    @Override
    public void close() throws SecurityException { }
}
